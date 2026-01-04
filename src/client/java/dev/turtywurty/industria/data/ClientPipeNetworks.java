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
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ClientPipeNetworks {
    private static final Map<ResourceKey<Level>, List<PipeNetworkManager<?, PipeNetwork<?>>>> PIPE_NETWORKS = new ConcurrentHashMap<>();

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(SyncPipeNetworkManagerPayload.ID, (payload, context) -> {
            ResourceKey<Level> worldKey = payload.dimension();
            TransferType<?, ?, ?> transferType = payload.transferType();
            Map<BlockPos, UUID> pipeToNetworkId = payload.pipeToNetworkId();

            List<PipeNetworkManager<?, PipeNetwork<?>>> pipeNetworkManagers = PIPE_NETWORKS.computeIfAbsent(worldKey, k -> new ArrayList<>());
            pipeNetworkManagers.removeIf(pipeNetworkManager -> pipeNetworkManager.getTransferType() == transferType);
            PipeNetworkManagerType<?, ? extends PipeNetwork<?>> type = PipeNetworkManagerTypeInit.getType(transferType);
            PipeNetworkManager<?, ? extends PipeNetwork<?>> manager = type.factory().get();
            manager.getPipeToNetworkId().putAll(pipeToNetworkId);
            pipeNetworkManagers.add((PipeNetworkManager<?, PipeNetwork<?>>) manager);
        });

        ClientPlayNetworking.registerGlobalReceiver(AddPipeNetworkPayload.ID, (payload, context) -> {
            ResourceKey<Level> worldKey = payload.world();
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
            ResourceKey<Level> worldKey = payload.world();
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
            ResourceKey<Level> worldKey = payload.world();
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
            Optional<PipeNetwork<?>> optionalNetwork = networks.stream()
                    .filter(pipeNetwork -> pipeNetwork.getId().equals(networkId))
                    .findFirst();
            optionalNetwork.ifPresent(network -> {
                switch (operation) {
                    case ADD_PIPE -> network.addPipe(pos);
                    case REMOVE_PIPE -> network.removePipe(pos);
                    case ADD_CONNECTED_BLOCK -> network.getConnectedBlocks().add(pos);
                    case REMOVE_CONNECTED_BLOCK -> network.getConnectedBlocks().remove(pos);
                }
            });
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) ->
                PIPE_NETWORKS.clear());
    }

    public static List<PipeNetworkManager<?, PipeNetwork<?>>> get(ResourceKey<Level> worldKey) {
        return PIPE_NETWORKS.get(worldKey);
    }
}
