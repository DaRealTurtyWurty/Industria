package dev.turtywurty.industria.persistent;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.init.ModPipeNetworkManagerTypes;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.network.pipe.AddPipeNetworkPayload;
import dev.turtywurty.industria.network.pipe.SyncPipeNetworkManagerPayload;
import dev.turtywurty.industria.pipe.PipeNetwork;
import dev.turtywurty.industria.pipe.PipeNetworkManager;
import dev.turtywurty.turtymultiloader.network.NetworkService;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import dev.turtywurty.turtymultiloader.transfer.storage.ResourceStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.SavedDataStorage;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class WorldPipeNetworks extends SavedData {
    private static final NetworkService NETWORK = NetworkService.get();

    public static final Codec<WorldPipeNetworks> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Data.CODEC.fieldOf("data").forGetter(WorldPipeNetworks::getData)
    ).apply(instance, WorldPipeNetworks::new));

    private static final SavedDataType<WorldPipeNetworks> TYPE = new SavedDataType<>(
            Industria.id("pipe_networks"),
            WorldPipeNetworks::new,
            CODEC,
            null
    );

    public static WorldPipeNetworks getOrCreate(ServerLevel serverWorld) {
        SavedDataStorage persistentStateManager = serverWorld.getDataStorage();
        return persistentStateManager.computeIfAbsent(TYPE);
    }

    private final Data data;

    public WorldPipeNetworks() {
        this(new Data());
    }

    public WorldPipeNetworks(Data data) {
        this.data = data;
    }

    public static void syncToClient(ServerPlayer player) {
        ServerLevel serverWorld = player.level();
        WorldPipeNetworks worldPipeNetworks = getOrCreate(serverWorld);
        for (PipeNetworkManager<?, ?> manager : worldPipeNetworks.getPipeNetworkManagers()) {
            NETWORK.sendToPlayer(player, new SyncPipeNetworkManagerPayload(
                    manager.getTransferType(), serverWorld.dimension(), manager.getPipeToNetworkId()));
            for (PipeNetwork<?> network : manager.getNetworks()) {
                NETWORK.sendToPlayer(player, new AddPipeNetworkPayload(
                        serverWorld.dimension(), manager.getTransferType(), network));
            }
        }
    }

    public List<PipeNetworkManager<?, ?>> getPipeNetworkManagers() {
        return this.data.pipeNetworkManagers;
    }

    public Data getData() {
        return this.data;
    }

    @SuppressWarnings("unchecked")
    public <V extends ResourceVariant<?>, N extends PipeNetwork<V>> PipeNetworkManager<V, N> getNetworkManager(
            TransferType<ResourceStorage<V>, V, Long> transferType) {
        for (PipeNetworkManager<?, ?> pipeNetworkManager : getPipeNetworkManagers()) {
            if (transferType.equals(pipeNetworkManager.getTransferType())) {
                return (PipeNetworkManager<V, N>) pipeNetworkManager;
            }
        }

        PipeNetworkManager<V, N> manager = ModPipeNetworkManagerTypes.<V, N>getType(transferType).factory().get();
        this.data.pipeNetworkManagers.add(manager);
        return manager;
    }

    public <V extends ResourceVariant<?>, N extends PipeNetwork<V>> @Nullable N getNetwork(
            TransferType<ResourceStorage<V>, V, Long> transferType, BlockPos pos) {
        PipeNetworkManager<V, N> pipeNetworkManager = getNetworkManager(transferType);
        if (pipeNetworkManager != null) {
            return pipeNetworkManager.getNetwork(pos);
        }

        return null;
    }

    public <V extends ResourceVariant<?>, N extends PipeNetwork<V>> @Nullable ResourceStorage<V> getStorage(
            TransferType<ResourceStorage<V>, V, Long> transferType, BlockPos pos) {
        N network = getNetwork(transferType, pos);
        if (network != null) {
            return network.getStorage(pos);
        }

        return null;
    }

    public record Data(List<PipeNetworkManager<?, ?>> pipeNetworkManagers) {
        public Data() {
            this(new ArrayList<>());
        }

        public static final Codec<Data> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                PipeNetworkManager.LIST_CODEC.fieldOf("pipeNetworkManagers").forGetter(Data::pipeNetworkManagers)
        ).apply(instance, Data::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, Data> STREAM_CODEC =
                StreamCodec.composite(PipeNetworkManager.LIST_STREAM_CODEC, Data::pipeNetworkManagers, Data::new);
    }
}
