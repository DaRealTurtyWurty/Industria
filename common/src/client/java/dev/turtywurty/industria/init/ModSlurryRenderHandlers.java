package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.slurryapi.client.SimpleSlurryRenderHandler;
import dev.turtywurty.slurryapi.client.SlurryRenderHandlerRegistry;

public class ModSlurryRenderHandlers {
    public static void init() {
        SlurryRenderHandlerRegistry.register(ModSlurries.BAUXITE_SLURRY.get(),
                new SimpleSlurryRenderHandler(Industria.id("block/bauxite_slurry"), -1));
    }
}
