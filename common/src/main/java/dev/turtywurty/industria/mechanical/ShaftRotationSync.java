package dev.turtywurty.industria.mechanical;

import dev.turtywurty.industria.block.ShaftBlock;
import dev.turtywurty.industria.network.mechanical.ShaftChunkPayload;
import dev.turtywurty.industria.network.mechanical.ShaftRotationPayload;
import dev.turtywurty.turtymultiloader.network.NetworkService;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ShaftRotationSync {
    private static final double RPM_UPDATE_THRESHOLD = 0.01;
    private static final int ROTATION_SYNC_INTERVAL = 10;

    private Map<ChunkPos, Map<MechanicalNetwork, Set<BlockPos>>> shaftsByChunk = Map.of();
    private final Map<MechanicalNetwork, Set<ChunkPos>> shaftChunksByNetwork = new HashMap<>();
    private final Map<MechanicalNetwork, ShaftRotation> lastBroadcast = new HashMap<>();

    public void updateTopology(ServerLevel level, Map<BlockPos, MechanicalNetwork> networksByPos) {
        Map<ChunkPos, Map<MechanicalNetwork, Set<BlockPos>>> newShaftsByChunk = new HashMap<>();
        shaftChunksByNetwork.clear();
        networksByPos.forEach((pos, network) -> {
            if (level.getBlockState(pos).getBlock() instanceof ShaftBlock) {
                ChunkPos chunk = ChunkPos.containing(pos);
                newShaftsByChunk.computeIfAbsent(chunk, _ -> new HashMap<>())
                    .computeIfAbsent(network, _ -> new HashSet<>()).add(pos);
                shaftChunksByNetwork.computeIfAbsent(network, _ -> new HashSet<>()).add(chunk);
            }
        });

        Set<ChunkPos> affectedChunks = new HashSet<>(shaftsByChunk.keySet());
        affectedChunks.addAll(newShaftsByChunk.keySet());
        for (ChunkPos chunk : affectedChunks) {
            Map<MechanicalNetwork, Set<BlockPos>> previous = shaftsByChunk.getOrDefault(chunk, Map.of());
            Map<MechanicalNetwork, Set<BlockPos>> current = newShaftsByChunk.getOrDefault(chunk, Map.of());
            if (!previous.equals(current)) {
                List<ServerPlayer> players = level.getChunkSource().chunkMap.getPlayers(chunk, false);
                if (!players.isEmpty()) {
                    ShaftChunkPayload payload = chunkPayload(level, chunk, current);
                    players.forEach(player -> NetworkService.get().sendToPlayer(player, payload));
                }
            }
        }

        shaftsByChunk = newShaftsByChunk;
        lastBroadcast.keySet().retainAll(shaftChunksByNetwork.keySet());
        for (MechanicalNetwork network : shaftChunksByNetwork.keySet()) {
            lastBroadcast.computeIfAbsent(network, MechanicalNetwork::getRotationSnapshot);
        }
    }

    public void sendInitialChunk(ServerLevel level, ServerPlayer player, ChunkPos chunk) {
        Map<MechanicalNetwork, Set<BlockPos>> shafts = shaftsByChunk.get(chunk);
        if (shafts != null) {
            NetworkService.get().sendToPlayer(player, chunkPayload(level, chunk, shafts));
        }
    }

    public void tick(ServerLevel level) {
        if (level.players().isEmpty())
            return;

        shaftChunksByNetwork.forEach((network, chunks) -> {
            ShaftRotation rotation = network.getRotationSnapshot();
            ShaftRotation previous = lastBroadcast.get(network);
            if (!shouldBroadcast(previous, rotation))
                return;

            Set<ServerPlayer> players = new HashSet<>();
            for (ChunkPos chunk : chunks) {
                players.addAll(level.getChunkSource().chunkMap.getPlayers(chunk, false));
            }

            if (!players.isEmpty()) {
                var payload = new ShaftRotationPayload(level.dimension(), network.getId(), rotation);
                players.forEach(player -> NetworkService.get().sendToPlayer(player, payload));
            }

            lastBroadcast.put(network, rotation);
        });
    }

    private static boolean shouldBroadcast(ShaftRotation previous, ShaftRotation current) {
        return Math.signum(previous.rpm()) != Math.signum(current.rpm())
            || Math.abs(previous.rpm() - current.rpm()) >= RPM_UPDATE_THRESHOLD
            || (current.rpm() != 0.0 && current.gameTick() - previous.gameTick() >= ROTATION_SYNC_INTERVAL);
    }

    private static ShaftChunkPayload chunkPayload(
        ServerLevel level,
        ChunkPos chunk,
        Map<MechanicalNetwork, Set<BlockPos>> shafts
    ) {
        List<ShaftChunkPayload.NetworkShafts> networks = shafts.entrySet().stream().map(entry -> {
            MechanicalNetwork network = entry.getKey();
            return new ShaftChunkPayload.NetworkShafts(
                network.getId(), network.getRotationSnapshot(), List.copyOf(entry.getValue()));
        }).toList();
        return new ShaftChunkPayload(level.dimension(), chunk, networks);
    }
}
