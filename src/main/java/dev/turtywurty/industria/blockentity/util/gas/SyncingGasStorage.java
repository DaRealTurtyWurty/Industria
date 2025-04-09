package dev.turtywurty.industria.blockentity.util.gas;

import dev.turtywurty.gasapi.api.GasVariant;
import dev.turtywurty.gasapi.api.storage.SingleGasStorage;
import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.UpdatableBlockEntity;
import net.minecraft.block.entity.BlockEntity;
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
    protected long getCapacity(GasVariant variant) {
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
    public boolean canInsert(GasVariant variant) {
        return super.canInsert(variant);
    }

    @Override
    public boolean canExtract(GasVariant variant) {
        return super.canExtract(variant);
    }

    public boolean canInsert(GasStack stack) {
        return (this.variant.equals(stack.variant()) || this.variant.isBlank()) && stack.amount() <= this.capacity - this.amount;
    }

    public boolean canExtract(GasStack stack) {
        return this.variant.equals(stack.variant()) && stack.amount() <= this.amount;
    }
}
