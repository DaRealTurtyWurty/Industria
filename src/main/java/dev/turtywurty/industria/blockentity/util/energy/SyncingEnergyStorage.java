package dev.turtywurty.industria.blockentity.util.energy;

import dev.turtywurty.industria.blockentity.util.UpdatableBlockEntity;
import team.reborn.energy.api.base.SimpleEnergyStorage;

public class SyncingEnergyStorage extends SimpleEnergyStorage {
    private final UpdatableBlockEntity blockEntity;

    public SyncingEnergyStorage(UpdatableBlockEntity blockEntity, long capacity, long maxInput, long maxOutput) {
        super(capacity, maxInput, maxOutput);
        this.blockEntity = blockEntity;
    }

    @Override
    protected void onFinalCommit() {
        super.onFinalCommit();
        this.blockEntity.update();
    }

    public UpdatableBlockEntity getBlockEntity() {
        return this.blockEntity;
    }
}
