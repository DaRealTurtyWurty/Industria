package dev.turtywurty.industria.fluid;

import net.minecraft.block.FluidBlock;
import net.minecraft.item.BucketItem;

public record FluidRegistryObject(IndustriaFluid.Still still, IndustriaFluid.Flowing flowing, BucketItem bucket,
                                  FluidBlock block) {
}
