package dev.turtywurty.industria.init.worldgen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.worldgen.config.FluidPocketConfig;
import dev.turtywurty.industria.worldgen.feature.FluidPocketFeature;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class FeatureInit {
    public static final Feature<FluidPocketConfig> FLUID_POCKET =
            register("crude_oil_pocket", new FluidPocketFeature());

    public static <FC extends FeatureConfiguration, F extends Feature<FC>> F register(String name, F feature) {
        return Registry.register(BuiltInRegistries.FEATURE, Industria.id(name), feature);
    }

    public static void init() {}
}
