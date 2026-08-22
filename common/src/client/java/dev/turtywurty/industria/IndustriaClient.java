package dev.turtywurty.industria;

import dev.turtywurty.industria.data.ClientConveyorNetworks;
import dev.turtywurty.industria.pipe.ClientPipeNetworks;
import dev.turtywurty.industria.client.IndustriaWoodSetClient;
import dev.turtywurty.industria.init.*;
import dev.turtywurty.industria.pipe.PipeConnectionModelLoading;
import dev.turtywurty.industria.util.DebugRenderingRegistry;

public class IndustriaClient {
    private static boolean postRegistryInitialized;

    public static void onInitializeClient() {
        ModScreens.init();
        ModEntityModelLayers.init();
        ModBlockEntityRenderers.init();
        ModDynamicItemRenderers.init();
        ModRenderFluidHandlers.init();
        ModClientPackets.init();
        ModClientEvents.init();
        ModModels.init();
        DebugRenderingRegistry.init();
        IndustriaWoodSetClient.init();
        ClientPipeNetworks.init();
        ClientConveyorNetworks.init();
        ModReloadListeners.init();
        ModTooltips.init();
    }

    public static synchronized void onRegistriesAppliedClient() {
        if (postRegistryInitialized)
            return;

        ModSlurryRenderHandlers.init();
        ModGasRenderHandlers.init();
        ModPipeConnectionModels.register();
        PipeConnectionModelLoading.init();
        ModArmPositions.init();
        ModDrillHeads.init();
        ModConveyorAnchorProviders.init();
        ModConveyorSpecialRenderers.init();

        postRegistryInitialized = true;
    }
}
