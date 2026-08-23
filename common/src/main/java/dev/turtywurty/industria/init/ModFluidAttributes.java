package dev.turtywurty.industria.init;

import dev.turtywurty.turtymultiloader.transfer.fluid.FluidVariantAttributeHandler;
import dev.turtywurty.turtymultiloader.transfer.fluid.FluidVariantAttributes;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

public class ModFluidAttributes {
    public static void init() {
        var commonFluidAttributes = new FluidVariantAttributeHandler() {
            @Override
            public int getViscosity(ResourceVariant<Fluid> variant, @Nullable Level level) {
                return 7500;
            }
        };

        FluidVariantAttributes.register(ModFluids.CRUDE_OIL.still().get(), commonFluidAttributes);
        FluidVariantAttributes.register(ModFluids.CRUDE_OIL.flowing().get(), commonFluidAttributes);

        FluidVariantAttributes.register(ModFluids.DIRTY_SODIUM_ALUMINATE.still().get(), commonFluidAttributes);
        FluidVariantAttributes.register(ModFluids.DIRTY_SODIUM_ALUMINATE.flowing().get(), commonFluidAttributes);

        FluidVariantAttributes.register(ModFluids.SODIUM_ALUMINATE.still().get(), commonFluidAttributes);
        FluidVariantAttributes.register(ModFluids.SODIUM_ALUMINATE.flowing().get(), commonFluidAttributes);

        FluidVariantAttributes.register(ModFluids.LATEX.still().get(), commonFluidAttributes);
        FluidVariantAttributes.register(ModFluids.LATEX.flowing().get(), commonFluidAttributes);

        FluidVariantAttributes.register(ModFluids.METHANOL.still().get(), commonFluidAttributes);
        FluidVariantAttributes.register(ModFluids.METHANOL.flowing().get(), commonFluidAttributes);

        FluidVariantAttributes.register(ModFluids.DILUTED_FORMIC_ACID.still().get(), commonFluidAttributes);
        FluidVariantAttributes.register(ModFluids.DILUTED_FORMIC_ACID.flowing().get(), commonFluidAttributes);
    }
}
