package dev.turtywurty.industria.blockentity.util.slurry;

import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.UpdateableBlockEntityLike;
import dev.turtywurty.slurryapi.api.Slurry;
import dev.turtywurty.slurryapi.api.storage.SingleSlurryStorage;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

public class SyncingSlurryStorage extends SingleSlurryStorage implements SyncableStorage {
    private final BlockEntity blockEntity;
    private final long capacity;

    private boolean isDirty = false;

    public SyncingSlurryStorage(@NotNull BlockEntity blockEntity, long capacity) {
        this.capacity = capacity;
        this.blockEntity = blockEntity;
    }

    @Override
    protected long getCapacity(ResourceVariant<Slurry> variant) {
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

    public boolean canInsert(SlurryStack stack) {
        return (getResource().equals(stack.variant()) || getResource().isBlank())
                && stack.amount() <= this.capacity - getAmount();
    }

    public boolean canExtract(SlurryStack stack) {
        return getResource().equals(stack.variant()) && stack.amount() <= getAmount();
    }
}
