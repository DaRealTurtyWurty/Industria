package dev.turtywurty.industria.blockentity.util.heat;

import dev.turtywurty.heatapi.api.base.SimpleHeatStorage;
import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.UpdatableBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class SyncingHeatStorage extends SimpleHeatStorage implements SyncableStorage {
    private final BlockEntity blockEntity;
    private boolean isDirty = false;

    public SyncingHeatStorage(@NotNull BlockEntity blockEntity, long capacity, long maxInsert, long maxExtract) {
        super(capacity, maxInsert, maxExtract);
        Objects.requireNonNull(blockEntity, "BlockEntity cannot be null!");

        this.blockEntity = blockEntity;
    }

    @Override
    protected void onFinalCommit() {
        super.onFinalCommit();
        this.isDirty = true;
    }

    @Override
    public void sync() {
        if (this.isDirty && this.blockEntity.hasWorld() && !this.blockEntity.getWorld().isClient()) {
            this.isDirty = false;

            if (this.blockEntity instanceof UpdatableBlockEntity updatableBlockEntity) {
                updatableBlockEntity.update();
            } else {
                this.blockEntity.markDirty();
            }
        }
    }
}
