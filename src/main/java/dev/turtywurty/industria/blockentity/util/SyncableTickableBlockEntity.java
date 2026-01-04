package dev.turtywurty.industria.blockentity.util;

import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;

public interface SyncableTickableBlockEntity extends TickableBlockEntity {
    List<SyncableStorage> getSyncableStorages();

    void onTick();

    default void onClientTick() {
    }

    @Override
    default void tick() {
        if (this instanceof BlockEntity blockEntity) {
            if (blockEntity.getLevel() != null && blockEntity.getLevel().isClientSide()) {
                onClientTick();
            } else {
                onTick();
            }
        }

        getSyncableStorages().forEach(SyncableStorage::sync);

        if (this instanceof UpdatableBlockEntity updatableBlockEntity) {
            updatableBlockEntity.endTick();
        }
    }
}
