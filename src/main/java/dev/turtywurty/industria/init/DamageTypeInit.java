package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageType;

public class DamageTypeInit {
    public static final ResourceKey<DamageType> DRILL = ResourceKey.create(Registries.DAMAGE_TYPE, Industria.id("drill"));

    public static void bootstrap(BootstrapContext<DamageType> context) {
        context.register(DRILL, new DamageType(Industria.MOD_ID + ".drill", DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER, 0.1f));
    }

    public static DamageSource drillDamageSource(DamageSources damageSources) {
        return new DamageSource(damageSources.damageTypes.getOrThrow(DRILL));
    }
}
