package dev.turtywurty.industria.blockentity.util.gas;

import dev.turtywurty.gasapi.api.GasVariant;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.function.Predicate;

public class InputGasStorage extends PredicateGasStorage {
    public InputGasStorage(BlockEntity blockEntity, long capacity, Predicate<GasVariant> canInsert) {
        super(blockEntity, capacity, canInsert, $ -> false);
    }

    public InputGasStorage(BlockEntity blockEntity, long capacity) {
        this(blockEntity, capacity, $ -> true);
    }
}
