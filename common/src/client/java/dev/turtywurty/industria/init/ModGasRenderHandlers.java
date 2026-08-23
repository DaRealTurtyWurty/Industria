package dev.turtywurty.industria.init;

import dev.turtywurty.gasapi.client.GasRenderHandlerRegistry;
import dev.turtywurty.gasapi.client.SimpleGasRenderHandler;

public class ModGasRenderHandlers {
    public static void init() {
        GasRenderHandlerRegistry.register(ModGases.OXYGEN.get(), new SimpleGasRenderHandler(0x80EEEEFF));
        GasRenderHandlerRegistry.register(ModGases.HYDROGEN.get(), new SimpleGasRenderHandler(0x80DDDDDD));
        GasRenderHandlerRegistry.register(ModGases.CARBON_DIOXIDE.get(), new SimpleGasRenderHandler(0x80CCCCCC));
        GasRenderHandlerRegistry.register(ModGases.METHANE.get(), new SimpleGasRenderHandler(0x80FFCC00));
        GasRenderHandlerRegistry.register(ModGases.CARBON_MONOXIDE.get(), new SimpleGasRenderHandler(0x80FF0000));
    }
}
