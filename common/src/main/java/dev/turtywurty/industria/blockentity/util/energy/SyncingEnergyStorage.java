package dev.turtywurty.industria.blockentity.util.energy;

import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.UpdateableBlockEntityLike;
import dev.turtywurty.turtymultiloader.transfer.storage.SimpleEnergyStorage;
import net.minecraft.world.level.block.entity.BlockEntity;

public class SyncingEnergyStorage extends SimpleEnergyStorage implements SyncableStorage {
    private final UpdateableBlockEntityLike blockEntity;
    private boolean isDirty = false;

    public SyncingEnergyStorage(UpdateableBlockEntityLike blockEntity, long capacity, long maxInput, long maxOutput) {
        super(capacity, maxInput, maxOutput);
        this.blockEntity = blockEntity;
    }

    @Override
    protected void onFinalCommit() {
        super.onFinalCommit();
        this.isDirty = true;
    }

    @Override
    public void sync() {
        if (this.isDirty && this.blockEntity instanceof BlockEntity be && be.hasLevel() && !be.getLevel().isClientSide()) {
            this.isDirty = false;

            this.blockEntity.update();
        }
    }

    public UpdateableBlockEntityLike getBlockEntity() {
        return this.blockEntity;
    }
}
