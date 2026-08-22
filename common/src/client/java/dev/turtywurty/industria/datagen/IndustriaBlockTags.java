package dev.turtywurty.industria.datagen;

import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.init.list.TagList;
import dev.turtywurty.turtymultiloader.datagen.tag.TagGenerationContext;
import net.minecraft.world.level.block.Block;

public final class IndustriaBlockTags {
    public static void generate(TagGenerationContext<Block> tags) {
        tags.tag(TagList.Blocks.BATTERY_BLOCKS)
                .add(ModBlocks.BASIC_BATTERY.get().builtInRegistryHolder().key())
                .add(ModBlocks.ADVANCED_BATTERY.get().builtInRegistryHolder().key())
                .add(ModBlocks.ELITE_BATTERY.get().builtInRegistryHolder().key())
                .add(ModBlocks.ULTIMATE_BATTERY.get().builtInRegistryHolder().key())
                .add(ModBlocks.CREATIVE_BATTERY.get().builtInRegistryHolder().key());

        tags.tag(TagList.Blocks.CONVEYORS)
                .add(ModBlocks.CONVEYOR.get().builtInRegistryHolder().key())
                .add(ModBlocks.SPLITTER_CONVEYOR.get().builtInRegistryHolder().key())
                .add(ModBlocks.MERGER_CONVEYOR.get().builtInRegistryHolder().key())
                .add(ModBlocks.ALTERNATOR_CONVEYOR.get().builtInRegistryHolder().key())
                .add(ModBlocks.FEEDER_CONVEYOR.get().builtInRegistryHolder().key())
                .add(ModBlocks.HATCH_CONVEYOR.get().builtInRegistryHolder().key())
                .add(ModBlocks.SIDE_INJECTOR_CONVEYOR.get().builtInRegistryHolder().key())
                .add(ModBlocks.LADDER_CONVEYOR.get().builtInRegistryHolder().key())
                .add(ModBlocks.FILTER_CONVEYOR.get().builtInRegistryHolder().key())
                .add(ModBlocks.MAGNETIC_CONVEYOR.get().builtInRegistryHolder().key())
                .add(ModBlocks.DETECTOR_CONVEYOR.get().builtInRegistryHolder().key())
                .add(ModBlocks.DROP_CHUTE_CONVEYOR.get().builtInRegistryHolder().key())
                .add(ModBlocks.COUNT_CONVEYOR.get().builtInRegistryHolder().key())
                .add(ModBlocks.DELAY_CONVEYOR.get().builtInRegistryHolder().key())
                .add(ModBlocks.CONTAINMENT_CONVEYOR.get().builtInRegistryHolder().key());
    }

    private IndustriaBlockTags() {
    }
}
