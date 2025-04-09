package dev.turtywurty.industria.init;

import dev.turtywurty.gasapi.handler.GasRenderHandlerRegistry;
import dev.turtywurty.gasapi.handler.SimpleGasRenderHandler;

public class GasRenderHandlerInit {
    public static void init() {
        GasRenderHandlerRegistry.register(GasInit.OXYGEN, new SimpleGasRenderHandler(0x80EEEEFF));
        GasRenderHandlerRegistry.register(GasInit.HYDROGEN, new SimpleGasRenderHandler(0x80DDDDDD));
        GasRenderHandlerRegistry.register(GasInit.CARBON_DIOXIDE, new SimpleGasRenderHandler(0x80CCCCCC));
        GasRenderHandlerRegistry.register(GasInit.METHANE, new SimpleGasRenderHandler(0x80FFCC00));
    }
}
