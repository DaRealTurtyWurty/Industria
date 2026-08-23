package dev.turtywurty.industria.init;

import dev.turtywurty.industria.menu.UpgradeStationScreenHandler;
import dev.turtywurty.industria.network.OpenSeismicScannerPayload;
import dev.turtywurty.industria.network.RotaryKilnControllerRemovedPayload;
import dev.turtywurty.industria.network.SyncFluidPocketsPayload;
import dev.turtywurty.industria.network.UpgradeStationUpdateRecipesPayload;
import dev.turtywurty.industria.renderer.block.RotaryKilnBlockEntityRenderer;
import dev.turtywurty.industria.renderer.world.FluidPocketLevelRenderer;
import dev.turtywurty.industria.screen.SeismicScannerScreen;
import dev.turtywurty.turtymultiloader.network.NetworkService;
import dev.turtywurty.turtymultiloader.network.PayloadPhase;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class ModClientPackets {
    public static void init() {
        NetworkService.get().registerClientHandler(PayloadPhase.PLAY, OpenSeismicScannerPayload.ID, (payload, context) ->
                context.enqueueWork(() -> Minecraft.getInstance().setScreen(new SeismicScannerScreen(payload.stack()))));

        NetworkService.get().registerClientHandler(PayloadPhase.PLAY, SyncFluidPocketsPayload.ID, (payload, context) -> {
            ResourceKey<Level> worldKey = context.player().get().level().dimension();
            FluidPocketLevelRenderer.FLUID_POCKETS.put(worldKey, payload.fluidPockets());
        });

        NetworkService.get().registerClientHandler(PayloadPhase.PLAY, UpgradeStationUpdateRecipesPayload.ID, (payload, context) -> {
            if (context.player().get().containerMenu instanceof UpgradeStationScreenHandler handler) {
                handler.setAvailableRecipes(payload.recipes());
            }
        });

        NetworkService.get().registerClientHandler(PayloadPhase.PLAY, RotaryKilnControllerRemovedPayload.ID,
                (payload, context) -> context.enqueueWork(
                        () -> RotaryKilnBlockEntityRenderer.BLOCK_POS_RENDERER_DATA_MAP.remove(payload.pos())));
    }
}
