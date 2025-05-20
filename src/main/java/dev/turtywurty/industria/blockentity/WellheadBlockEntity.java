package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.SyncableTickableBlockEntity;
import dev.turtywurty.industria.blockentity.util.UpdatableBlockEntity;
import dev.turtywurty.industria.blockentity.util.fluid.OutputFluidStorage;
import dev.turtywurty.industria.blockentity.util.fluid.WrappedFluidStorage;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.persistent.WorldFluidPocketsState;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WellheadBlockEntity extends UpdatableBlockEntity implements SyncableTickableBlockEntity {
    private BlockPos oilPumpJackPos;
    private final Map<BlockPos, Integer> drillTubes = new HashMap<>();

    private final WrappedFluidStorage<SingleFluidStorage> wrappedFluidStorage = new WrappedFluidStorage<>();

    public WellheadBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypeInit.WELLHEAD, pos, state);

        OutputFluidStorage storage = new OutputFluidStorage(this, FluidConstants.BUCKET);
        Direction.Type.HORIZONTAL.stream().forEach(direction ->
                this.wrappedFluidStorage.addStorage(storage, direction));
        this.wrappedFluidStorage.addStorage(storage, Direction.UP);
    }

    @Override
    public List<SyncableStorage> getSyncableStorages() {
        return List.of(getFluidStorageBuffer());
    }

    public OutputFluidStorage getFluidStorageBuffer() {
        return (OutputFluidStorage) this.wrappedFluidStorage.getStorage(0);
    }

    public SingleFluidStorage getFluidProvider(@Nullable Direction direction) {
        return this.wrappedFluidStorage.getStorage(direction);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);

        this.oilPumpJackPos = nbt.get("OilPumpJackPos", BlockPos.CODEC).orElse(null);
        this.drillTubes.clear();
        NbtCompound drillTubesNbt = nbt.getCompoundOrEmpty("DrillTubes");
        for (String key : drillTubesNbt.getKeys()) {
            BlockPos pos = BlockPos.fromLong(Long.parseLong(key));
            int fluidAmount = drillTubesNbt.getInt(key, 0);
            this.drillTubes.put(pos, fluidAmount);
        }

        if (nbt.contains("FluidTank")) {
            this.wrappedFluidStorage.readNbt(nbt.getListOrEmpty("FluidTank"), registries);
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);

        if (this.oilPumpJackPos != null) {
            nbt.put("OilPumpJackPos", BlockPos.CODEC, this.oilPumpJackPos);
        }

        NbtCompound drillTubesNbt = new NbtCompound();
        for (Map.Entry<BlockPos, Integer> entry : this.drillTubes.entrySet()) {
            drillTubesNbt.putLong(String.valueOf(entry.getKey().asLong()), entry.getValue());
        }

        nbt.put("DrillTubes", drillTubesNbt);
        nbt.put("FluidTank", this.wrappedFluidStorage.writeNbt(registries));
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries) {
        NbtCompound nbt = super.toInitialChunkDataNbt(registries);
        writeNbt(nbt, registries);
        return nbt;
    }

    @Override
    public @Nullable Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    private void findDrillTubes() {
        if (this.world == null || this.oilPumpJackPos == null)
            return;

        BlockPos.Mutable mutablePos = this.pos.down().mutableCopy();
        BlockState below = this.world.getBlockState(mutablePos);
        while (below.isOf(BlockInit.DRILL_TUBE)) {
            this.drillTubes.put(mutablePos.toImmutable(), 0);
            mutablePos.move(Direction.DOWN);
            below = this.world.getBlockState(mutablePos);
        }
    }

    @Override
    public void onTick() {
        if (this.world == null || this.world.isClient || this.oilPumpJackPos == null)
            return;

        if (!(this.world.getBlockEntity(this.oilPumpJackPos) instanceof OilPumpJackBlockEntity oilPumpJackBlockEntity)
                || !oilPumpJackBlockEntity.isRunning())
            return;

        findDrillTubes();

        BlockPos bottomOfTubes = this.drillTubes.keySet().stream()
                .min(Comparator.comparingInt(Vec3i::getY))
                .orElse(null);
        if (bottomOfTubes == null)
            return;

        bottomOfTubes = bottomOfTubes.down();
        WorldFluidPocketsState fluidPocketsState = WorldFluidPocketsState.getServerState(((ServerWorld) this.world));
        if (fluidPocketsState == null)
            return;

        if (!fluidPocketsState.isPositionInPocket(bottomOfTubes))
            return;

        WorldFluidPocketsState.FluidPocket fluidPocket = fluidPocketsState.getFluidPocket(bottomOfTubes);
        if (fluidPocket == null)
            return;

        if (fluidPocket.isEmpty())
            return;

        int inBottomTube = this.drillTubes.get(bottomOfTubes.up());
        if (inBottomTube >= FluidConstants.BUCKET)
            return;

        int amountToExtract = (int) (FluidConstants.BUCKET - inBottomTube);

        long extracted = fluidPocket.extractFluid(amountToExtract);
        if (extracted == 0) {
            oilPumpJackBlockEntity.setRunning(false);
            return;
        }

        this.drillTubes.put(bottomOfTubes.up(), inBottomTube + (int) extracted);
        fluidPocketsState.markDirty();
    }

    public Map<BlockPos, Integer> getDrillTubes() {
        return this.drillTubes;
    }

    public void setOilPumpJackPos(@Nullable BlockPos pos) {
        this.oilPumpJackPos = pos;
        update();
    }

    public boolean hasOilPumpJack() {
        return this.oilPumpJackPos != null;
    }

    public BlockPos getOilPumpJackPos() {
        return this.oilPumpJackPos;
    }

    @Override
    public void markRemoved() {
        super.markRemoved();
        if (this.oilPumpJackPos != null && this.world != null) {
            if (this.world.getBlockEntity(this.oilPumpJackPos) instanceof OilPumpJackBlockEntity oilPumpJackBlockEntity) {
                oilPumpJackBlockEntity.removeWellhead();
            }
        }
    }
}
