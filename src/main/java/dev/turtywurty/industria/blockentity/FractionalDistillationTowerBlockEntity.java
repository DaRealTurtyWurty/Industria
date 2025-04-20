package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.SyncableTickableBlockEntity;
import dev.turtywurty.industria.blockentity.util.UpdatableBlockEntity;
import dev.turtywurty.industria.blockentity.util.fluid.SyncingFluidStorage;
import dev.turtywurty.industria.blockentity.util.fluid.WrappedFluidStorage;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FractionalDistillationTowerBlockEntity extends UpdatableBlockEntity implements SyncableTickableBlockEntity {
    private BlockPos controllerPos = null;
    private int ticks = 0;

    private final WrappedFluidStorage<SingleFluidStorage> tank = new WrappedFluidStorage<>();

    public FractionalDistillationTowerBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypeInit.FRACTIONAL_DISTILLATION_TOWER, pos, state);
        this.tank.addStorage(new SyncingFluidStorage(this, FluidConstants.BUCKET * 5));
    }

    @Override
    public List<SyncableStorage> getSyncableStorages() {
        return List.of((SyncableStorage) this.tank.getStorage(0));
    }

    @Override
    public void onTick() {
        if(this.world == null || this.world.isClient)
            return;

        if(this.ticks++ == 0) {
            this.controllerPos = searchForController();
            if(this.controllerPos == null) {
                this.world.breakBlock(this.pos, true);
            }
        } else if (this.ticks > Integer.MAX_VALUE - 1) {
            this.ticks = 1;
        }
    }

    private BlockPos searchForController() {
        for (int i = 1; i <= 8; i++) {
            BlockPos pos = this.pos.down(i);
            if (this.world.getBlockEntity(pos) instanceof FractionalDistillationControllerBlockEntity blockEntity) {
                if(!blockEntity.addTower(this.pos))
                    return null;

                return pos;
            }
        }

        return null;
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        this.controllerPos = nbt.contains("ControllerPos") ? BlockPos.fromLong(nbt.getLong("ControllerPos", 0L)) : null;
        this.ticks = nbt.getInt("Ticks", 0);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        if(this.controllerPos != null)
            nbt.putLong("ControllerPos", this.controllerPos.asLong());

        nbt.putInt("Ticks", this.ticks);
    }

    @Override
    public @Nullable Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries) {
        NbtCompound nbt = super.toInitialChunkDataNbt(registries);
        writeNbt(nbt, registries);
        return nbt;
    }

    public BlockPos getControllerPos() {
        return this.controllerPos;
    }

    public WrappedFluidStorage<SingleFluidStorage> getTank() {
        return this.tank;
    }

    public @NotNull SingleFluidStorage getFluidStorage() {
        return getFluidProvider(null);
    }

    public @NotNull SingleFluidStorage getFluidProvider(@Nullable Direction side) {
        return this.tank.getStorage(0);
    }
}
