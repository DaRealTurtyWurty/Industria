package dev.turtywurty.industria.blockentity.util.slurry;

import dev.turtywurty.fabricslurryapi.api.SlurryVariant;
import dev.turtywurty.fabricslurryapi.api.storage.SingleSlurryStorage;
import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.UpdatableBlockEntity;
import net.minecraft.block.entity.BlockEntity;
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
    protected long getCapacity(SlurryVariant variant) {
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
    public boolean canInsert(SlurryVariant variant) {
        return super.canInsert(variant);
    }

    @Override
    public boolean canExtract(SlurryVariant variant) {
        return super.canExtract(variant);
    }

    public boolean canInsert(SlurryStack stack) {
        return (this.variant == stack.variant() || this.variant.isBlank()) && stack.amount() <= this.capacity - this.amount;
    }

    public boolean canExtract(SlurryStack stack) {
        return this.variant == stack.variant() && stack.amount() <= this.amount;
    }
}
