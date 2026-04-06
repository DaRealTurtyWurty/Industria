package dev.turtywurty.industria;

import dev.turtywurty.industria.data.ClientConveyorNetworks;
import dev.turtywurty.industria.data.ClientPipeNetworks;
import dev.turtywurty.industria.init.*;
import dev.turtywurty.industria.util.DebugRenderingRegistry;
import net.fabricmc.api.ClientModInitializer;

public class IndustriaClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ScreenInit.init();
        EntityModelLayerInit.init();
        BlockEntityRendererInit.init();
        DynamicItemRendererInit.init();
        RenderFluidHandlerInit.init();
        SlurryRenderHandlerInit.init();
        GasRenderHandlerInit.init();
        ClientPacketsInit.init();
        ClientEventsInit.init();
        ModelInit.init();
        ArmPositionInit.init();
        DrillHeadInit.init();
        DebugRenderingRegistry.init();
        EntityRendererInit.init();
        ColorProviderInit.init();
        ClientPipeNetworks.init();
        ClientConveyorNetworks.init();
        ReloadListenerInit.init();
        ConveyorAnchorProviderInit.init();
        ConveyorSpecialRendererInit.init();
        TooltipInit.init();
    }
}
