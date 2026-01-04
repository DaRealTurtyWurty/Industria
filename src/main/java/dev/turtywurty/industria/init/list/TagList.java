package dev.turtywurty.industria.init.list;

import dev.turtywurty.industria.Industria;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

public class TagList {
    public static class Items {
        public static final TagKey<Item> ELECTROLYSIS_RODS = of("electrolysis_rods");

        public static TagKey<Item> of(String id) {
            return TagKey.create(Registries.ITEM, Industria.id(id));
        }
    }

    public static class Blocks {
        public static final TagKey<Block> BATTERY_BLOCKS = of("battery_blocks");

        public static TagKey<Block> of(String id) {
            return TagKey.create(Registries.BLOCK, Industria.id(id));
        }
    }

    public static class Fluids {
        public static final TagKey<Fluid> CRUDE_OIL = of("crude_oil");
        public static final TagKey<Fluid> DIRTY_SODIUM_ALUMINATE = of("dirty_sodium_aluminate");
        public static final TagKey<Fluid> SODIUM_ALUMINATE = of("sodium_aluminate");

        public static TagKey<Fluid> of(String id) {
            return TagKey.create(Registries.FLUID, Industria.id(id));
        }
    }
}
