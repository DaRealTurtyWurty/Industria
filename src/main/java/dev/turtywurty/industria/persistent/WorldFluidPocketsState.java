package dev.turtywurty.industria.persistent;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.network.SyncFluidPocketsPayload;
import dev.turtywurty.industria.util.ExtraCodecs;
import dev.turtywurty.industria.util.ExtraPacketCodecs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class WorldFluidPocketsState extends SavedData {
    public static final Codec<WorldFluidPocketsState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            FluidPocket.CODEC.listOf().fieldOf("FluidPockets").forGetter(state -> state.fluidPockets)
    ).apply(instance, pockets -> {
        var state = new WorldFluidPocketsState();
        state.fluidPockets.addAll(pockets);
        return state;
    }));

    private static final SavedDataType<WorldFluidPocketsState> TYPE = new SavedDataType<>(
            Industria.MOD_ID + ".fluid_pockets",
            WorldFluidPocketsState::new,
            CODEC,
            null
    );

    private final List<FluidPocket> fluidPockets = new CopyOnWriteArrayList<>();

    public WorldFluidPocketsState() {
    }

    public WorldFluidPocketsState(List<FluidPocket> fluidPockets) {
        this.fluidPockets.addAll(fluidPockets);
    }

    public static WorldFluidPocketsState getServerState(ServerLevel world) {
        DimensionDataStorage persistentStateManager = world.getDataStorage();
        return persistentStateManager.computeIfAbsent(TYPE);
    }

    public void addFluidPocket(FluidPocket fluidPocket) {
        if (fluidPocket.isEmpty())
            return;

        this.fluidPockets.add(fluidPocket);
        setDirty();
    }

    public boolean removeFluidPocket(FluidPocket fluidPocket) {
        if (this.fluidPockets.remove(fluidPocket)) {
            setDirty();
            return true;
        }

        return false;
    }

    public boolean removePosition(BlockPos pos) {
        boolean changed = false;

        for (FluidPocket fluidPocket : this.fluidPockets) {
            if (fluidPocket.removeIf(pos)) {
                changed = true;
            }
        }

        changed = changed || this.fluidPockets.removeIf(FluidPocket::isEmpty);

        if (changed)
            setDirty();

        return changed;
    }

    public List<FluidPocket> existsBelow(BlockPos pos) {
        for (FluidPocket fluidPocket : this.fluidPockets) {
            if (fluidPocket.fluidPositions.keySet().stream()
                    .anyMatch(pos1 -> pos1.getX() == pos.getX() && pos1.getY() <= pos.getY() && pos1.getZ() == pos.getZ()))
                return List.of(fluidPocket);
        }

        return List.of();
    }

    public static SyncFluidPocketsPayload createSyncPacket(ServerLevel world) {
        return new SyncFluidPocketsPayload(getServerState(world).fluidPockets);
    }

    public static void sync(ServerLevel world) {
        List<ServerPlayer> players = world.players();
        if (!players.isEmpty()) {
            SyncFluidPocketsPayload payload = WorldFluidPocketsState.createSyncPacket(world);
            players.forEach(player -> ServerPlayNetworking.send(player, payload));
        }
    }

    public boolean isPositionInPocket(BlockPos pos) {
        return this.fluidPockets.stream().anyMatch(fluidPocket -> fluidPocket.fluidPositions().containsKey(pos));
    }

    public FluidPocket getFluidPocket(BlockPos pos) {
        for (FluidPocket fluidPocket : this.fluidPockets) {
            if (fluidPocket.isWithinDistance(pos, 0)) {
                return fluidPocket;
            }
        }

        return null;
    }

    public static class FluidPocket {
        private final FluidState fluidState;
        private final Map<BlockPos, Integer> fluidPositions = new HashMap<>();
        private int minX, minY, minZ, maxX, maxY, maxZ;
        private long fluidAmount;

        public FluidPocket(FluidState fluidState, Map<BlockPos, Integer> fluidPositions) {
            this.fluidState = fluidState;
            this.fluidPositions.putAll(fluidPositions);

            updateMinMax();

            this.fluidAmount = fluidPositions.values().stream()
                    .mapToLong(fluidAmount -> fluidAmount)
                    .sum();
        }

        public FluidState fluidState() {
            return this.fluidState;
        }

        public Map<BlockPos, Integer> fluidPositions() {
            return this.fluidPositions;
        }

        public int minX() {
            return this.minX;
        }

        public int minY() {
            return this.minY;
        }

        public int minZ() {
            return this.minZ;
        }

        public int maxX() {
            return this.maxX;
        }

        public int maxY() {
            return this.maxY;
        }

        public int maxZ() {
            return this.maxZ;
        }

        public long fluidAmount() {
            return this.fluidAmount;
        }

        private static final Codec<Map<BlockPos, Integer>> FLUID_POSITIONS_CODEC =
                Codec.unboundedMap(ExtraCodecs.BLOCK_POS_STRING_CODEC, Codec.INT);

        public static final StreamCodec<RegistryFriendlyByteBuf, Map<BlockPos, Integer>> FLUID_POSITIONS_STREAM_CODEC =
                ByteBufCodecs.map(HashMap::new, ExtraPacketCodecs.BLOCK_POS_STRING_CODEC, ByteBufCodecs.INT);

        public static final Codec<FluidPocket> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                FluidState.CODEC.fieldOf("FluidState").forGetter(FluidPocket::fluidState),
                FLUID_POSITIONS_CODEC.fieldOf("FluidPositions").forGetter(FluidPocket::fluidPositions)
        ).apply(instance, FluidPocket::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, FluidPocket> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.fromCodecWithRegistries(FluidState.CODEC), FluidPocket::fluidState,
                FLUID_POSITIONS_STREAM_CODEC, FluidPocket::fluidPositions,
                FluidPocket::new);

        public boolean isEmpty() {
            return this.fluidPositions.isEmpty() || this.fluidState.isEmpty() || this.fluidAmount <= 0;
        }

        public long extractFluid(long amount) {
            long originalAmount = this.fluidAmount;

            if(amount > this.fluidAmount)
                amount = this.fluidAmount;

            Map<BlockPos, Integer> nonEmptyPositions = new HashMap<>();
            for (Map.Entry<BlockPos, Integer> entry : this.fluidPositions.entrySet()) {
                if (entry.getValue() > 0) {
                    nonEmptyPositions.put(entry.getKey(), entry.getValue());
                }
            }

            while(!nonEmptyPositions.isEmpty() && amount > 0) {
                int randomIndex = ThreadLocalRandom.current().nextInt(nonEmptyPositions.size());
                BlockPos randomPos = (BlockPos) nonEmptyPositions.keySet().toArray()[randomIndex];
                int fluidAmount = nonEmptyPositions.get(randomPos);

                if (fluidAmount > amount) {
                    nonEmptyPositions.put(randomPos, fluidAmount - (int) amount);
                    break;
                } else {
                    amount -= fluidAmount;
                    nonEmptyPositions.remove(randomPos);
                }
            }

            long change = originalAmount - amount;
            this.fluidAmount -= change;
            this.fluidPositions.putAll(nonEmptyPositions);

            return change;
        }

        public boolean removeIf(BlockPos pos) {
            if (this.fluidPositions.isEmpty())
                return false;

            if (this.fluidPositions.containsKey(pos)) {
                updateMinMax();
                this.fluidAmount -= this.fluidPositions.remove(pos);
                return true;
            }

            return false;
        }

        private void updateMinMax() {
            this.minX = Integer.MAX_VALUE;
            this.minY = Integer.MAX_VALUE;
            this.minZ = Integer.MAX_VALUE;
            this.maxX = Integer.MIN_VALUE;
            this.maxY = Integer.MIN_VALUE;
            this.maxZ = Integer.MIN_VALUE;

            for (BlockPos pos : this.fluidPositions.keySet()) {
                int x = pos.getX();
                int y = pos.getY();
                int z = pos.getZ();

                this.minX = Math.min(this.minX, x);
                this.minY = Math.min(this.minY, y);
                this.minZ = Math.min(this.minZ, z);
                this.maxX = Math.max(this.maxX, x);
                this.maxY = Math.max(this.maxY, y);
                this.maxZ = Math.max(this.maxZ, z);
            }
        }

        public boolean isWithinDistance(BlockPos pos, int distance) {
            return pos.getX() >= this.minX - distance && pos.getX() <= this.maxX + distance &&
                    pos.getY() >= this.minY - distance && pos.getY() <= this.maxY + distance &&
                    pos.getZ() >= this.minZ - distance && pos.getZ() <= this.maxZ + distance;
        }
    }
}
