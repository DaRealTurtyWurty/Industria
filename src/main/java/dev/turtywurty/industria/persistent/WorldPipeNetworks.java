package dev.turtywurty.industria.persistent;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.network.SyncPipeNetworkManagerPayload;
import dev.turtywurty.industria.network.AddPipeNetworkPayload;
import dev.turtywurty.industria.pipe.PipeNetwork;
import dev.turtywurty.industria.pipe.PipeNetworkManager;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.PersistentStateType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class WorldPipeNetworks extends PersistentState {
    public static final Codec<WorldPipeNetworks> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Data.CODEC.fieldOf("data").forGetter(WorldPipeNetworks::getData)
    ).apply(instance, WorldPipeNetworks::new));

    private static final PersistentStateType<WorldPipeNetworks> TYPE = new PersistentStateType<>(
            Industria.MOD_ID + ".pipe_networks",
            WorldPipeNetworks::new,
            CODEC,
            null
    );

    public static WorldPipeNetworks getOrCreate(ServerWorld serverWorld) {
        PersistentStateManager persistentStateManager = serverWorld.getPersistentStateManager();
        return persistentStateManager.getOrCreate(TYPE);
    }

    private final Data data;

    public WorldPipeNetworks() {
        this(new Data());
    }

    public WorldPipeNetworks(Data data) {
        this.data = data;
    }

    public static void syncToClient(PacketSender sender, ServerWorld serverWorld) {
        WorldPipeNetworks worldPipeNetworks = getOrCreate(serverWorld);
        for (PipeNetworkManager<?, ?> manager : worldPipeNetworks.getPipeNetworkManagers()) {
            sender.sendPacket(new SyncPipeNetworkManagerPayload(manager.getTransferType(), manager.getDimension(), manager.getPipeToNetworkId()));
            for (PipeNetwork<?> network : manager.getNetworks()) {
                sender.sendPacket(new AddPipeNetworkPayload(manager.getDimension(), manager.getTransferType(), network));
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

        return null;
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

        public static final PacketCodec<RegistryByteBuf, Data> PACKET_CODEC =
                PacketCodec.tuple(PipeNetworkManager.LIST_PACKET_CODEC, Data::pipeNetworkManagers,
                        Data::new);
    }
}
