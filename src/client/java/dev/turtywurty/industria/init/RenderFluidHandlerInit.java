package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;

public class RenderFluidHandlerInit {
    public static void init() {
        FluidRenderHandlerRegistry.INSTANCE.register(FluidInit.CRUDE_OIL.still(), FluidInit.CRUDE_OIL.flowing(),
                new SimpleFluidRenderHandler(Industria.id("block/crude_oil_still"), Industria.id("block/crude_oil_flow")));

        FluidRenderHandlerRegistry.INSTANCE.register(FluidInit.DIRTY_SODIUM_ALUMINATE.still(), FluidInit.DIRTY_SODIUM_ALUMINATE.flowing(),
                new SimpleFluidRenderHandler(Industria.id("block/dirty_sodium_aluminate_still"), Industria.id("block/dirty_sodium_aluminate_flow")));

        FluidRenderHandlerRegistry.INSTANCE.register(FluidInit.SODIUM_ALUMINATE.still(), FluidInit.SODIUM_ALUMINATE.flowing(),
                new SimpleFluidRenderHandler(Industria.id("block/sodium_aluminate_still"), Industria.id("block/sodium_aluminate_flow")));

        FluidRenderHandlerRegistry.INSTANCE.register(FluidInit.MOLTEN_ALUMINIUM.still(), FluidInit.MOLTEN_ALUMINIUM.flowing(),
                new SimpleFluidRenderHandler(Industria.id("block/molten_aluminium_still"), Industria.id("block/molten_aluminium_flow")));

        FluidRenderHandlerRegistry.INSTANCE.register(FluidInit.MOLTEN_CRYOLITE.still(), FluidInit.MOLTEN_CRYOLITE.flowing(),
                new SimpleFluidRenderHandler(Industria.id("block/molten_cryolite_still"), Industria.id("block/molten_cryolite_flow")));
    }
}
