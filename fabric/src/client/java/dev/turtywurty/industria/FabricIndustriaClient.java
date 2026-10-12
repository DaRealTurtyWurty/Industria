package dev.turtywurty.industria;

import dev.turtywurty.industria.molding.MoldCastPreviewRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.PictureInPictureRendererRegistry;

public class FabricIndustriaClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FabricIndustriaTestWorldButton.init();
        IndustriaClient.onInitializeClient();
        IndustriaClient.onRegistriesAppliedClient();

        PictureInPictureRendererRegistry.register(context -> new MoldCastPreviewRenderer());
    }
}
