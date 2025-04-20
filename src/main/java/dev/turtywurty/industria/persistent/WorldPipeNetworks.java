package dev.turtywurty.industria.persistent;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.pipe.PipeNetwork;
import dev.turtywurty.industria.pipe.PipeNetworkManager;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.PersistentStateType;

import java.util.ArrayList;
import java.util.List;

public class WorldPipeNetworks extends PersistentState {
    public static final Codec<WorldPipeNetworks> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PipeNetworkManager.LIST_CODEC.fieldOf("pipeNetworkManagers").forGetter(WorldPipeNetworks::getPipeNetworkManagers)
    ).apply(instance, WorldPipeNetworks::new));

    public static final PacketCodec<RegistryByteBuf, WorldPipeNetworks> PACKET_CODEC =
            PacketCodec.tuple(PipeNetworkManager.LIST_PACKET_CODEC, WorldPipeNetworks::getPipeNetworkManagers,
                    WorldPipeNetworks::new);

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

    private final List<PipeNetworkManager<?, ?>> pipeNetworkManagers = new ArrayList<>();

    public WorldPipeNetworks() {}

    public WorldPipeNetworks(List<PipeNetworkManager<?, ?>> pipeNetworkManagers) {
        this.pipeNetworkManagers.addAll(pipeNetworkManagers);
    }

    public List<PipeNetworkManager<?, ?>> getPipeNetworkManagers() {
        return this.pipeNetworkManagers;
    }

    public <S, N extends PipeNetwork<S>> PipeNetworkManager<S, N> getNetwork(TransferType<S, ?, ?> transferType) {
        for (PipeNetworkManager<?, ?> pipeNetworkManager : this.pipeNetworkManagers) {
            if (pipeNetworkManager.getTransferType() == transferType) {
                return (PipeNetworkManager<S, N>) pipeNetworkManager;
            }
        }

        return null;
    }
}
