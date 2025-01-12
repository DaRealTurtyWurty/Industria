package dev.turtywurty.industria.blockentity.util.heat;

import dev.turtywurty.heatapi.api.base.NoLimitHeatStorage;
import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.UpdatableBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

public class SyncingNoLimitHeatStorage extends NoLimitHeatStorage implements SyncableStorage {
    private final BlockEntity blockEntity;

    private boolean isDirty = false;

    public SyncingNoLimitHeatStorage(@NotNull BlockEntity blockEntity, boolean insert, boolean extract) {
        super(insert, extract);
        this.blockEntity = blockEntity;
    }

    @Override
    public void setAmount(long amount) {
        long prevAmount = getAmount();
        super.setAmount(amount);

        if(prevAmount != getAmount())
            this.isDirty = true;
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
}
