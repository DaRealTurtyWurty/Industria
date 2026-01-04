package dev.turtywurty.industria.init.worldgen;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.GenerationStep;

public class BiomeModificationInit {
    public static void init() {
        BiomeModifications.addFeature(BiomeSelectors.includeByKey(Biomes.DESERT),
                GenerationStep.Decoration.FLUID_SPRINGS,
                PlacedFeatureInit.CRUDE_OIL_POCKET);

        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(),
                GenerationStep.Decoration.UNDERGROUND_ORES,
                PlacedFeatureInit.BAUXITE_ORE);

        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(),
                GenerationStep.Decoration.UNDERGROUND_ORES,
                PlacedFeatureInit.CASSITERITE_ORE);

        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(),
                GenerationStep.Decoration.UNDERGROUND_ORES,
                PlacedFeatureInit.ZINC_ORE);

        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld().and(BiomeSelectors.includeByKey(Biomes.JUNGLE)),
                GenerationStep.Decoration.VEGETAL_DECORATION,
                PlacedFeatureInit.RUBBER_TREE);
    }
}
