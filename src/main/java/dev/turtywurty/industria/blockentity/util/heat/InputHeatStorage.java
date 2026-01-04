package dev.turtywurty.industria.blockentity.util.heat;

import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

public class InputHeatStorage extends SyncingHeatStorage {
    public InputHeatStorage(@NotNull BlockEntity blockEntity, long capacity, long maxInsert) {
        super(blockEntity, capacity, maxInsert, 0);
    }
}
