package dev.turtywurty.industria.datagen;

import dev.turtywurty.industria.init.ItemInit;
import dev.turtywurty.industria.init.list.TagList;
import dev.turtywurty.industria.util.WoodRegistrySet;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class IndustriaItemTagProvider extends FabricTagProvider.ItemTagProvider {
    public IndustriaItemTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        for (WoodRegistrySet woodSet : WoodRegistrySet.getWoodSets()) {
            woodSet.generateItemTags(this);
        }

        getOrCreateTagBuilder(TagList.Items.STEEL_INGOTS)
                .add(ItemInit.STEEL_INGOT);

        getOrCreateTagBuilder(ConventionalItemTags.INGOTS)
                .addTag(TagList.Items.STEEL_INGOTS);
    }
}
