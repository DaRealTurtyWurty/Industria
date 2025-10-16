package dev.turtywurty.industria.init.worldgen;

import dev.turtywurty.industria.Industria;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.math.VerticalSurfaceType;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.gen.YOffset;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.GenerationShapeConfig;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import net.minecraft.world.gen.densityfunction.DensityFunctions;
import net.minecraft.world.gen.noise.NoiseParametersKeys;
import net.minecraft.world.gen.noise.NoiseRouter;
import net.minecraft.world.gen.surfacebuilder.MaterialRules;

import java.util.List;

public class ChunkGeneratorSettingsInit {
    public static final RegistryKey<ChunkGeneratorSettings> THE_LUMEN_DEPTHS = registerKey("the_lumen_depths");
    public static final RegistryKey<DensityFunction> THE_LUMEN_DEPTHS_DENSITY = RegistryKey.of(RegistryKeys.DENSITY_FUNCTION, Industria.id("the_lumen_depths"));

    private static RegistryKey<ChunkGeneratorSettings> registerKey(String name) {
        return RegistryKey.of(RegistryKeys.CHUNK_GENERATOR_SETTINGS, Industria.id(name));
    }

    public static void bootstrap(Registerable<ChunkGeneratorSettings> context) {
        RegistryEntryLookup<DensityFunction> densityFunctionLookup = context.getRegistryLookup(RegistryKeys.DENSITY_FUNCTION);
        RegistryEntryLookup<DoublePerlinNoiseSampler.NoiseParameters> noiseParametersLookup = context.getRegistryLookup(RegistryKeys.NOISE_PARAMETERS);


        context.register(THE_LUMEN_DEPTHS, createChunkGeneratorSettings(
                new GenerationShapeConfig(
                        -64,
                        320,
                        1,
                        1
                ),
                Blocks.CYAN_WOOL.getDefaultState(),
                Blocks.WATER.getDefaultState(),
                new NoiseRouter(
                        DensityFunctionTypes.zero(),
                        DensityFunctionTypes.zero(),
                        DensityFunctionTypes.zero(),
                        DensityFunctionTypes.zero(),
                        DensityFunctionTypes.shiftedNoise(
                                fromKey(densityFunctionLookup, DensityFunctions.SHIFT_X),
                                fromKey(densityFunctionLookup, DensityFunctions.SHIFT_Z),
                                0.25,
                                noiseParametersLookup.getOrThrow(NoiseParametersKeys.TEMPERATURE)
                        ),
                        DensityFunctionTypes.shiftedNoise(
                                fromKey(densityFunctionLookup, DensityFunctions.SHIFT_X),
                                fromKey(densityFunctionLookup, DensityFunctions.SHIFT_Z),
                                0.25,
                                noiseParametersLookup.getOrThrow(NoiseParametersKeys.VEGETATION)
                        ),
                        DensityFunctionTypes.zero(),
                        DensityFunctionTypes.zero(),
                        DensityFunctionTypes.zero(),
                        DensityFunctionTypes.zero(),
                        DensityFunctionTypes.zero(),
                        fromKey(densityFunctionLookup, THE_LUMEN_DEPTHS_DENSITY),
                        DensityFunctionTypes.zero(),
                        DensityFunctionTypes.zero(),
                        DensityFunctionTypes.zero()

                ),
                MaterialRules.sequence(
                        MaterialRules.condition(
                                MaterialRules.not(
                                        MaterialRules.verticalGradient(
                                                "minecraft:bedrock_roof",
                                                YOffset.belowTop(5),
                                                YOffset.TOP
                                        )
                                ),
                                MaterialRules.block(Blocks.BEDROCK.getDefaultState())
                        ),
                        MaterialRules.condition(
                                MaterialRules.verticalGradient(
                                        "minecraft:bedrock_floor",
                                        YOffset.BOTTOM,
                                        YOffset.aboveBottom(5)
                                ),
                                MaterialRules.block(Blocks.BEDROCK.getDefaultState())
                        ),
                        MaterialRules.sequence(
                                MaterialRules.condition(
                                        MaterialRules.stoneDepth(
                                                0,
                                                false,
                                                0,
                                                VerticalSurfaceType.FLOOR
                                        ),
                                        MaterialRules.sequence(
                                                MaterialRules.sequence(
                                                        MaterialRules.condition(
                                                                MaterialRules.stoneDepth(
                                                                        0,
                                                                        false,
                                                                        0,
                                                                        VerticalSurfaceType.CEILING
                                                                ),
                                                                MaterialRules.block(Blocks.CYAN_WOOL.getDefaultState())
                                                        ),
                                                        MaterialRules.block(Blocks.BLUE_WOOL.getDefaultState())
                                                )
                                        )
                                )
                        )
                ),
                List.of(),
                -64,
                false,
                false,
                false,
                false
        ));
    }

    private static ChunkGeneratorSettings createChunkGeneratorSettings(GenerationShapeConfig generationShapeConfig, BlockState defaultBlock, BlockState defaultFluid, NoiseRouter noiseRouter, MaterialRules.MaterialRule surfaceRule, List<MultiNoiseUtil.NoiseHypercube> spawnTarget, int seaLevel, boolean mobGenerationDisabled, boolean aquifers, boolean oreVeins, boolean usesLegacyRandom) {
        return new ChunkGeneratorSettings(
                generationShapeConfig,
                defaultBlock,
                defaultFluid,
                noiseRouter,
                surfaceRule,
                spawnTarget,
                seaLevel,
                mobGenerationDisabled,
                aquifers,
                oreVeins,
                usesLegacyRandom
        );
    }

    private static DensityFunctionTypes.RegistryEntryHolder fromKey(RegistryEntryLookup<DensityFunction> lookup, RegistryKey<DensityFunction> key) {
        return new DensityFunctionTypes.RegistryEntryHolder(lookup.getOrThrow(key));
    }
}
