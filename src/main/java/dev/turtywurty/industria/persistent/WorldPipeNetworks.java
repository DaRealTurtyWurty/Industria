package dev.turtywurty.industria.persistent;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.init.PipeNetworkManagerTypeInit;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.network.AddPipeNetworkPayload;
import dev.turtywurty.industria.network.SyncPipeNetworkManagerPayload;
import dev.turtywurty.industria.pipe.PipeNetwork;
import dev.turtywurty.industria.pipe.PipeNetworkManager;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class WorldPipeNetworks extends SavedData {
    public static final Codec<WorldPipeNetworks> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Data.CODEC.fieldOf("data").forGetter(WorldPipeNetworks::getData)
    ).apply(instance, WorldPipeNetworks::new));

    private static final SavedDataType<WorldPipeNetworks> TYPE = new SavedDataType<>(
            Industria.MOD_ID + ".pipe_networks",
            WorldPipeNetworks::new,
            CODEC,
            null
    );

    public static WorldPipeNetworks getOrCreate(ServerLevel serverWorld) {
        DimensionDataStorage persistentStateManager = serverWorld.getDataStorage();
        return persistentStateManager.computeIfAbsent(TYPE);
    }

    private final Data data;

    public WorldPipeNetworks() {
        this(new Data());
    }

    public WorldPipeNetworks(Data data) {
        this.data = data;
    }

    public static void syncToClient(PacketSender sender, ServerLevel serverWorld) {
        WorldPipeNetworks worldPipeNetworks = getOrCreate(serverWorld);
        for (PipeNetworkManager<?, ?> manager : worldPipeNetworks.getPipeNetworkManagers()) {
            sender.sendPacket(new SyncPipeNetworkManagerPayload(manager.getTransferType(), serverWorld.dimension(), manager.getPipeToNetworkId()));
            for (PipeNetwork<?> network : manager.getNetworks()) {
                sender.sendPacket(new AddPipeNetworkPayload(serverWorld.dimension(), manager.getTransferType(), network));
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
    public <S, N extends PipeNetwork<S>> PipeNetworkManager<S, N> getNetworkManager(TransferType<S, ?, ?> transferType) {
        for (PipeNetworkManager<?, ?> pipeNetworkManager : getPipeNetworkManagers()) {
            if (pipeNetworkManager.getTransferType() == transferType) {
                return (PipeNetworkManager<S, N>) pipeNetworkManager;
            }
        }

        PipeNetworkManager<S, N> manager = PipeNetworkManagerTypeInit.<S, N>getType(transferType).factory().get();
        this.data.pipeNetworkManagers.add(manager);
        return manager;
    }

    public <S, N extends PipeNetwork<S>> @Nullable N getNetwork(TransferType<S, ?, ?> transferType, BlockPos pos) {
        PipeNetworkManager<S, N> pipeNetworkManager = getNetworkManager(transferType);
        if (pipeNetworkManager != null) {
            return pipeNetworkManager.getNetwork(pos);
        }

        return null;
    }

    public <S, N extends PipeNetwork<S>> @Nullable S getStorage(TransferType<S, ?, ?> transferType, BlockPos pos) {
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
