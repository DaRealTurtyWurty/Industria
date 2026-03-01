package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.renderer.world.ConveyorNetworkLevelRenderer;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.server.packs.PackType;

public final class ReloadListenerInit {
    private ReloadListenerInit() {
    }

    public static void init() {
        ResourceLoader clientResourceLoader = ResourceLoader.get(PackType.CLIENT_RESOURCES);

        clientResourceLoader.registerReloadListener(
                Industria.id("client_conveyor_networks"),
                ConveyorNetworkLevelRenderer.ReloadListener.INSTANCE
        );
    }
}
