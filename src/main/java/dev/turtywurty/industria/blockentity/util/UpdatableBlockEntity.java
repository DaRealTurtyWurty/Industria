package dev.turtywurty.industria.blockentity.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;

public class UpdatableBlockEntity extends BlockEntity {
    protected boolean isDirty = false;

    public UpdatableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void update() {
        this.isDirty = true;
        if (!shouldWaitForEndTick()) {
            markDirty();

            if (this.world != null && !this.world.isClient) {
                this.world.updateListeners(this.pos, getCachedState(), getCachedState(), Block.NOTIFY_ALL);
            }
        }
    }

    public boolean shouldWaitForEndTick() {
        return true;
    }

    public void endTick() {
        if (this.isDirty) {
            this.isDirty = false;

            markDirty();

            if (this.world != null) {
                this.world.updateListeners(this.pos, getCachedState(), getCachedState(), Block.NOTIFY_ALL);
            }
        }
    }
}
