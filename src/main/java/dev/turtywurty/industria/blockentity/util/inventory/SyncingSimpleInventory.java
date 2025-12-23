package dev.turtywurty.industria.blockentity.util.inventory;

import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.UpdatableBlockEntity;
import net.minecraft.item.ItemStack;

public class SyncingSimpleInventory extends RecipeSimpleInventory implements SyncableStorage {
    private final UpdatableBlockEntity blockEntity;
    private boolean isDirty = false;

    public SyncingSimpleInventory(UpdatableBlockEntity blockEntity, int size) {
        super(size);
        this.blockEntity = blockEntity;
    }

    public SyncingSimpleInventory(UpdatableBlockEntity blockEntity, ItemStack... stacks) {
        super(stacks);
        this.blockEntity = blockEntity;
    }

    @Override
    public void sync() {
        if (this.isDirty && this.blockEntity != null && this.blockEntity.hasWorld() && !this.blockEntity.getWorld().isClient()) {
            this.isDirty = false;

            this.blockEntity.update();
        }
    }

    @Override
    public void markDirty() {
        super.markDirty();
        this.isDirty = true;
    }

    public UpdatableBlockEntity getBlockEntity() {
        return this.blockEntity;
    }
}
