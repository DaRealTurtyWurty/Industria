package dev.turtywurty.industria.pipe;

import dev.turtywurty.industria.util.NBTSerializable;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PipeNetworksData<S, N extends PipeNetwork<S>> implements NBTSerializable<NbtCompound> {
    private final RegistryKey<World> dimension;
    private final Set<N> networks = ConcurrentHashMap.newKeySet();
    private final Map<BlockPos, UUID> pipeToNetworkId = new ConcurrentHashMap<>();

    private final PipeNetwork.Factory<S, N> networkSupplier;

    public PipeNetworksData(RegistryKey<World> dimension, PipeNetwork.Factory<S, N> networkSupplier) {
        this.dimension = dimension;
        this.networkSupplier = networkSupplier;
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

    public void clear() {
        this.networks.clear();
        this.pipeToNetworkId.clear();
    }

    @Override
    public NbtCompound writeNbt(RegistryWrapper.WrapperLookup registries) {
        var manager = new NbtCompound();

        var networksNbt = new NbtCompound();
        for (PipeNetwork<S> network : this.networks) {
            networksNbt.put(network.getId().toString(), network.writeNbt(registries));
        }

        manager.put("Networks", networksNbt);

        var pipeToNetworkNbtList = new NbtList();
        for (Map.Entry<BlockPos, UUID> entry : this.pipeToNetworkId.entrySet()) {
            var pipe = new NbtCompound();
            pipe.putLong("Pos", entry.getKey().asLong());
            pipe.putString("Network", entry.getValue().toString());
            pipeToNetworkNbtList.add(pipe);
        }

        manager.put("PipeToNetwork", pipeToNetworkNbtList);

        return manager;
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        this.networks.clear();
        this.pipeToNetworkId.clear();

        NbtCompound networks = nbt.getCompound("Networks");
        for (String key : networks.getKeys()) {
            UUID id = UUID.fromString(key);
            N network = this.networkSupplier.create(id);
            network.readNbt(networks.getCompound(key), registries);
            this.networks.add(network);
        }

        NbtList pipeToNetwork = nbt.getList("PipeToNetwork", NbtCompound.COMPOUND_TYPE);
        for (int i = 0; i < pipeToNetwork.size(); i++) {
            NbtCompound pipe = pipeToNetwork.getCompound(i);
            BlockPos pos = BlockPos.fromLong(pipe.getLong("Pos"));
            UUID network = UUID.fromString(pipe.getString("Network"));
            this.pipeToNetworkId.put(pos, network);
        }
    }

    public boolean containsPipe(BlockPos pos) {
        return this.pipeToNetworkId.containsKey(pos);
    }
}
