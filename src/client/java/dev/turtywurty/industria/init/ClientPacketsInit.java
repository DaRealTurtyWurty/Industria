package dev.turtywurty.industria.init;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.turtywurty.industria.network.OpenSeismicScannerPayload;
import dev.turtywurty.industria.network.SyncFluidPocketsPayload;
import dev.turtywurty.industria.network.SyncPipeNetworksPayload;
import dev.turtywurty.industria.network.UpgradeStationUpdateRecipesPayload;
import dev.turtywurty.industria.pipe.PipeNetworkManager;
import dev.turtywurty.industria.renderer.world.FluidPocketWorldRenderer;
import dev.turtywurty.industria.screen.SeismicScannerScreen;
import dev.turtywurty.industria.screenhandler.UpgradeStationScreenHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientPacketsInit {
    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(OpenSeismicScannerPayload.ID, (payload, context) ->
                context.client().execute(() ->
                        context.client().setScreen(new SeismicScannerScreen(payload.stack()))));

        ClientPlayNetworking.registerGlobalReceiver(SyncFluidPocketsPayload.ID, (payload, context) -> {
            RegistryKey<World> worldKey = context.player().getEntityWorld().getRegistryKey();
            FluidPocketWorldRenderer.FLUID_POCKETS.put(worldKey, payload.fluidPockets());
        });

        ClientPlayNetworking.registerGlobalReceiver(UpgradeStationUpdateRecipesPayload.ID, (payload, context) -> {
            if (context.player().currentScreenHandler instanceof UpgradeStationScreenHandler handler) {
                handler.setAvailableRecipes(payload.recipes());
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(SyncPipeNetworksPayload.ID, new SyncPipeNetworksHandler());
    }

    public static class SyncPipeNetworksHandler implements ClientPlayNetworking.PlayPayloadHandler<SyncPipeNetworksPayload> {
        private final Map<UUID, Map<Integer, String>> packetChunkMap = new HashMap<>();

        @Override
        public void receive(SyncPipeNetworksPayload payload, ClientPlayNetworking.Context context) {
            Map<Integer, String> chunkList = packetChunkMap.computeIfAbsent(payload.packetGroupId(), k -> new HashMap<>());
            int index = payload.index();
            chunkList.put(index, payload.data());

            boolean allReceived = chunkList.size() == payload.chunks();
            if(!allReceived)
                return;

            ClientPlayerEntity player = context.player();
            if(payload.index() == payload.chunks() - 1) {
                RegistryKey<World> worldKey = player.getEntityWorld().getRegistryKey(); // TODO: Use this
                String nbtString = chunkList.values().stream().reduce((a, b) -> a + b).orElse("");

                try {
                    NbtCompound data = StringNbtReader.parse(nbtString);
                    PipeNetworkManager.readAllNbt(worldKey, data, player.getEntityWorld().getRegistryManager());
                } catch (CommandSyntaxException e) {
                    player.sendMessage(Text.literal("Failed to read pipe network data").withColor(0xFF0000), false);
                }
            }

            packetChunkMap.remove(payload.packetGroupId());
        }
    }
}
