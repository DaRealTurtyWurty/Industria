package dev.turtywurty.industria.init.worldgen;

import com.mojang.serialization.MapCodec;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.worldgen.feature.FluidPocketFeature;
import dev.turtywurty.turtymultiloader.registration.RegistrationHandle;
import dev.turtywurty.turtymultiloader.registration.RegistryService;
import net.minecraft.world.level.levelgen.feature.Feature;

public final class ModFeatures {
    public static final RegistrationHandle<MapCodec<? extends Feature>, MapCodec<FluidPocketFeature>> FLUID_POCKET = RegistryService
        .get().registerFeatureType(Industria.id("crude_oil_pocket"), () -> FluidPocketFeature.CODEC);

    private ModFeatures() {
    }

    public static void init() {
    }
}
