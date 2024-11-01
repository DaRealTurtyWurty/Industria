package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import net.minecraft.entity.damage.DamageScaling;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

public class DamageTypeInit {
    public static final RegistryKey<DamageType> DRILL = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Industria.id("drill"));

    public static void bootstrap(Registerable<DamageType> context) {
        context.register(DRILL, new DamageType(Industria.MOD_ID + ".drill", DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER, 0.1f));
    }

    public static DamageSource drillDamageSource(DamageSources damageSources) {
        return new DamageSource(damageSources.registry.getOrThrow(DRILL));
    }
}
