package dev.turtywurty.industria.worldgen.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.floatprovider.FloatProvider;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

public record FloatingOrbFeatureConfig(BlockStateProvider state1, BlockStateProvider state2,
                                       IntProvider radius, IntProvider yOffset, FloatProvider chanceOfSecondBlock,
                                       FloatProvider hollowChance, FloatProvider vineChance, IntProvider vineLength,
                                       BlockStateProvider vineBlock) implements FeatureConfig {
    public static final Codec<FloatingOrbFeatureConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockStateProvider.TYPE_CODEC.fieldOf("state1").forGetter(FloatingOrbFeatureConfig::state1),
            BlockStateProvider.TYPE_CODEC.fieldOf("state2").forGetter(FloatingOrbFeatureConfig::state2),
            IntProvider.VALUE_CODEC.fieldOf("radius").forGetter(FloatingOrbFeatureConfig::radius),
            IntProvider.VALUE_CODEC.fieldOf("y_offset").forGetter(FloatingOrbFeatureConfig::yOffset),
            FloatProvider.VALUE_CODEC.fieldOf("chance_of_second_block").forGetter(FloatingOrbFeatureConfig::chanceOfSecondBlock),
            FloatProvider.VALUE_CODEC.fieldOf("hollow_chance").forGetter(FloatingOrbFeatureConfig::hollowChance),
            FloatProvider.VALUE_CODEC.fieldOf("vine_chance").forGetter(FloatingOrbFeatureConfig::vineChance),
            IntProvider.VALUE_CODEC.fieldOf("vine_length").forGetter(FloatingOrbFeatureConfig::vineLength),
            BlockStateProvider.TYPE_CODEC.fieldOf("vine_block").forGetter(FloatingOrbFeatureConfig::vineBlock)
    ).apply(instance, FloatingOrbFeatureConfig::new));
}
