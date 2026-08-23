package dev.turtywurty.industria.init.worldgen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.worldgen.config.FluidPocketConfig;
import dev.turtywurty.industria.worldgen.feature.FluidPocketFeature;
import dev.turtywurty.turtymultiloader.registration.RegistrationHandle;
import dev.turtywurty.turtymultiloader.registration.RegistryService;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

import java.util.function.Supplier;

public class ModFeatures {
    private static final RegistryService REGISTRIES = RegistryService.get();

    public static final RegistrationHandle<Feature<?>, Feature<FluidPocketConfig>> FLUID_POCKET =
            register("crude_oil_pocket", FluidPocketFeature::new);

    public static <FC extends FeatureConfiguration, F extends Feature<FC>> RegistrationHandle<Feature<?>, F> register(String name, Supplier<F> feature) {
        return REGISTRIES.registerFeature(Industria.id(name), feature);
    }

    public static void init() {
    }
}
