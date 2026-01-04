package dev.turtywurty.industria.datagen;

import dev.turtywurty.industria.util.WoodRegistrySet;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;

import java.util.concurrent.CompletableFuture;

public class IndustriaEntityTypeTagProvider extends FabricTagsProvider.EntityTypeTagsProvider {
    public IndustriaEntityTypeTagProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider wrapperLookup) {
        for (WoodRegistrySet woodSet : WoodRegistrySet.getWoodSets()) {
            WoodSetDatagen.generateEntityTags(woodSet, this::valueLookupBuilder);
        }
    }
}
