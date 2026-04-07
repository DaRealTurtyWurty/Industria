package dev.turtywurty.industria.blockentity.util.inventory;

import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.UpdateableBlockEntityLike;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class SyncingSimpleInventory extends RecipeSimpleInventory implements SyncableStorage {
    private final UpdateableBlockEntityLike blockEntity;
    private boolean isDirty = false;

    public SyncingSimpleInventory(UpdateableBlockEntityLike blockEntity, int size) {
        super(size);
        this.blockEntity = blockEntity;
    }

    public SyncingSimpleInventory(UpdateableBlockEntityLike blockEntity, ItemStack... stacks) {
        super(stacks);
        this.blockEntity = blockEntity;
    }

    @Override
    public void sync() {
        if (this.isDirty && this.blockEntity instanceof BlockEntity blockEntity &&
                blockEntity.hasLevel() && !blockEntity.getLevel().isClientSide()) {
            this.isDirty = false;

            this.blockEntity.update();
        }
    }

    @Override
    public void setChanged() {
        super.setChanged();
        this.isDirty = true;
    }

    public UpdateableBlockEntityLike getBlockEntity() {
        return this.blockEntity;
    }
}
