package dev.turtywurty.industria.datagen;

import dev.turtywurty.industria.init.ItemInit;
import dev.turtywurty.industria.init.list.TagList;
import dev.turtywurty.industria.util.WoodRegistrySet;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.core.HolderLookup;

import java.util.concurrent.CompletableFuture;

public class IndustriaItemTagProvider extends FabricTagsProvider.ItemTagsProvider {
    public IndustriaItemTagProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider wrapperLookup) {
        for (WoodRegistrySet woodSet : WoodRegistrySet.getWoodSets()) {
            WoodSetDatagen.generateItemTags(woodSet, this::valueLookupBuilder);
        }

        valueLookupBuilder(ConventionalItemTags.INGOTS)
                .add(ItemInit.STEEL_INGOT);

        valueLookupBuilder(TagList.Items.ELECTROLYSIS_RODS)
                .add(ItemInit.CARBON_ROD);
    }
}
