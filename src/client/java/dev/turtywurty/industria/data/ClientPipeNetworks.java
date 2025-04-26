package dev.turtywurty.industria.data;

import dev.turtywurty.industria.init.PipeNetworkManagerTypeInit;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.network.AddPipeNetworkPayload;
import dev.turtywurty.industria.network.ModifyPipeNetworkPayload;
import dev.turtywurty.industria.network.RemovePipeNetworkPayload;
import dev.turtywurty.industria.network.SyncPipeNetworkManagerPayload;
import dev.turtywurty.industria.pipe.PipeNetwork;
import dev.turtywurty.industria.pipe.PipeNetworkManager;
import dev.turtywurty.industria.pipe.PipeNetworkManagerType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ClientPipeNetworks {
    private static final Map<RegistryKey<World>, List<PipeNetworkManager<?, PipeNetwork<?>>>> PIPE_NETWORKS = new ConcurrentHashMap<>();

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(SyncPipeNetworkManagerPayload.ID, (payload, context) -> {
            RegistryKey<World> worldKey = payload.dimension();
            TransferType<?, ?, ?> transferType = payload.transferType();
            Map<BlockPos, UUID> pipeToNetworkId = payload.pipeToNetworkId();

            List<PipeNetworkManager<?, PipeNetwork<?>>> pipeNetworkManagers = PIPE_NETWORKS.computeIfAbsent(worldKey, k -> new ArrayList<>());
            pipeNetworkManagers.removeIf(pipeNetworkManager -> pipeNetworkManager.getTransferType() == transferType);
            PipeNetworkManagerType<?, ? extends PipeNetwork<?>> type = PipeNetworkManagerTypeInit.getType(transferType);
            PipeNetworkManager<?, ? extends PipeNetwork<?>> manager = type.factory().apply(worldKey);
            manager.getPipeToNetworkId().putAll(pipeToNetworkId);
            pipeNetworkManagers.add((PipeNetworkManager<?, PipeNetwork<?>>) manager);
        });

        ClientPlayNetworking.registerGlobalReceiver(AddPipeNetworkPayload.ID, (payload, context) -> {
            RegistryKey<World> worldKey = payload.world();
            TransferType<?, ?, ?> transferType = payload.transferType();
            PipeNetwork<?> network = payload.network();

            List<PipeNetworkManager<?, PipeNetwork<?>>> pipeNetworkManagers = PIPE_NETWORKS.get(worldKey);
            if (pipeNetworkManagers == null)
                return;

            PipeNetworkManager<?, PipeNetwork<?>> manager = pipeNetworkManagers.stream()
                    .filter(pipeNetworkManager -> pipeNetworkManager.getTransferType() == transferType)
                    .findFirst()
                    .orElse(null);
            if (manager == null)
                return;

            Set<PipeNetwork<?>> networks = manager.getNetworks();
            networks.add(network);
        });

        ClientPlayNetworking.registerGlobalReceiver(RemovePipeNetworkPayload.ID, (payload, context) -> {
            RegistryKey<World> worldKey = payload.world();
            TransferType<?, ?, ?> transferType = payload.transferType();
            UUID networkId = payload.networkId();

            List<PipeNetworkManager<?, PipeNetwork<?>>> pipeNetworkManagers = PIPE_NETWORKS.get(worldKey);
            if (pipeNetworkManagers == null)
                return;

            PipeNetworkManager<?, PipeNetwork<?>> manager = pipeNetworkManagers.stream()
                    .filter(pipeNetworkManager -> pipeNetworkManager.getTransferType() == transferType)
                    .findFirst()
                    .orElse(null);
            if (manager == null)
                return;

            Set<PipeNetwork<?>> networks = manager.getNetworks();
            networks.removeIf(pipeNetwork -> pipeNetwork.getId().equals(networkId));
        });

        ClientPlayNetworking.registerGlobalReceiver(ModifyPipeNetworkPayload.ID, (payload, context) -> {
            ModifyPipeNetworkPayload.Operation operation = payload.operation();
            RegistryKey<World> worldKey = payload.world();
            TransferType<?, ?, ?> transferType = payload.transferType();
            UUID networkId = payload.networkId();
            BlockPos pos = payload.pos();

            List<PipeNetworkManager<?, PipeNetwork<?>>> pipeNetworkManagers = PIPE_NETWORKS.get(worldKey);
            if (pipeNetworkManagers == null)
                return;

            PipeNetworkManager<?, PipeNetwork<?>> manager = pipeNetworkManagers.stream()
                    .filter(pipeNetworkManager -> pipeNetworkManager.getTransferType() == transferType)
                    .findFirst()
                    .orElse(null);
            if (manager == null)
                return;

            Set<PipeNetwork<?>> networks = manager.getNetworks();
            Optional<PipeNetwork<?>> network = networks.stream()
                    .filter(pipeNetwork -> pipeNetwork.getId().equals(networkId))
                    .findFirst();
            if (operation == ModifyPipeNetworkPayload.Operation.ADD_PIPE) {
                network.ifPresent(pipeNetwork -> pipeNetwork.addPipe(pos));
            } else if (operation == ModifyPipeNetworkPayload.Operation.REMOVE_PIPE) {
                network.ifPresent(pipeNetwork -> pipeNetwork.removePipe(pos));
            } else if (operation == ModifyPipeNetworkPayload.Operation.ADD_CONNECTED_BLOCK) {
                network.ifPresent(pipeNetwork -> pipeNetwork.getConnectedBlocks().add(pos));
            } else if (operation == ModifyPipeNetworkPayload.Operation.REMOVE_CONNECTED_BLOCK) {
                network.ifPresent(pipeNetwork -> pipeNetwork.getConnectedBlocks().remove(pos));
            }
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) ->
                PIPE_NETWORKS.clear());
    }

    public static List<PipeNetworkManager<?, PipeNetwork<?>>> get(RegistryKey<World> worldKey) {
        return PIPE_NETWORKS.get(worldKey);
    }
}
