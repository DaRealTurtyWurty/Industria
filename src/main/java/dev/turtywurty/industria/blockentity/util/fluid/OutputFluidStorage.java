package dev.turtywurty.industria.blockentity.util.fluid;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.block.entity.BlockEntity;

import java.util.function.Predicate;

public class OutputFluidStorage extends PredicateFluidStorage {
    public OutputFluidStorage(BlockEntity blockEntity, long capacity, Predicate<FluidVariant> canExtract) {
        super(blockEntity, capacity, $ -> false, canExtract);
    }

    public OutputFluidStorage(BlockEntity blockEntity, long capacity) {
        this(blockEntity, capacity, $ -> true);
    }
}
