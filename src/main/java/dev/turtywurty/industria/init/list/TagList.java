package dev.turtywurty.industria.init.list;

import dev.turtywurty.industria.Industria;
import net.fabricmc.fabric.impl.tag.convention.v2.TagRegistration;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

@SuppressWarnings("UnstableApiUsage")
public class TagList {
    public static class Items {
        public static final TagKey<Item> STEEL_INGOTS = TagRegistration.ITEM_TAG.registerC("ingots/steel");
    }

    public static class Blocks {
        public static final TagKey<Block> BATTERY_BLOCKS = TagKey.of(RegistryKeys.BLOCK, Industria.id("battery_blocks"));
    }
}