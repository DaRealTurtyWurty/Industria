package dev.turtywurty.industria.fluid;

import net.minecraft.block.Block;
import net.minecraft.block.FluidBlock;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;

import java.util.function.Supplier;

public record FluidRegistryObject<S extends IndustriaFluid, F extends IndustriaFluid>(
        S still, F flowing, BucketItem bucket, FluidBlock block) {

    @FunctionalInterface
    public interface IndustriaFluidFactory<F extends IndustriaFluid> {
        F create(Supplier<Fluid> stillSupplier, Supplier<Fluid> flowingSupplier, Supplier<Item> bucketSupplier, Supplier<Block> blockSupplier);
    }
}
