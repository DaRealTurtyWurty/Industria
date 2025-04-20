package dev.turtywurty.industria.init;

import dev.turtywurty.industria.network.OpenSeismicScannerPayload;
import dev.turtywurty.industria.network.SyncFluidPocketsPayload;
import dev.turtywurty.industria.network.UpgradeStationUpdateRecipesPayload;
import dev.turtywurty.industria.persistent.WorldPipeNetworks;
import dev.turtywurty.industria.pipe.PipeNetworkNetworking;
import dev.turtywurty.industria.renderer.world.FluidPocketWorldRenderer;
import dev.turtywurty.industria.screen.SeismicScannerScreen;
import dev.turtywurty.industria.screenhandler.UpgradeStationScreenHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;

import java.util.*;

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

        ClientPlayNetworking.registerGlobalReceiver(PipeNetworkNetworking.Payload.ID, new SyncPipeNetworksHandler());
    }

    public static class SyncPipeNetworksHandler implements ClientPlayNetworking.PlayPayloadHandler<PipeNetworkNetworking.Payload> {
        private final Map<UUID, List<byte[]>> packetChunkMap = new HashMap<>();

        @Override
        public void receive(PipeNetworkNetworking.Payload payload, ClientPlayNetworking.Context context) {
            if(payload.data().length != 0) {
                packetChunkMap.computeIfAbsent(payload.uuid(), k -> new ArrayList<>()).add(payload.data());
                return;
            }

            if (packetChunkMap.containsKey(payload.uuid())) {
                List<byte[]> chunks = packetChunkMap.get(payload.uuid());
                byte[] data = new byte[chunks.stream().mapToInt(arr -> arr.length).sum()];
                int offset = 0;
                for (byte[] chunk : chunks) {
                    System.arraycopy(chunk, 0, data, offset, chunk.length);
                    offset += chunk.length;
                }

                packetChunkMap.remove(payload.uuid());

                PacketByteBuf packetByteBuf = PacketByteBufs.create();
                packetByteBuf.writeBytes(data);

                RegistryByteBuf registryByteBuf = new RegistryByteBuf(packetByteBuf, context.player().getRegistryManager());
                WorldPipeNetworks worldPipeNetworks = WorldPipeNetworks.PACKET_CODEC.decode(registryByteBuf);
            }
        }
    }
}
