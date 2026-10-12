package dev.turtywurty.industria.init;

import dev.turtywurty.industria.menu.SmelteryControllerMenu;
import dev.turtywurty.industria.menu.UpgradeStationScreenHandler;
import dev.turtywurty.industria.network.*;
import dev.turtywurty.industria.renderer.block.RotaryKilnBlockEntityRenderer;
import dev.turtywurty.industria.renderer.world.FluidPocketLevelRenderer;
import dev.turtywurty.industria.screen.SeismicScannerScreen;
import dev.turtywurty.turtymultiloader.network.NetworkService;
import dev.turtywurty.turtymultiloader.network.PayloadPhase;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class ModClientPackets {
    public static void init() {
        NetworkService networkService = NetworkService.get();
        networkService.registerClientHandler(PayloadPhase.PLAY, OpenSeismicScannerPayload.ID, (payload, context) ->
                context.enqueueWork(() -> Minecraft.getInstance().gui.setScreen(new SeismicScannerScreen(payload.stack()))));

        networkService.registerClientHandler(PayloadPhase.PLAY, SyncFluidPocketsPayload.ID, (payload, context) -> {
            ResourceKey<Level> worldKey = context.player().get().level().dimension();
            FluidPocketLevelRenderer.FLUID_POCKETS.put(worldKey, payload.fluidPockets());
        });

        networkService.registerClientHandler(PayloadPhase.PLAY, UpgradeStationUpdateRecipesPayload.ID, (payload, context) -> {
            if (context.player().get().containerMenu instanceof UpgradeStationScreenHandler handler) {
                handler.setAvailableRecipes(payload.recipes());
            }
        });

        networkService.registerClientHandler(PayloadPhase.PLAY, RotaryKilnControllerRemovedPayload.ID,
                (payload, context) -> context.enqueueWork(
                        () -> RotaryKilnBlockEntityRenderer.BLOCK_POS_RENDERER_DATA_MAP.remove(payload.pos())));

        networkService.registerClientHandler(PayloadPhase.PLAY, SmelteryControllerSnapshotPayload.ID,
                (payload, context) -> context.enqueueWork(() -> {
                    Player player = context.player().orElse(null);
                    if (player != null
                            && player.containerMenu instanceof SmelteryControllerMenu menu
                            && menu.getControllerPos().equals(payload.controllerPos())
                            && menu.containerId == payload.syncId()) {
                        menu.applySnapshot(payload.temperature(), payload.structureReady(), payload.keepMolten(), payload.chambers());
                    }
                }));
    }
}
