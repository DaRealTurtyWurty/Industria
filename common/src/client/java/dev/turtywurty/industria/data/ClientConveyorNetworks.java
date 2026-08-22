package dev.turtywurty.industria.data;

import dev.turtywurty.industria.conveyor.ConveyorNetwork;
import dev.turtywurty.industria.conveyor.ConveyorNetworkManager;
import dev.turtywurty.industria.network.conveyor.AddConveyorNetworkPayload;
import dev.turtywurty.industria.network.conveyor.ModifyConveyorNetworkPayload;
import dev.turtywurty.industria.network.conveyor.RemoveConveyorNetworkPayload;
import dev.turtywurty.turtymultiloader.event.client.ClientEvents;
import dev.turtywurty.turtymultiloader.network.NetworkService;
import dev.turtywurty.turtymultiloader.network.PayloadPhase;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ClientConveyorNetworks {
    private static final Map<ResourceKey<Level>, ConveyorNetworkManager> CONVEYOR_NETWORKS = new ConcurrentHashMap<>();

    public static void init() {
        NetworkService.get().registerClientHandler(PayloadPhase.PLAY, AddConveyorNetworkPayload.ID, (payload, _) -> {
            ResourceKey<Level> levelKey = payload.level();
            ConveyorNetwork network = payload.network();

            ConveyorNetworkManager manager = CONVEYOR_NETWORKS.computeIfAbsent(levelKey, _ -> new ConveyorNetworkManager());

            Set<ConveyorNetwork> networks = manager.getNetworks();
            networks.removeIf(existingNetwork -> existingNetwork.getId().equals(network.getId()));
            networks.add(network);

            Map<BlockPos, UUID> conveyorToNetworkId = manager.getConveyorToNetworkId();
            conveyorToNetworkId.entrySet().removeIf(entry -> entry.getValue().equals(network.getId()));
            for (BlockPos conveyor : network.getConveyors()) {
                manager.addConveyor(conveyor, network.getId());
            }
        });

        NetworkService.get().registerClientHandler(PayloadPhase.PLAY, RemoveConveyorNetworkPayload.ID, (payload, _) -> {
            ResourceKey<Level> levelKey = payload.level();
            UUID networkId = payload.networkId();

            ConveyorNetworkManager manager = CONVEYOR_NETWORKS.get(levelKey);
            if (manager == null)
                return;

            Set<ConveyorNetwork> networks = manager.getNetworks();
            ConveyorNetwork network = manager.getNetwork(networkId);
            if (network != null) {
                for (BlockPos conveyor : network.getConveyors()) {
                    manager.removeConveyor(conveyor);
                }
            }

            networks.removeIf(existingNetwork -> existingNetwork.getId().equals(networkId));
        });

        NetworkService.get().registerClientHandler(PayloadPhase.PLAY, ModifyConveyorNetworkPayload.ID, (payload, _) -> {
            ModifyConveyorNetworkPayload.Operation operation = payload.operation();
            ResourceKey<Level> levelKey = payload.level();
            UUID networkId = payload.networkId();
            BlockPos pos = payload.pos();

            ConveyorNetworkManager manager = CONVEYOR_NETWORKS.get(levelKey);
            if (manager == null)
                return;

            Set<ConveyorNetwork> networks = manager.getNetworks();
            Optional<ConveyorNetwork> optionalNetwork = networks.stream()
                    .filter(network -> network.getId().equals(networkId))
                    .findFirst();
            optionalNetwork.ifPresent(network -> {
                switch (operation) {
                    case ADD_CONVEYOR -> {
                        network.addConveyor(pos);
                        manager.addConveyor(pos, networkId);
                    }
                    case REMOVE_CONVEYOR -> {
                        network.removeConveyor(pos);
                        manager.removeConveyor(pos);
                    }
//                    case ADD_CONNECTED_BLOCK -> network.getConnectedBlocks().add(pos);
//                    case REMOVE_CONNECTED_BLOCK -> network.getConnectedBlocks().remove(pos);
                    default ->
                            throw new UnsupportedOperationException("Operation " + operation + " is not supported for ConveyorNetwork yet");
                }
            });
        });

        ClientEvents.onDisconnection(_ -> CONVEYOR_NETWORKS.clear());
    }

    public static ConveyorNetworkManager get(ResourceKey<Level> levelKey) {
        return CONVEYOR_NETWORKS.get(levelKey);
    }
}
