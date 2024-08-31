package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.fluid.CrudeOilFluid;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class FluidInit {
    public static final CrudeOilFluid.Still CRUDE_OIL = register("crude_oil",
            new CrudeOilFluid.Still());
    public static final CrudeOilFluid.Flowing CRUDE_OIL_FLOWING = register("flowing_crude_oil",
            new CrudeOilFluid.Flowing());

    public static final BucketItem CRUDE_OIL_BUCKET = ItemInit.register("crude_oil_bucket",
            new BucketItem(CRUDE_OIL, new Item.Settings().maxCount(1).recipeRemainder(Items.BUCKET)));

    public static final FluidBlock CRUDE_OIL_BLOCK = BlockInit.register("crude_oil",
            new FluidBlock(CRUDE_OIL, AbstractBlock.Settings.copy(Blocks.WATER)));

    public static <T extends Fluid> T register(String name, T fluid) {
        return Registry.register(Registries.FLUID, Industria.id(name), fluid);
    }

    public static void init() {}
}
