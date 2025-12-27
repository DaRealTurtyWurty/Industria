package dev.turtywurty.industria.init.list;

import dev.turtywurty.industria.Industria;
import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public class TagList {
    public static class Items {
        public static final TagKey<Item> ELECTROLYSIS_RODS = of("electrolysis_rods");

        public static TagKey<Item> of(String id) {
            return TagKey.of(RegistryKeys.ITEM, Industria.id(id));
        }
    }

    public static class Blocks {
        public static final TagKey<Block> BATTERY_BLOCKS = of("battery_blocks");

        public static TagKey<Block> of(String id) {
            return TagKey.of(RegistryKeys.BLOCK, Industria.id(id));
        }
    }

    public static class Fluids {
        public static final TagKey<Fluid> CRUDE_OIL = of("crude_oil");
        public static final TagKey<Fluid> DIRTY_SODIUM_ALUMINATE = of("dirty_sodium_aluminate");
        public static final TagKey<Fluid> SODIUM_ALUMINATE = of("sodium_aluminate");

        public static TagKey<Fluid> of(String id) {
            return TagKey.of(RegistryKeys.FLUID, Industria.id(id));
        }
    }
}
