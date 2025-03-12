package dev.turtywurty.industria.blockentity.util.fluid;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.block.entity.BlockEntity;

import java.util.function.Predicate;

public class InputFluidStorage extends PredicateFluidStorage {
    public InputFluidStorage(BlockEntity blockEntity, long capacity, Predicate<FluidVariant> canInsert) {
        super(blockEntity, capacity, canInsert, variant -> false);
    }

    public InputFluidStorage(BlockEntity blockEntity, long capacity) {
        this(blockEntity, capacity, $ -> true);
    }
}
