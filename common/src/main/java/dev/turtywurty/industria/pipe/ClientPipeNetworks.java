package dev.turtywurty.industria.pipe;

import dev.turtywurty.industria.init.ModPipeNetworkManagerTypes;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.network.pipe.AddPipeNetworkPayload;
import dev.turtywurty.industria.network.pipe.ModifyPipeNetworkPayload;
import dev.turtywurty.industria.network.pipe.RemovePipeNetworkPayload;
import dev.turtywurty.industria.network.pipe.SyncPipeNetworkManagerPayload;
import dev.turtywurty.turtymultiloader.network.NetworkService;
import dev.turtywurty.turtymultiloader.network.PayloadPhase;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ClientPipeNetworks {
    private static final NetworkService NETWORK = NetworkService.get();
    private static final Map<ResourceKey<Level>, List<PipeNetworkManager<?, ?>>> PIPE_NETWORKS =
            new ConcurrentHashMap<>();

    private ClientPipeNetworks() {
    }

    public static void init() {
        NETWORK.registerClientHandler(PayloadPhase.PLAY, SyncPipeNetworkManagerPayload.ID,
                (payload, _) -> syncManager(payload));
        NETWORK.registerClientHandler(PayloadPhase.PLAY, AddPipeNetworkPayload.ID,
                (payload, _) -> addNetwork(payload));
        NETWORK.registerClientHandler(PayloadPhase.PLAY, RemovePipeNetworkPayload.ID,
                (payload, _) -> removeNetwork(payload));
        NETWORK.registerClientHandler(PayloadPhase.PLAY, ModifyPipeNetworkPayload.ID,
                (payload, _) -> modifyNetwork(payload));
        NETWORK.onClientDisconnect(PIPE_NETWORKS::clear);
    }

    private static void syncManager(SyncPipeNetworkManagerPayload payload) {
        ResourceKey<Level> worldKey = payload.dimension();
        TransferType<?, ?, ?> transferType = payload.transferType();
        List<PipeNetworkManager<?, ?>> managers =
                PIPE_NETWORKS.computeIfAbsent(worldKey, ignored -> new ArrayList<>());
        managers.removeIf(manager -> transferType.equals(manager.getTransferType()));

        PipeNetworkManagerType<?, ?> type = ModPipeNetworkManagerTypes.getTypeUnchecked(transferType);
        PipeNetworkManager<?, ?> manager = type.factory().get();
        manager.getPipeToNetworkId().putAll(payload.pipeToNetworkId());
        managers.add(manager);
    }

    private static void addNetwork(AddPipeNetworkPayload payload) {
        PipeNetworkManager<?, ?> manager = findManager(payload.world(), payload.transferType());
        if (manager != null) {
            addNetworkUnchecked(manager, payload.network());
        }
    }

    private static void removeNetwork(RemovePipeNetworkPayload payload) {
        PipeNetworkManager<?, ?> manager = findManager(payload.world(), payload.transferType());
        if (manager != null) {
            manager.getNetworks().removeIf(network -> network.getId().equals(payload.networkId()));
        }
    }

    private static void modifyNetwork(ModifyPipeNetworkPayload payload) {
        PipeNetworkManager<?, ?> manager = findManager(payload.world(), payload.transferType());
        if (manager == null)
            return;

        Optional<? extends PipeNetwork<?>> network = manager.getNetworks().stream()
                .filter(candidate -> candidate.getId().equals(payload.networkId()))
                .findFirst();
        network.ifPresent(value -> {
            BlockPos pos = payload.pos();
            switch (payload.operation()) {
                case ADD_PIPE -> value.addPipe(pos);
                case REMOVE_PIPE -> value.removePipe(pos);
                case ADD_CONNECTED_BLOCK -> value.getConnectedBlocks().add(pos);
                case REMOVE_CONNECTED_BLOCK -> value.getConnectedBlocks().remove(pos);
            }
        });
    }

    private static PipeNetworkManager<?, ?> findManager(
            ResourceKey<Level> worldKey,
            TransferType<?, ?, ?> transferType) {
        List<PipeNetworkManager<?, ?>> managers = PIPE_NETWORKS.get(worldKey);
        if (managers == null)
            return null;

        return managers.stream()
                .filter(manager -> transferType.equals(manager.getTransferType()))
                .findFirst()
                .orElse(null);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void addNetworkUnchecked(PipeNetworkManager manager, PipeNetwork network) {
        manager.getNetworks().add(network);
    }

    public static List<PipeNetworkManager<?, ?>> get(ResourceKey<Level> worldKey) {
        return PIPE_NETWORKS.get(worldKey);
    }
}
