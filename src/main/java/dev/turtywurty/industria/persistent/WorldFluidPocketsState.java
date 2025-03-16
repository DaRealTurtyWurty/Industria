package dev.turtywurty.industria.persistent;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.network.SyncFluidPocketsPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class WorldFluidPocketsState extends PersistentState {
    private static final Type<WorldFluidPocketsState> TYPE = new Type<>(
            WorldFluidPocketsState::new,
            WorldFluidPocketsState::fromNbt,
            null
    );

    private final List<FluidPocket> fluidPockets = new CopyOnWriteArrayList<>();

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        var fluidPocketList = new NbtList();
        for (FluidPocket fluidPocket : this.fluidPockets) {
            if(fluidPocket.isEmpty())
                continue;

            fluidPocketList.add(FluidPocket.CODEC.encodeStart(NbtOps.INSTANCE, fluidPocket)
                    .resultOrPartial(Industria.LOGGER::error)
                    .orElse(new NbtCompound()));
        }

        nbt.put("FluidPockets", fluidPocketList);
        return nbt;
    }

    public static WorldFluidPocketsState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        var state = new WorldFluidPocketsState();

        if(!nbt.contains("FluidPockets", NbtElement.LIST_TYPE))
            return state;

        NbtList fluidPockets = nbt.getList("FluidPockets", NbtElement.COMPOUND_TYPE);
        for (NbtElement fluidPocketElement : fluidPockets) {
            FluidPocket fluidPocket = FluidPocket.CODEC.parse(NbtOps.INSTANCE, fluidPocketElement)
                    .resultOrPartial(Industria.LOGGER::error)
                    .orElseGet(() -> new FluidPocket(Fluids.EMPTY.getDefaultState(), new ArrayList<>()));
            if(fluidPocket.isEmpty())
                continue;

            state.fluidPockets.add(fluidPocket);
        }

        return state;
    }

    public static WorldFluidPocketsState getServerState(ServerWorld world) {
        PersistentStateManager persistentStateManager = world.getPersistentStateManager();
        return persistentStateManager.getOrCreate(TYPE, Industria.MOD_ID + ".fluid_pockets");
    }

    public void addFluidPocket(FluidPocket fluidPocket) {
        if(fluidPocket.isEmpty())
            return;

        this.fluidPockets.add(fluidPocket);
        markDirty();
    }

    public boolean removeFluidPocket(FluidPocket fluidPocket) {
        if(this.fluidPockets.remove(fluidPocket)) {
            markDirty();
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
            markDirty();

        return changed;
    }

    public List<FluidPocket> existsBelow(BlockPos pos) {
        for (FluidPocket fluidPocket : this.fluidPockets) {
            if(fluidPocket.fluidPositions.stream()
                    .anyMatch(pos1 -> pos1.getX() == pos.getX() && pos1.getY() <= pos.getY() && pos1.getZ() == pos.getZ()))
                return List.of(fluidPocket);
        }

        return List.of();
    }

    public static SyncFluidPocketsPayload createSyncPacket(ServerWorld world) {
        return new SyncFluidPocketsPayload(getServerState(world).fluidPockets);
    }

    public static void sync(ServerWorld world) {
        List<ServerPlayerEntity> players = world.getPlayers();
        if(!players.isEmpty()) {
            SyncFluidPocketsPayload payload = WorldFluidPocketsState.createSyncPacket(world);
            players.forEach(player -> ServerPlayNetworking.send(player, payload));
        }
    }

    public boolean isPositionInPocket(BlockPos pos) {
        return this.fluidPockets.stream().anyMatch(fluidPocket -> fluidPocket.fluidPositions().contains(pos));
    }

    public static class FluidPocket {
        private final FluidState fluidState;
        private final List<BlockPos> fluidPositions = new ArrayList<>();
        private int minX, minY, minZ, maxX, maxY, maxZ;

        public FluidPocket(FluidState fluidState, Collection<BlockPos> fluidPositions) {
            this.fluidState = fluidState;
            this.fluidPositions.addAll(fluidPositions);

            updateMinMax();
        }

        public FluidState fluidState() {
            return this.fluidState;
        }

        public List<BlockPos> fluidPositions() {
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

        public static final Codec<FluidPocket> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                FluidState.CODEC.fieldOf("FluidState").forGetter(FluidPocket::fluidState),
                BlockPos.CODEC.listOf().fieldOf("FluidPositions").forGetter(FluidPocket::fluidPositions)
        ).apply(instance, FluidPocket::new));

        public static final PacketCodec<RegistryByteBuf, FluidPocket> PACKET_CODEC = PacketCodec.tuple(
                PacketCodecs.registryCodec(FluidState.CODEC), FluidPocket::fluidState,
                PacketCodecs.collection(ArrayList::new, BlockPos.PACKET_CODEC), FluidPocket::fluidPositions,
                FluidPocket::new);

        public boolean isEmpty() {
            return this.fluidPositions.isEmpty() || this.fluidState.isEmpty();
        }

        public boolean removeIf(BlockPos pos) {
            if(this.fluidPositions.removeIf(pos1 -> Objects.equals(pos1, pos))) {
                updateMinMax();
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

            for (BlockPos pos : this.fluidPositions) {
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
