package dev.turtywurty.industria.blockentity.util.heat;

import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

public class OutputHeatStorage extends SyncingHeatStorage {
    public OutputHeatStorage(@NotNull BlockEntity blockEntity, long capacity, long maxExtract) {
        super(blockEntity, capacity, 0, maxExtract);
    }
}
