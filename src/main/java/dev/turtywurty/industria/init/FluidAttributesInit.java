package dev.turtywurty.industria.init;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributeHandler;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class FluidAttributesInit {
    public static void init() {
        var commonFluidAttributes = new FluidVariantAttributeHandler() {
            @Override
            public int getViscosity(FluidVariant variant, @Nullable Level world) {
                return 7500;
            }
        };

        FluidVariantAttributes.register(FluidInit.CRUDE_OIL.still(), commonFluidAttributes);
        FluidVariantAttributes.register(FluidInit.CRUDE_OIL.flowing(), commonFluidAttributes);

        FluidVariantAttributes.register(FluidInit.DIRTY_SODIUM_ALUMINATE.still(), commonFluidAttributes);
        FluidVariantAttributes.register(FluidInit.DIRTY_SODIUM_ALUMINATE.flowing(), commonFluidAttributes);

        FluidVariantAttributes.register(FluidInit.SODIUM_ALUMINATE.still(), commonFluidAttributes);
        FluidVariantAttributes.register(FluidInit.SODIUM_ALUMINATE.flowing(), commonFluidAttributes);
    }
}
