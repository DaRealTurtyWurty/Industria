package dev.turtywurty.industria.init;

import dev.turtywurty.industria.network.OpenSeismicScannerPayload;
import dev.turtywurty.industria.network.RotaryKilnControllerRemovedPayload;
import dev.turtywurty.industria.network.SyncFluidPocketsPayload;
import dev.turtywurty.industria.network.UpgradeStationUpdateRecipesPayload;
import dev.turtywurty.industria.renderer.block.RotaryKilnBlockEntityRenderer;
import dev.turtywurty.industria.renderer.world.FluidPocketWorldRenderer;
import dev.turtywurty.industria.screen.SeismicScannerScreen;
import dev.turtywurty.industria.screenhandler.UpgradeStationScreenHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class ClientPacketsInit {
    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(OpenSeismicScannerPayload.ID, (payload, context) ->
                context.client().execute(() ->
                        context.client().setScreen(new SeismicScannerScreen(payload.stack()))));

        ClientPlayNetworking.registerGlobalReceiver(SyncFluidPocketsPayload.ID, (payload, context) -> {
            ResourceKey<Level> worldKey = context.player().level().dimension();
            FluidPocketWorldRenderer.FLUID_POCKETS.put(worldKey, payload.fluidPockets());
        });

        ClientPlayNetworking.registerGlobalReceiver(UpgradeStationUpdateRecipesPayload.ID, (payload, context) -> {
            if (context.player().containerMenu instanceof UpgradeStationScreenHandler handler) {
                handler.setAvailableRecipes(payload.recipes());
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(RotaryKilnControllerRemovedPayload.ID,
                (payload, context) -> context.client().execute(
                        () -> RotaryKilnBlockEntityRenderer.BLOCK_POS_RENDERER_DATA_MAP.remove(payload.pos())));
    }
}
