package dev.turtywurty.industria.init;

import dev.turtywurty.gasapi.api.GasVariantAttributes;
import dev.turtywurty.industria.Industria;
import net.minecraft.network.chat.Component;

public class GasAttributesInit {
    public static void init() {
        Component oxygenText = Component.translatable("gas." + Industria.MOD_ID + ".oxygen");
        GasVariantAttributes.register(GasInit.OXYGEN, gasVariant -> oxygenText);
        Component hydrogenText = Component.translatable("gas." + Industria.MOD_ID + ".hydrogen");
        GasVariantAttributes.register(GasInit.HYDROGEN, gasVariant -> hydrogenText);
        Component carbonDioxideText = Component.translatable("gas." + Industria.MOD_ID + ".carbon_dioxide");
        GasVariantAttributes.register(GasInit.CARBON_DIOXIDE, gasVariant -> carbonDioxideText);
        Component methaneText = Component.translatable("gas." + Industria.MOD_ID + ".methane");
        GasVariantAttributes.register(GasInit.METHANE, gasVariant -> methaneText);
    }
}
