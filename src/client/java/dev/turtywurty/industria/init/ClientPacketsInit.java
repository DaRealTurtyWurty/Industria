package dev.turtywurty.industria.init;

import dev.turtywurty.industria.network.OpenSeismicScannerPayload;
import dev.turtywurty.industria.network.SyncFluidPocketsPayload;
import dev.turtywurty.industria.network.UpgradeStationUpdateRecipesPayload;
import dev.turtywurty.industria.renderer.world.FluidPocketWorldRenderer;
import dev.turtywurty.industria.screen.SeismicScannerScreen;
import dev.turtywurty.industria.screenhandler.UpgradeStationScreenHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;

public class ClientPacketsInit {
    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(OpenSeismicScannerPayload.ID, (payload, context) ->
                context.client().execute(() ->
                        context.client().setScreen(new SeismicScannerScreen(payload.stack()))));

        ClientPlayNetworking.registerGlobalReceiver(SyncFluidPocketsPayload.ID, (payload, context) -> {
            RegistryKey<World> worldKey = context.player().getWorld().getRegistryKey();
            FluidPocketWorldRenderer.FLUID_POCKETS.put(worldKey, payload.fluidPockets());
        });

        ClientPlayNetworking.registerGlobalReceiver(UpgradeStationUpdateRecipesPayload.ID, (payload, context) -> {
            if (context.player().currentScreenHandler instanceof UpgradeStationScreenHandler handler) {
                handler.setAvailableRecipes(payload.recipes());
            }
        });
    }
}
