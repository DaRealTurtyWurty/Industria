package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.turtymultiloader.registration.RegistrationHandle;
import dev.turtywurty.turtymultiloader.registration.RegistryService;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;

public final class ModParticleTypes {
    public static final RegistrationHandle<ParticleType<?>, SimpleParticleType> COKE_OVEN_SMOKE =
        RegistryService.get().register(Registries.PARTICLE_TYPE, Industria.id("coke_oven_smoke"),
            () -> new SimpleParticleType(false) {});

    private ModParticleTypes() {
    }

    public static void init() {
    }
}
