package dev.turtywurty.industria.init.worldgen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.init.WoodSetInit;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.*;

import java.util.List;

public class PlacedFeatureInit {
    public static final ResourceKey<PlacedFeature> CRUDE_OIL_POCKET = registerKey("crude_oil_pocket");

    public static final ResourceKey<PlacedFeature> BAUXITE_ORE = registerKey("bauxite_ore");
    public static final ResourceKey<PlacedFeature> CASSITERITE_ORE = registerKey("cassiterite_ore");
    public static final ResourceKey<PlacedFeature> ZINC_ORE = registerKey("zinc_ore");

    public static final ResourceKey<PlacedFeature> RUBBER_TREE = registerKey("rubber_tree");

    public static void bootstrap(BootstrapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> registryLookup = context.lookup(Registries.CONFIGURED_FEATURE);

        register(context, CRUDE_OIL_POCKET, registryLookup.getOrThrow(ConfiguredFeatureInit.CRUDE_OIL_POCKET),
                Modifiers.modifiersCount(1,
                        HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(64))));

        register(context, BAUXITE_ORE, registryLookup.getOrThrow(ConfiguredFeatureInit.BAUXITE_ORE),
                Modifiers.modifiersCount(10,
                        HeightRangePlacement.uniform(VerticalAnchor.absolute(32), VerticalAnchor.absolute(128))));

        register(context, CASSITERITE_ORE, registryLookup.getOrThrow(ConfiguredFeatureInit.CASSITERITE_ORE),
                Modifiers.modifiersCount(7,
                        HeightRangePlacement.uniform(VerticalAnchor.absolute(-16), VerticalAnchor.absolute(32))));

        register(context, ZINC_ORE, registryLookup.getOrThrow(ConfiguredFeatureInit.ZINC_ORE),
                Modifiers.modifiersCount(7,
                        HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(0))));

        register(context, RUBBER_TREE, registryLookup.getOrThrow(ConfiguredFeatureInit.RUBBER_TREE),
                List.of(
                        CountPlacement.of(2),
                        InSquarePlacement.spread(),
                        HeightmapPlacement.onHeightmap(Heightmap.Types.OCEAN_FLOOR),
                        BiomeFilter.biome(),
                        BlockPredicateFilter.forPredicate(
                                BlockPredicate.wouldSurvive(
                                        WoodSetInit.RUBBER.sapling.defaultBlockState(),
                                        new Vec3i(0, -1, 0)
                                )
                        ),
                        BlockPredicateFilter.forPredicate(BlockPredicate.matchesTag(BlockTags.AIR))
                ));
    }

    private static ResourceKey<PlacedFeature> registerKey(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, Industria.id(name));
    }

    private static void register(BootstrapContext<PlacedFeature> context,
                                 ResourceKey<PlacedFeature> key,
                                 Holder<ConfiguredFeature<?, ?>> config,
                                 List<PlacementModifier> modifiers) {
        context.register(key, new PlacedFeature(config, List.copyOf(modifiers)));
    }

    public static class Modifiers {
        public static List<PlacementModifier> modifiers(PlacementModifier countModifier, PlacementModifier heightModifier) {
            return List.of(countModifier, InSquarePlacement.spread(), heightModifier, BiomeFilter.biome());
        }

        public static List<PlacementModifier> modifiersCount(int count, PlacementModifier heightModifier) {
            return modifiers(CountPlacement.of(count), heightModifier);
        }

        public static List<PlacementModifier> modifiersRarity(int chance, PlacementModifier heightModifier) {
            return modifiers(RarityFilter.onAverageOnceEvery(chance), heightModifier);
        }
    }
}
