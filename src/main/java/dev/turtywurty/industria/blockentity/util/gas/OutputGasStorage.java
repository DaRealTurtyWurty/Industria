package dev.turtywurty.industria.blockentity.util.gas;

import dev.turtywurty.gasapi.api.GasVariant;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.function.Predicate;

public class OutputGasStorage extends PredicateGasStorage {
    public OutputGasStorage(BlockEntity blockEntity, long capacity, Predicate<GasVariant> canExtract) {
        super(blockEntity, capacity, $ -> false, canExtract);
    }

    public OutputGasStorage(BlockEntity blockEntity, long capacity) {
        this(blockEntity, capacity, $ -> true);
    }
}
