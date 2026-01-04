package dev.turtywurty.industria.worldgen.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.material.FluidState;

public record FluidPocketConfig(FluidState fluidState, IntProvider radius, IntProvider depth, RuleTest replaceable) implements FeatureConfiguration {
    public static final Codec<FluidPocketConfig> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    FluidState.CODEC.fieldOf("fluid_state").forGetter(FluidPocketConfig::fluidState),
                    IntProvider.POSITIVE_CODEC.fieldOf("radius").forGetter(FluidPocketConfig::radius),
                    IntProvider.POSITIVE_CODEC.fieldOf("depth").forGetter(FluidPocketConfig::depth),
                    RuleTest.CODEC.fieldOf("replaceable").forGetter(FluidPocketConfig::replaceable)
            ).apply(instance, FluidPocketConfig::new)
    );
}
