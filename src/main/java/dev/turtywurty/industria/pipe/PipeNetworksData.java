package dev.turtywurty.industria.pipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PipeNetworksData<S, N extends PipeNetwork<S>> {
    public static final Codec<PipeNetworksData<?, PipeNetwork<?>>> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    RegistryKey.createCodec(RegistryKeys.WORLD).fieldOf("dimension").forGetter(PipeNetworksData::getDimension),
                    PipeNetwork.SET_CODEC.fieldOf("networks").forGetter(PipeNetworksData::getNetworks),
                    Codec.unboundedMap(BlockPos.CODEC, Uuids.CODEC).fieldOf("pipes").forGetter(PipeNetworksData::getPipes)
            ).apply(instance, (dimension, networks, pipeToNetworkId) -> {
                PipeNetworksData<?, ?> networkData = createPipeNetworksData(dimension, networks, pipeToNetworkId);
                // noinspection unchecked
                return (PipeNetworksData<?, PipeNetwork<?>>) networkData;
            }));
    public static final PacketCodec<RegistryByteBuf, PipeNetworksData<?, PipeNetwork<?>>> PACKET_CODEC =
            PacketCodec.tuple(
                    RegistryKey.createPacketCodec(RegistryKeys.WORLD), PipeNetworksData::getDimension,
                    PipeNetwork.SET_PACKET_CODEC, PipeNetworksData::getNetworks,
                    PacketCodecs.map(HashMap::new, BlockPos.PACKET_CODEC, Uuids.PACKET_CODEC), PipeNetworksData::getPipes,
                    (dimension, networks, pipeToNetworkIds) -> {
                        PipeNetworksData<?, ?> networkData = createPipeNetworksData(dimension, networks, pipeToNetworkIds);
                        // noinspection unchecked
                        return (PipeNetworksData<?, PipeNetwork<?>>) networkData;
                    });

    private final RegistryKey<World> dimension;
    private final Set<N> networks = ConcurrentHashMap.newKeySet();
    private final Map<BlockPos, UUID> pipeToNetworkId = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public static <S, N extends PipeNetwork<S>> PipeNetworksData<S, N> createPipeNetworksData(RegistryKey<World> dimension, Set<PipeNetwork<?>> networks, Map<BlockPos, UUID> pipeToNetworkId) {
        PipeNetworksData<S, N> data = new PipeNetworksData<>(dimension);
        data.networks.addAll((Collection<? extends N>) networks);
        data.pipeToNetworkId.putAll(pipeToNetworkId);
        return data;
    }

    public PipeNetworksData(RegistryKey<World> dimension) {
        this.dimension = dimension;
    }

    public void addNetwork(N network) {
        this.networks.add(network);
    }

    public void removeNetwork(N network) {
        this.networks.remove(network);
    }

    public void addPipe(BlockPos pos, UUID networkId) {
        this.pipeToNetworkId.put(pos, networkId);
    }

    public UUID removePipe(BlockPos pos) {
        return this.pipeToNetworkId.remove(pos);
    }

    public boolean containsPipe(BlockPos pos) {
        return this.pipeToNetworkId.containsKey(pos);
    }

    public N getNetwork(UUID id) {
        return this.networks.stream().filter(network -> network.getId().equals(id)).findFirst().orElse(null);
    }

    public N getNetwork(BlockPos pos) {
        return getNetwork(this.pipeToNetworkId.get(pos));
    }

    public UUID getNetworkId(BlockPos pos) {
        return this.pipeToNetworkId.get(pos);
    }

    public Set<N> getNetworks() {
        return this.networks;
    }

    public RegistryKey<World> getDimension() {
        return this.dimension;
    }

    public Map<BlockPos, UUID> getPipes() {
        return this.pipeToNetworkId;
    }

    public void clear() {
        this.networks.clear();
        this.pipeToNetworkId.clear();
    }
}
