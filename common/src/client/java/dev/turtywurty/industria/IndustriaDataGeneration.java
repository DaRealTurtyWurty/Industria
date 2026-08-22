package dev.turtywurty.industria;

import dev.turtywurty.industria.datagen.IndustriaBlockLootTables;
import dev.turtywurty.industria.datagen.IndustriaBlockTags;
import dev.turtywurty.industria.datagen.IndustriaEnglishLanguage;
import dev.turtywurty.industria.datagen.IndustriaEntityTypeTags;
import dev.turtywurty.industria.datagen.IndustriaFluidTags;
import dev.turtywurty.industria.datagen.IndustriaItemTags;
import dev.turtywurty.industria.datagen.IndustriaModels;
import dev.turtywurty.industria.datagen.IndustriaRecipes;
import dev.turtywurty.industria.init.ModDamageTypes;
import dev.turtywurty.industria.init.ModWoodSets;
import dev.turtywurty.industria.init.worldgen.ModConfiguredFeatures;
import dev.turtywurty.industria.init.worldgen.ModPlacedFeatures;
import dev.turtywurty.turtymultiloader.datagen.DataGeneration;
import dev.turtywurty.turtymultiloader.datagen.DataGenerationSpec;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.Set;

public final class IndustriaDataGeneration {
    public static final DataGenerationSpec SPEC = DataGeneration.spec(Industria.MOD_ID)
            .woodSet(ModWoodSets.RUBBER)
            .recipes(IndustriaRecipes::generate)
            .lootTables(Set.of(), List.of(new LootTableProvider.SubProviderEntry(
                    IndustriaBlockLootTables::new,
                    LootContextParamSets.BLOCK)))
            .blockTags((registries, tags) -> IndustriaBlockTags.generate(tags))
            .itemTags((registries, tags) -> IndustriaItemTags.generate(tags))
            .fluidTags((registries, tags) -> IndustriaFluidTags.generate(tags))
            .entityTypeTags((registries, tags) -> IndustriaEntityTypeTags.generate(tags))
            .language("en_us", IndustriaEnglishLanguage::generate)
            .vanillaModels(IndustriaModels::generate)
            .damageTypes(ModDamageTypes::bootstrap)
            .worldGeneration(Registries.CONFIGURED_FEATURE, ModConfiguredFeatures::bootstrap)
            .worldGeneration(Registries.PLACED_FEATURE, ModPlacedFeatures::bootstrap)
            .build();

    private IndustriaDataGeneration() {
    }
}
