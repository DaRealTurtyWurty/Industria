package dev.turtywurty.industria.init.worldgen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.init.ModFluids;
import dev.turtywurty.industria.init.ModWoodSets;
import dev.turtywurty.industria.worldgen.config.FluidPocketConfig;
import dev.turtywurty.industria.worldgen.feature.FluidPocketFeature;
import dev.turtywurty.industria.worldgen.trunkplacer.RubberTreeTrunkPlacer;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.BlockReplacement;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.OreFeature;
import net.minecraft.world.level.levelgen.feature.ScatteredOreFeature;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.RuleBasedStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.SimpleStateProvider;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

public class ModConfiguredFeatures {
    public static final ResourceKey<Feature> CRUDE_OIL_POCKET = registerKey("crude_oil_pocket");

    public static final ResourceKey<Feature> BAUXITE_ORE = registerKey("bauxite_ore");
    public static final ResourceKey<Feature> CASSITERITE_ORE = registerKey("cassiterite_ore");
    public static final ResourceKey<Feature> ZINC_ORE = registerKey("zinc_ore");

    public static final ResourceKey<Feature> RUBBER_TREE = registerKey("rubber_tree");

    public static void bootstrap(BootstrapContext<Feature> context) {
        RuleTest stoneOreReplaceables = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);
        RuleTest deepslateOreReplaceables = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);
        RuleTest netherOreReplaceables = new TagMatchTest(BlockTags.BASE_STONE_NETHER);
        RuleTest endOreReplaceables = new BlockMatchTest(Blocks.END_STONE);

        HolderGetter<PlacedFeature> registryLookup = context.lookup(Registries.PLACED_FEATURE);

        context.register(CRUDE_OIL_POCKET, new FluidPocketFeature(new FluidPocketConfig(ModFluids.CRUDE_OIL.still().get().defaultFluidState(),
                        UniformInt.of(4, 6),
                        UniformInt.of(2, 4),
                        stoneOreReplaceables)));

        List<BlockReplacement> bauxiteTargets = List.of(
                BlockReplacement.replace(stoneOreReplaceables, ModBlocks.BAUXITE_ORE.get().defaultBlockState()),
                BlockReplacement.replace(deepslateOreReplaceables, ModBlocks.BAUXITE_ORE.get().defaultBlockState()));

        List<BlockReplacement> tinTargets = List.of(
                BlockReplacement.replace(stoneOreReplaceables, ModBlocks.CASSITERITE_ORE.get().defaultBlockState()),
                BlockReplacement.replace(deepslateOreReplaceables, ModBlocks.DEEPSLATE_CASSITERITE_ORE.get().defaultBlockState()));

        List<BlockReplacement> zincTargets = List.of(
                BlockReplacement.replace(stoneOreReplaceables, ModBlocks.SPHALERITE_ORE.get().defaultBlockState()),
                BlockReplacement.replace(deepslateOreReplaceables, ModBlocks.DEEPSLATE_SPHALERITE_ORE.get().defaultBlockState()));

        context.register(BAUXITE_ORE, new OreFeature(bauxiteTargets, 8, 0.25f));
        context.register(CASSITERITE_ORE, new OreFeature(tinTargets, 14));
        context.register(ZINC_ORE, new ScatteredOreFeature(zincTargets, 10, 0.0f));

        context.register(RUBBER_TREE, new TreeFeature.Builder(
                        BlockStateProvider.of(ModWoodSets.RUBBER.log().get()),
                        new RubberTreeTrunkPlacer(15, 2, 1, UniformInt.of(10, 13), UniformInt.of(3, 5)),
                        BlockStateProvider.of(ModWoodSets.RUBBER.leaves().get()),
                        new BlobFoliagePlacer(UniformInt.of(2, 4), ConstantInt.of(2), 4),
                        new TwoLayersFeatureSize(1, 0, 1), Holder.direct(BlockStateProvider.of(Blocks.DIRT)))
                        .ignoreVines()
                        .build());
    }

    private static ResourceKey<Feature> registerKey(String name) {
        return ResourceKey.create(Registries.FEATURE, Industria.id(name));
    }


}
