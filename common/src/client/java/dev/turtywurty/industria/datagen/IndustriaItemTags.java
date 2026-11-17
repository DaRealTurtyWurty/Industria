package dev.turtywurty.industria.datagen;

import dev.turtywurty.industria.init.ModItems;
import dev.turtywurty.industria.init.list.TagList;
import dev.turtywurty.turtymultiloader.datagen.convention.ConventionTags;
import dev.turtywurty.turtymultiloader.datagen.tag.TagGenerationContext;
import net.minecraft.data.BlockFamilies;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public final class IndustriaItemTags {
    public static void generate(TagGenerationContext<Item> tags) {
        generateCrushableStone(tags);

        tags.tag(ConventionTags.item("ingots"))
            .add(ModItems.STEEL_INGOT.get().builtInRegistryHolder().key());

        tags.tag(TagList.Items.ELECTROLYSIS_RODS)
            .add(ModItems.CARBON_ROD.get().builtInRegistryHolder().key());

        tags.tag(TagList.Items.BLANK_MOLDS)
            .add(ModItems.BLANK_SAND_CAST.get().builtInRegistryHolder().key())
            .add(ModItems.BLANK_RED_SAND_CAST.get().builtInRegistryHolder().key())
            .add(ModItems.BLANK_CLAY_CAST.get().builtInRegistryHolder().key());
    }

    private static void generateCrushableStone(TagGenerationContext<Item> tags) {
        var stone = tags.tag(TagList.Items.CRUSHABLE_STONE)
            .addOptionalTag(ItemTags.STONE_BRICKS);

        List.of(
            BlockFamilies.STONE, BlockFamilies.STONE_BRICK, BlockFamilies.MOSSY_STONE_BRICKS,
            BlockFamilies.COBBLESTONE, BlockFamilies.MOSSY_COBBLESTONE)
            .stream()
            .flatMap(family -> Stream.concat(Stream.of(family.getBaseBlock()), family.getVariants().values().stream()))
            .map(Block::asItem)
            .filter(item -> item != Items.AIR)
            .distinct()
            .map(item -> item.builtInRegistryHolder().key())
            .sorted(Comparator.comparing(key -> key.identifier().toString()))
            .forEach(stone::add);

        List.of(Blocks.SMOOTH_STONE, Blocks.SMOOTH_STONE_SLAB)
            .forEach(block -> stone.add(block.asItem().builtInRegistryHolder().key()));
    }

    private IndustriaItemTags() {
    }
}
