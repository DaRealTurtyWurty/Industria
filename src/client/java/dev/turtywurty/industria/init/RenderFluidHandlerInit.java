package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;

public class RenderFluidHandlerInit {
    public static void init() {
        FluidRenderHandlerRegistry.INSTANCE.register(FluidInit.CRUDE_OIL, FluidInit.CRUDE_OIL_FLOWING,
                new SimpleFluidRenderHandler(Industria.id("block/crude_oil_still"), Industria.id("block/crude_oil_flow")));
    }
}
