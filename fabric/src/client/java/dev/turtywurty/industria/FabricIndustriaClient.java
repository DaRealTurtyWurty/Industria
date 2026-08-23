package dev.turtywurty.industria;

import net.fabricmc.api.ClientModInitializer;

public class FabricIndustriaClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FabricIndustriaTestWorldButton.init();
        IndustriaClient.onInitializeClient();
        IndustriaClient.onRegistriesAppliedClient();
    }
}
