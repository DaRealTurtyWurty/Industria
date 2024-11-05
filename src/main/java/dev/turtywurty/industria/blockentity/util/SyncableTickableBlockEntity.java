package dev.turtywurty.industria.blockentity.util;

import java.util.List;

public interface SyncableTickableBlockEntity extends TickableBlockEntity {
    List<SyncableStorage> getSyncableStorages();

    void onTick();

    @Override
    default void tick() {
        onTick();
        getSyncableStorages().forEach(SyncableStorage::sync);

        if(this instanceof UpdatableBlockEntity updatableBlockEntity) {
            updatableBlockEntity.endTick();
        }
    }
}
