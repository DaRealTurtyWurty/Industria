package dev.turtywurty.industria.init.worldgen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.FluidInit;
import dev.turtywurty.industria.init.WoodSetInit;
import dev.turtywurty.industria.worldgen.config.FluidPocketConfig;
import dev.turtywurty.industria.worldgen.trunkplacer.RubberTreeTrunkPlacer;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.SimpleStateProvider;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

import java.util.List;

public class ConfiguredFeatureInit {
    public static final ResourceKey<ConfiguredFeature<?, ?>> CRUDE_OIL_POCKET = registerKey("crude_oil_pocket");

    public static final ResourceKey<ConfiguredFeature<?, ?>> BAUXITE_ORE = registerKey("bauxite_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> CASSITERITE_ORE = registerKey("cassiterite_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> ZINC_ORE = registerKey("zinc_ore");

    public static final ResourceKey<ConfiguredFeature<?, ?>> RUBBER_TREE = registerKey("rubber_tree");

    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        RuleTest stoneOreReplaceables = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);
        RuleTest deepslateOreReplaceables = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);
        RuleTest netherOreReplaceables = new TagMatchTest(BlockTags.BASE_STONE_NETHER);
        RuleTest endOreReplaceables = new BlockMatchTest(Blocks.END_STONE);

        HolderGetter<PlacedFeature> registryLookup = context.lookup(Registries.PLACED_FEATURE);

        register(context, CRUDE_OIL_POCKET, FeatureInit.FLUID_POCKET,
                new FluidPocketConfig(FluidInit.CRUDE_OIL.still().defaultFluidState(),
                        UniformInt.of(4, 6),
                        UniformInt.of(2, 4),
                        stoneOreReplaceables));

        List<OreConfiguration.TargetBlockState> bauxiteTargets = List.of(
                OreConfiguration.target(stoneOreReplaceables, BlockInit.BAUXITE_ORE.defaultBlockState()),
                OreConfiguration.target(deepslateOreReplaceables, BlockInit.DEEPSLATE_BAUXITE_ORE.defaultBlockState()));

        List<OreConfiguration.TargetBlockState> tinTargets = List.of(
                OreConfiguration.target(stoneOreReplaceables, BlockInit.CASSITERITE_ORE.defaultBlockState()),
                OreConfiguration.target(deepslateOreReplaceables, BlockInit.DEEPSLATE_CASSITERITE_ORE.defaultBlockState()));

        List<OreConfiguration.TargetBlockState> zincTargets = List.of(
                OreConfiguration.target(stoneOreReplaceables, BlockInit.SPHALERITE_ORE.defaultBlockState()),
                OreConfiguration.target(deepslateOreReplaceables, BlockInit.DEEPSLATE_SPHALERITE_ORE.defaultBlockState()));

        register(context, BAUXITE_ORE, Feature.ORE, new OreConfiguration(bauxiteTargets, 8, 0.25f));
        register(context, CASSITERITE_ORE, Feature.ORE, new OreConfiguration(tinTargets, 14));
        register(context, ZINC_ORE, Feature.SCATTERED_ORE, new OreConfiguration(zincTargets, 10));

        register(context, RUBBER_TREE, Feature.TREE,
                new TreeConfiguration.TreeConfigurationBuilder(
                        SimpleStateProvider.simple(WoodSetInit.RUBBER.log),
                        new RubberTreeTrunkPlacer(15, 2, 1, UniformInt.of(10, 13), UniformInt.of(3, 5)),
                        SimpleStateProvider.simple(WoodSetInit.RUBBER.leaves),
                        new BlobFoliagePlacer(UniformInt.of(2, 4), ConstantInt.of(2), 4),
                        new TwoLayersFeatureSize(1, 0, 1))
                        .ignoreVines()
                        .dirt(SimpleStateProvider.simple(Blocks.DIRT))
                        .build());
    }

    private static ResourceKey<ConfiguredFeature<?, ?>> registerKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, Industria.id(name));
    }

    private static <FC extends FeatureConfiguration, F extends Feature<FC>> void register(BootstrapContext<ConfiguredFeature<?, ?>> context,
                                                                                          ResourceKey<ConfiguredFeature<?, ?>> key,
                                                                                          F feature,
                                                                                          FC featureConfig) {
        context.register(key, new ConfiguredFeature<>(feature, featureConfig));
    }
}
