package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.renderer.world.ConveyorNetworkLevelRenderer;
import dev.turtywurty.turtymultiloader.event.client.ClientEvents;

public final class ModReloadListeners {
    private ModReloadListeners() {
    }

    public static void init() {
        ClientEvents.registerResourceReloadListener(
                Industria.id("client_conveyor_networks"),
                ConveyorNetworkLevelRenderer.ReloadListener.INSTANCE
        );

        ClientEvents.registerResourceReloadListener(
                Industria.id("conveyor_special_renderer"),
                ModConveyorSpecialRenderers.ReloadListener.INSTANCE
        );
    }
}
