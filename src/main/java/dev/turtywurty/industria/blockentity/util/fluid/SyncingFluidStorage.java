package dev.turtywurty.industria.blockentity.util.fluid;

import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.UpdatableBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.minecraft.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

// TODO: Store a FluidStack as a variable so we don't have to reconstruct constantly (not sure how to do this)
public class SyncingFluidStorage extends SingleFluidStorage implements SyncableStorage {
    private final BlockEntity blockEntity;
    private final long capacity;

    private boolean isDirty = false;

    public SyncingFluidStorage(@NotNull BlockEntity blockEntity, long capacity) {
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
        this.isDirty = true;
    }

    @Override
    public void sync() {
        if (this.isDirty && this.blockEntity.hasWorld() && !this.blockEntity.getWorld().isClient) {
            this.isDirty = false;

            if (this.blockEntity instanceof UpdatableBlockEntity updatableBlockEntity) {
                updatableBlockEntity.update();
            } else {
                this.blockEntity.markDirty();
            }
        }
    }

    @Override
    public boolean canInsert(FluidVariant variant) {
        return super.canInsert(variant);
    }

    @Override
    public boolean canExtract(FluidVariant variant) {
        return super.canExtract(variant);
    }

    public boolean canInsert(FluidStack fluidStack) {
        return (this.variant == fluidStack.variant() || this.variant.isBlank()) && fluidStack.amount() <= this.capacity - this.amount;
    }

    public boolean canExtract(FluidStack fluidStack) {
        return this.variant == fluidStack.variant() && fluidStack.amount() <= this.amount;
    }

    public void markDirty() {
        this.isDirty = true;
    }
}
