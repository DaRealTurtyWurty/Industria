package dev.turtywurty.industria.blockentity.util.gas;

import dev.turtywurty.gasapi.api.Gas;
import dev.turtywurty.gasapi.api.storage.SingleGasStorage;
import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.UpdateableBlockEntityLike;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

public class SyncingGasStorage extends SingleGasStorage implements SyncableStorage {
    private final BlockEntity blockEntity;
    private final long capacity;

    private boolean isDirty = false;

    public SyncingGasStorage(@NotNull BlockEntity blockEntity, long capacity) {
        this.capacity = capacity;
        this.blockEntity = blockEntity;
    }

    @Override
    protected long getCapacity(ResourceVariant<Gas> variant) {
        return this.capacity;
    }

    public long getCapacity() {
        return this.capacity;
    }

    @Override
    protected void onFinalCommit() {
        super.onFinalCommit();
        this.isDirty = true;
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

    public boolean canInsert(GasStack stack) {
        return (getResource().equals(stack.variant()) || getResource().isBlank())
                && stack.amount() <= this.capacity - getAmount();
    }

    public boolean canExtract(GasStack stack) {
        return getResource().equals(stack.variant()) && stack.amount() <= getAmount();
    }
}
