package dev.turtywurty.industria.blockentity.abstraction.component;

import dev.turtywurty.industria.blockentity.abstraction.IndustriaBlockEntity;

public class SyncingComponent implements Component {
    private final IndustriaBlockEntity<?> blockEntity;
    private boolean isDirty = false;

    public SyncingComponent(IndustriaBlockEntity<?> blockEntity) {
        this.blockEntity = blockEntity;
    }

    public void sync() {
        if (this.isDirty && this.blockEntity != null && this.blockEntity.hasWorld() && !this.blockEntity.getWorld().isClient) {
            this.isDirty = false;

            this.blockEntity.update();
        }
    }
}
