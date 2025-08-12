package dev.turtywurty.industria.init.worldgen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.worldgen.config.FluidPocketConfig;
import dev.turtywurty.industria.worldgen.feature.FluidPocketFeature;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;

public class FeatureInit {
    public static final Feature<FluidPocketConfig> FLUID_POCKET =
            register("crude_oil_pocket", new FluidPocketFeature());

    public static <FC extends FeatureConfig, F extends Feature<FC>> F register(String name, F feature) {
        return Registry.register(Registries.FEATURE, Industria.id(name), feature);
    }

    public static void init() {
    }
}
