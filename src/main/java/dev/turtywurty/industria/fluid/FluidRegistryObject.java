package dev.turtywurty.industria.fluid;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.Fluid;

import java.util.function.Supplier;

public record FluidRegistryObject<S extends IndustriaFluid, F extends IndustriaFluid>(
        S still, F flowing, BucketItem bucket, LiquidBlock block) {

    @FunctionalInterface
    public interface IndustriaFluidFactory<F extends IndustriaFluid> {
        F create(Supplier<Fluid> stillSupplier, Supplier<Fluid> flowingSupplier, Supplier<Item> bucketSupplier, Supplier<Block> blockSupplier);
    }
}
