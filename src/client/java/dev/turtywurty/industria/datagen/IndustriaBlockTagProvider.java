package dev.turtywurty.industria.datagen;

import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.list.TagList;
import dev.turtywurty.industria.util.WoodRegistrySet;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;

import java.util.concurrent.CompletableFuture;

public class IndustriaBlockTagProvider extends FabricTagsProvider.BlockTagsProvider {
    public IndustriaBlockTagProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider wrapperLookup) {
        for (WoodRegistrySet woodSet : WoodRegistrySet.getWoodSets()) {
            WoodSetDatagen.generateBlockTags(woodSet, this::valueLookupBuilder);
        }

        valueLookupBuilder(TagList.Blocks.BATTERY_BLOCKS)
                .add(BlockInit.BASIC_BATTERY)
                .add(BlockInit.ADVANCED_BATTERY)
                .add(BlockInit.ELITE_BATTERY)
                .add(BlockInit.ULTIMATE_BATTERY)
                .add(BlockInit.CREATIVE_BATTERY);

        valueLookupBuilder(TagList.Blocks.CONVEYORS)
                .add(BlockInit.CONVEYOR)
                .add(BlockInit.SPLITTER_CONVEYOR)
                .add(BlockInit.MERGER_CONVEYOR)
                .add(BlockInit.FEEDER_CONVEYOR)
                .add(BlockInit.HATCH_CONVEYOR)
                .add(BlockInit.SIDE_INJECTOR_CONVEYOR)
                .add(BlockInit.LADDER_CONVEYOR)
                .add(BlockInit.FILTER_CONVEYOR)
                .add(BlockInit.MAGNETIC_CONVEYOR)
                .add(BlockInit.DETECTOR_CONVEYOR)
                .add(BlockInit.DROP_CHUTE_CONVEYOR)
                .add(BlockInit.COUNT_CONVEYOR)
                .add(BlockInit.DELAY_CONVEYOR);
    }
}
