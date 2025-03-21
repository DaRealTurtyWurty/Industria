package dev.turtywurty.industria.init.worldgen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.FluidInit;
import dev.turtywurty.industria.worldgen.config.FluidPocketConfig;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.structure.rule.BlockMatchRuleTest;
import net.minecraft.structure.rule.RuleTest;
import net.minecraft.structure.rule.TagMatchRuleTest;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.gen.feature.*;

import java.util.List;

public class ConfiguredFeatureInit {
    public static final RegistryKey<ConfiguredFeature<?, ?>> CRUDE_OIL_POCKET = registerKey("crude_oil_pocket");

    public static final RegistryKey<ConfiguredFeature<?, ?>> BAUXITE_ORE = registerKey("bauxite_ore");
    public static final RegistryKey<ConfiguredFeature<?, ?>> TIN_ORE = registerKey("tin_ore");
    public static final RegistryKey<ConfiguredFeature<?, ?>> ZINC_ORE = registerKey("zinc_ore");

    public static void bootstrap(Registerable<ConfiguredFeature<?, ?>> context) {
        RuleTest stoneOreReplaceables = new TagMatchRuleTest(BlockTags.STONE_ORE_REPLACEABLES);
        RuleTest deepslateOreReplaceables = new TagMatchRuleTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);
        RuleTest netherOreReplaceables = new TagMatchRuleTest(BlockTags.BASE_STONE_NETHER);
        RuleTest endOreReplaceables = new BlockMatchRuleTest(Blocks.END_STONE);

        RegistryEntryLookup<PlacedFeature> registryLookup = context.getRegistryLookup(RegistryKeys.PLACED_FEATURE);

        register(context, CRUDE_OIL_POCKET, FeatureInit.FLUID_POCKET,
                new FluidPocketConfig(FluidInit.CRUDE_OIL.still().getDefaultState(),
                        UniformIntProvider.create(4, 6),
                        UniformIntProvider.create(2, 4),
                        stoneOreReplaceables));

        List<OreFeatureConfig.Target> bauxiteTargets = List.of(
                OreFeatureConfig.createTarget(stoneOreReplaceables, BlockInit.BAUXITE_ORE.getDefaultState()),
                OreFeatureConfig.createTarget(deepslateOreReplaceables, BlockInit.DEEPSLATE_BAUXITE_ORE.getDefaultState()));

        List<OreFeatureConfig.Target> tinTargets = List.of(
                OreFeatureConfig.createTarget(stoneOreReplaceables, BlockInit.TIN_ORE.getDefaultState()),
                OreFeatureConfig.createTarget(deepslateOreReplaceables, BlockInit.DEEPSLATE_TIN_ORE.getDefaultState()));

        List<OreFeatureConfig.Target> zincTargets = List.of(
                OreFeatureConfig.createTarget(stoneOreReplaceables, BlockInit.ZINC_ORE.getDefaultState()),
                OreFeatureConfig.createTarget(deepslateOreReplaceables, BlockInit.DEEPSLATE_ZINC_ORE.getDefaultState()));

        register(context, BAUXITE_ORE, Feature.ORE, new OreFeatureConfig(bauxiteTargets, 8, 0.25f));
        register(context, TIN_ORE, Feature.ORE, new OreFeatureConfig(tinTargets, 14));
        register(context, ZINC_ORE, Feature.SCATTERED_ORE, new OreFeatureConfig(zincTargets, 10));
    }

    private static RegistryKey<ConfiguredFeature<?, ?>> registerKey(String name) {
        return RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE, Industria.id(name));
    }

    private static <FC extends FeatureConfig, F extends Feature<FC>> void register(Registerable<ConfiguredFeature<?, ?>> context,
                                                                                   RegistryKey<ConfiguredFeature<?, ?>> key,
                                                                                   F feature,
                                                                                   FC featureConfig) {
        context.register(key, new ConfiguredFeature<>(feature, featureConfig));
    }
}
