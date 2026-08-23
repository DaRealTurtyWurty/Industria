package dev.turtywurty.industria.blockentity.util.fluid;

import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.UpdateableBlockEntityLike;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceTypes;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import dev.turtywurty.turtymultiloader.transfer.storage.SimpleSingleSlotStorage;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;

// TODO: Store a FluidStack as a variable so we don't have to reconstruct constantly (not sure how to do this)
public class SyncingFluidStorage extends SimpleSingleSlotStorage<ResourceVariant<Fluid>> implements SyncableStorage {
    private final BlockEntity blockEntity;
    private final long capacity;

    private boolean isDirty = false;

    public SyncingFluidStorage(@NotNull BlockEntity blockEntity, long capacity) {
        super(ResourceTypes.FLUID);
        this.capacity = capacity;
        this.blockEntity = blockEntity;
    }

    @Override
    protected long getCapacity(ResourceVariant<Fluid> variant) {
        return this.capacity;
    }

    public long getCapacity() {
        return this.capacity;
    }

    @Override
    protected void onFinalCommit() {
        super.onFinalCommit();
        this.isDirty = true;
        sync();
    }

    @Override
    public void sync() {
        if (this.isDirty && this.blockEntity.hasLevel() && !this.blockEntity.getLevel().isClientSide()) {
            this.isDirty = false;

            if (this.blockEntity instanceof UpdateableBlockEntityLike updatableBlockEntity) {
                updatableBlockEntity.update();
            } else {
                this.blockEntity.setChanged();
            }
        }
    }

    public boolean canInsert(FluidStack fluidStack) {
        return (getResource().equals(fluidStack.variant()) || getResource().isBlank())
                && fluidStack.amount() <= this.capacity - getAmount();
    }

    public boolean canExtract(FluidStack fluidStack) {
        return getResource().equals(fluidStack.variant()) && fluidStack.amount() <= getAmount();
    }

    public void markDirty() {
        this.isDirty = true;
        sync();
    }
}
