package dev.turtywurty.industria.blockentity.util.fluid;

import dev.turtywurty.industria.blockentity.util.UpdatableBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.minecraft.block.entity.BlockEntity;

public class SyncedFluidStorage extends SingleFluidStorage {
    private final BlockEntity blockEntity;
    private final long capacity;

    public SyncedFluidStorage(BlockEntity blockEntity, long capacity) {
        this.capacity = capacity;
        this.blockEntity = blockEntity;
    }

    @Override
    protected long getCapacity(FluidVariant variant) {
        return this.capacity;
    }

    @Override
    protected void onFinalCommit() {
        super.onFinalCommit();
        if (this.blockEntity instanceof UpdatableBlockEntity updatableBlockEntity) {
            updatableBlockEntity.update();
        } else {
            this.blockEntity.markDirty();
        }
    }
}
