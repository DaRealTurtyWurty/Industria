package dev.turtywurty.industria.init.worldgen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.init.WoodSetInit;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.YOffset;
import net.minecraft.world.gen.blockpredicate.BlockPredicate;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.PlacedFeatures;
import net.minecraft.world.gen.placementmodifier.*;

import java.util.List;

public class PlacedFeatureInit {
    public static final RegistryKey<PlacedFeature> CRUDE_OIL_POCKET = registerKey("crude_oil_pocket");

    public static final RegistryKey<PlacedFeature> BAUXITE_ORE = registerKey("bauxite_ore");
    public static final RegistryKey<PlacedFeature> CASSITERITE_ORE = registerKey("cassiterite_ore");
    public static final RegistryKey<PlacedFeature> ZINC_ORE = registerKey("zinc_ore");

    public static final RegistryKey<PlacedFeature> RUBBER_TREE = registerKey("rubber_tree");

    public static void bootstrap(Registerable<PlacedFeature> context) {
        RegistryEntryLookup<ConfiguredFeature<?, ?>> registryLookup = context.getRegistryLookup(RegistryKeys.CONFIGURED_FEATURE);

        PlacedFeatures.register(context, CRUDE_OIL_POCKET, registryLookup.getOrThrow(ConfiguredFeatureInit.CRUDE_OIL_POCKET),
                Modifiers.modifiersCount(1,
                        HeightRangePlacementModifier.uniform(YOffset.fixed(0), YOffset.fixed(64))));

        PlacedFeatures.register(context, BAUXITE_ORE, registryLookup.getOrThrow(ConfiguredFeatureInit.BAUXITE_ORE),
                Modifiers.modifiersCount(10,
                        HeightRangePlacementModifier.uniform(YOffset.fixed(32), YOffset.fixed(128))));

        PlacedFeatures.register(context, CASSITERITE_ORE, registryLookup.getOrThrow(ConfiguredFeatureInit.CASSITERITE_ORE),
                Modifiers.modifiersCount(7,
                        HeightRangePlacementModifier.uniform(YOffset.fixed(-16), YOffset.fixed(32))));

        PlacedFeatures.register(context, ZINC_ORE, registryLookup.getOrThrow(ConfiguredFeatureInit.ZINC_ORE),
                Modifiers.modifiersCount(7,
                        HeightRangePlacementModifier.uniform(YOffset.getBottom(), YOffset.fixed(0))));

        PlacedFeatures.register(context, RUBBER_TREE, registryLookup.getOrThrow(ConfiguredFeatureInit.RUBBER_TREE),
                List.of(
                        CountPlacementModifier.of(2),
                        SquarePlacementModifier.of(),
                        HeightmapPlacementModifier.of(Heightmap.Type.OCEAN_FLOOR),
                        BiomePlacementModifier.of(),
                        BlockFilterPlacementModifier.of(
                                BlockPredicate.wouldSurvive(
                                        WoodSetInit.RUBBER.sapling.getDefaultState(),
                                        new Vec3i(0, -1, 0)
                                )
                        ),
                        BlockFilterPlacementModifier.of(BlockPredicate.matchingBlockTag(BlockTags.AIR))
                ));
    }

    private static RegistryKey<PlacedFeature> registerKey(String name) {
        return RegistryKey.of(RegistryKeys.PLACED_FEATURE, Industria.id(name));
    }

    public static class Modifiers {
        public static List<PlacementModifier> modifiers(PlacementModifier countModifier, PlacementModifier heightModifier) {
            return List.of(countModifier, SquarePlacementModifier.of(), heightModifier, BiomePlacementModifier.of());
        }

        public static List<PlacementModifier> modifiersCount(int count, PlacementModifier heightModifier) {
            return modifiers(CountPlacementModifier.of(count), heightModifier);
        }

        public static List<PlacementModifier> modifiersRarity(int chance, PlacementModifier heightModifier) {
            return modifiers(RarityFilterPlacementModifier.of(chance), heightModifier);
        }
    }
}
