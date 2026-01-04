package dev.turtywurty.industria.blockentity.util.fluid;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.function.Predicate;

public class PredicateFluidStorage extends SyncingFluidStorage {
    private final Predicate<FluidVariant> canInsert;
    private final Predicate<FluidVariant> canExtract;

    public PredicateFluidStorage(BlockEntity blockEntity, long capacity, Predicate<FluidVariant> canInsert, Predicate<FluidVariant> canExtract) {
        super(blockEntity, capacity);
        this.canInsert = canInsert;
        this.canExtract = canExtract;
    }

    @Override
    public boolean canInsert(FluidVariant variant) {
        return this.canInsert.test(variant);
    }

    @Override
    public boolean canExtract(FluidVariant variant) {
        return this.canExtract.test(variant);
    }
}
