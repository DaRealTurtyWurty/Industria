package dev.turtywurty.industria.init;

import dev.turtywurty.gasapi.api.GasVariantAttributes;
import dev.turtywurty.industria.Industria;
import net.minecraft.network.chat.Component;

public class ModGasAttributes {
    public static void init() {
        Component oxygenText = Component.translatable("gas." + Industria.MOD_ID + ".oxygen");
        GasVariantAttributes.register(ModGases.OXYGEN.get(), gasVariant -> oxygenText);
        Component hydrogenText = Component.translatable("gas." + Industria.MOD_ID + ".hydrogen");
        GasVariantAttributes.register(ModGases.HYDROGEN.get(), gasVariant -> hydrogenText);
        Component carbonDioxideText = Component.translatable("gas." + Industria.MOD_ID + ".carbon_dioxide");
        GasVariantAttributes.register(ModGases.CARBON_DIOXIDE.get(), gasVariant -> carbonDioxideText);
        Component methaneText = Component.translatable("gas." + Industria.MOD_ID + ".methane");
        GasVariantAttributes.register(ModGases.METHANE.get(), gasVariant -> methaneText);
        Component carbonMonoxideText = Component.translatable("gas." + Industria.MOD_ID + ".carbon_monoxide");
        GasVariantAttributes.register(ModGases.CARBON_MONOXIDE.get(), gasVariant -> carbonMonoxideText);
    }
}
