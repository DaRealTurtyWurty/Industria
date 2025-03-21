package dev.turtywurty.industria.init;

import dev.turtywurty.fabricslurryapi.client.handler.SimpleSlurryRenderHandler;
import dev.turtywurty.fabricslurryapi.client.handler.SlurryRenderHandlerRegistry;
import dev.turtywurty.industria.Industria;

public class SlurryRenderHandlerInit {
    public static void init() {
        SlurryRenderHandlerRegistry.register(SlurryInit.BAUXITE_SLURRY,
                new SimpleSlurryRenderHandler(Industria.id("block/bauxite_slurry"), -1));
    }
}
