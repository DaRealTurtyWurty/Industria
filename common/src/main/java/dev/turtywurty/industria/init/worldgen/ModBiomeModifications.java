package dev.turtywurty.industria.init.worldgen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.turtymultiloader.worldgen.BiomeSelectors;
import dev.turtywurty.turtymultiloader.worldgen.WorldGeneration;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.GenerationStep;

public class ModBiomeModifications {
    public static void init() {
        WorldGeneration.addFeature(
                Industria.id("crude_oil_pocket"),
                BiomeSelectors.includeByKey(Biomes.DESERT),
                GenerationStep.Decoration.FLUID_SPRINGS,
                ModPlacedFeatures.CRUDE_OIL_POCKET
        );

        WorldGeneration.addFeature(
                Industria.id("bauxite_ore"),
                BiomeSelectors.foundInOverworld(),
                GenerationStep.Decoration.UNDERGROUND_ORES,
                ModPlacedFeatures.BAUXITE_ORE
        );

        WorldGeneration.addFeature(
                Industria.id("cassiterite_ore"),
                BiomeSelectors.foundInOverworld(),
                GenerationStep.Decoration.UNDERGROUND_ORES,
                ModPlacedFeatures.CASSITERITE_ORE
        );

        WorldGeneration.addFeature(
                Industria.id("zinc_ore"),
                BiomeSelectors.foundInOverworld(),
                GenerationStep.Decoration.UNDERGROUND_ORES,
                ModPlacedFeatures.ZINC_ORE
        );

        WorldGeneration.addFeature(
                Industria.id("rubber_tree"),
                BiomeSelectors.foundInOverworld().and(BiomeSelectors.includeByKey(Biomes.JUNGLE)),
                GenerationStep.Decoration.VEGETAL_DECORATION,
                ModPlacedFeatures.RUBBER_TREE
        );
    }
}
