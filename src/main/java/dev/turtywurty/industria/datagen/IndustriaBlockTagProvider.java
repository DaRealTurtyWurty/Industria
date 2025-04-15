package dev.turtywurty.industria.datagen;

import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.list.TagList;
import dev.turtywurty.industria.util.WoodRegistrySet;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class IndustriaBlockTagProvider extends FabricTagProvider.BlockTagProvider {
    public IndustriaBlockTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        for (WoodRegistrySet woodSet : WoodRegistrySet.getWoodSets()) {
            woodSet.generateBlockTags(this);
        }

        getOrCreateTagBuilder(TagList.Blocks.BATTERY_BLOCKS)
                .add(BlockInit.BASIC_BATTERY)
                .add(BlockInit.ADVANCED_BATTERY)
                .add(BlockInit.ELITE_BATTERY)
                .add(BlockInit.ULTIMATE_BATTERY)
                .add(BlockInit.CREATIVE_BATTERY);
    }
}
