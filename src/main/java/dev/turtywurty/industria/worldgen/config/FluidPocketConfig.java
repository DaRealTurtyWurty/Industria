package dev.turtywurty.industria.worldgen.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.fluid.FluidState;
import net.minecraft.structure.rule.RuleTest;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.world.gen.feature.FeatureConfig;

public record FluidPocketConfig(FluidState fluidState, IntProvider radius, IntProvider depth,
                                RuleTest replaceable) implements FeatureConfig {
    public static final Codec<FluidPocketConfig> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    FluidState.CODEC.fieldOf("fluid_state").forGetter(FluidPocketConfig::fluidState),
                    IntProvider.POSITIVE_CODEC.fieldOf("radius").forGetter(FluidPocketConfig::radius),
                    IntProvider.POSITIVE_CODEC.fieldOf("depth").forGetter(FluidPocketConfig::depth),
                    RuleTest.TYPE_CODEC.fieldOf("replaceable").forGetter(FluidPocketConfig::replaceable)
            ).apply(instance, FluidPocketConfig::new)
    );
}
