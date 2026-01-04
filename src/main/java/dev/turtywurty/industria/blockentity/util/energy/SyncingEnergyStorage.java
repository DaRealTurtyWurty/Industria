package dev.turtywurty.industria.blockentity.util.energy;

import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.UpdatableBlockEntity;
import team.reborn.energy.api.base.SimpleEnergyStorage;

public class SyncingEnergyStorage extends SimpleEnergyStorage implements SyncableStorage {
    private final UpdatableBlockEntity blockEntity;
    private boolean isDirty = false;

    public SyncingEnergyStorage(UpdatableBlockEntity blockEntity, long capacity, long maxInput, long maxOutput) {
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
        if (this.isDirty && this.blockEntity != null && this.blockEntity.hasLevel() && !this.blockEntity.getLevel().isClientSide()) {
            this.isDirty = false;

            this.blockEntity.update();
        }
    }

    public UpdatableBlockEntity getBlockEntity() {
        return this.blockEntity;
    }
}
