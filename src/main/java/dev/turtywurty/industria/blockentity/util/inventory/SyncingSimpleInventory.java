package dev.turtywurty.industria.blockentity.util.inventory;

import dev.turtywurty.industria.blockentity.util.UpdatableBlockEntity;
import net.minecraft.item.ItemStack;

public class SyncingSimpleInventory extends RecipeSimpleInventory {
    private final UpdatableBlockEntity blockEntity;

    public SyncingSimpleInventory(UpdatableBlockEntity blockEntity, int size) {
        super(size);
        this.blockEntity = blockEntity;
    }

    public SyncingSimpleInventory(UpdatableBlockEntity blockEntity, ItemStack... stacks) {
        super(stacks);
        this.blockEntity = blockEntity;
    }

    @Override
    public void markDirty() {
        super.markDirty();
        this.blockEntity.update();
    }

    public UpdatableBlockEntity getBlockEntity() {
        return this.blockEntity;
    }
}
