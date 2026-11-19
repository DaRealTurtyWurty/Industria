package dev.turtywurty.industria.data;

import dev.turtywurty.industria.mechanical.ShaftRotation;
import dev.turtywurty.industria.network.mechanical.ShaftChunkPayload;
import dev.turtywurty.industria.network.mechanical.ShaftRotationPayload;
import dev.turtywurty.turtymultiloader.event.client.ClientEvents;
import dev.turtywurty.turtymultiloader.network.NetworkService;
import dev.turtywurty.turtymultiloader.network.PayloadPhase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public final class ClientShaftRotations {
    private static final Map<ChunkPos, Map<BlockPos, Long>> SHAFTS_BY_CHUNK = new HashMap<>();
    private static final Map<ChunkPos, Map<BlockPos, Long>> SHAFT_CHUNKS_VIEW = Collections
        .unmodifiableMap(SHAFTS_BY_CHUNK);
    private static final Map<Long, TrackedRotation> ROTATIONS = new HashMap<>();
    private static @Nullable ClientLevel trackedLevel;

    private ClientShaftRotations() {
    }

    public static void init() {
        NetworkService network = NetworkService.get();
        network.registerClientHandler(PayloadPhase.PLAY, ShaftChunkPayload.ID,
            (payload, context) -> context.enqueueWork(() -> applyChunk(payload)));
        network.registerClientHandler(PayloadPhase.PLAY, ShaftRotationPayload.ID,
            (payload, context) -> context.enqueueWork(() -> applyRotation(payload)));
        ClientEvents.onLevelLeave(level -> {
            if (trackedLevel == level) {
                clear();
            }
        });

        ClientEvents.onDisconnection(_ -> clear());
    }

    public static Map<BlockPos, Long> getShafts(ClientLevel level, ChunkPos chunk) {
        return getShaftChunks(level).getOrDefault(chunk, Map.of());
    }

    public static Map<ChunkPos, Map<BlockPos, Long>> getShaftChunks(ClientLevel level) {
        return trackedLevel == level ? SHAFT_CHUNKS_VIEW : Map.of();
    }

    public static @Nullable ShaftRotation getRotation(ClientLevel level, long networkId) {
        TrackedRotation tracked = trackedLevel == level ? ROTATIONS.get(networkId) : null;
        return tracked == null ? null : tracked.rotation;
    }

    public static void onChunkUnloaded(ClientLevel level, ChunkPos chunk) {
        if (trackedLevel != level)
            return;

        Map<BlockPos, Long> removed = SHAFTS_BY_CHUNK.remove(chunk);
        if (removed != null) {
            for (long networkId : new HashSet<>(removed.values())) {
                TrackedRotation tracked = ROTATIONS.get(networkId);
                if (--tracked.chunkCount == 0) {
                    ROTATIONS.remove(networkId);
                }
            }
        }
    }

    private static void applyChunk(ShaftChunkPayload payload) {
        ClientLevel level = Minecraft.getInstance().level;
        ChunkPos chunk = payload.chunk();
        if (level == null || !level.dimension().equals(payload.level()) || !level.hasChunk(chunk.x(), chunk.z()))
            return;

        if (trackedLevel != level) {
            clear();
            trackedLevel = level;
        }

        onChunkUnloaded(level, chunk);
        if (payload.networks().isEmpty())
            return;

        Map<BlockPos, Long> shafts = new HashMap<>();
        for (ShaftChunkPayload.NetworkShafts network : payload.networks()) {
            long networkId = network.networkId();
            TrackedRotation tracked = ROTATIONS.computeIfAbsent(networkId,
                _ -> new TrackedRotation(network.rotation()));
            tracked.update(network.rotation());
            tracked.chunkCount++;
            for (BlockPos pos : network.positions()) {
                shafts.put(pos, networkId);
            }
        }

        SHAFTS_BY_CHUNK.put(chunk, Map.copyOf(shafts));
    }

    private static void applyRotation(ShaftRotationPayload payload) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || trackedLevel != level || !level.dimension().equals(payload.level()))
            return;

        TrackedRotation tracked = ROTATIONS.get(payload.networkId());
        if (tracked != null) {
            tracked.update(payload.rotation());
        }
    }

    private static void clear() {
        trackedLevel = null;
        SHAFTS_BY_CHUNK.clear();
        ROTATIONS.clear();
    }

    private static final class TrackedRotation {
        private ShaftRotation rotation;
        private int chunkCount;

        private TrackedRotation(ShaftRotation rotation) {
            this.rotation = rotation;
        }

        private void update(ShaftRotation rotation) {
            if (rotation.gameTick() >= this.rotation.gameTick()) {
                this.rotation = rotation;
            }
        }
    }
}
