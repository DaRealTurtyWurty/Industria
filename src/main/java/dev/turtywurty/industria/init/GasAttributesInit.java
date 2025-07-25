package dev.turtywurty.industria.init;

import dev.turtywurty.gasapi.api.GasVariantAttributes;
import dev.turtywurty.industria.Industria;
import net.minecraft.text.Text;

public class GasAttributesInit {
    public static void init() {
        Text oxygenText = Text.translatable("gas." + Industria.MOD_ID + ".oxygen");
        GasVariantAttributes.register(GasInit.OXYGEN, gasVariant -> oxygenText);
        Text hydrogenText = Text.translatable("gas." + Industria.MOD_ID + ".hydrogen");
        GasVariantAttributes.register(GasInit.HYDROGEN, gasVariant -> hydrogenText);
        Text carbonDioxideText = Text.translatable("gas." + Industria.MOD_ID + ".carbon_dioxide");
        GasVariantAttributes.register(GasInit.CARBON_DIOXIDE, gasVariant -> carbonDioxideText);
        Text methaneText = Text.translatable("gas." + Industria.MOD_ID + ".methane");
        GasVariantAttributes.register(GasInit.METHANE, gasVariant -> methaneText);
    }
}
