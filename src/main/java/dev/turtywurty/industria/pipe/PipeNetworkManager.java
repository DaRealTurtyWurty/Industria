package dev.turtywurty.industria.pipe;

import dev.turtywurty.fabricslurryapi.api.SlurryVariant;
import dev.turtywurty.heatapi.api.HeatStorage;
import dev.turtywurty.industria.block.PipeBlock;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.persistent.WorldPipeNetworks;
import dev.turtywurty.industria.pipe.impl.CableNetwork;
import dev.turtywurty.industria.pipe.impl.FluidPipeNetwork;
import dev.turtywurty.industria.pipe.impl.HeatPipeNetwork;
import dev.turtywurty.industria.pipe.impl.SlurryPipeNetwork;
import dev.turtywurty.industria.util.NBTSerializable;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class PipeNetworkManager<S> implements NBTSerializable<NbtCompound> {
    private final Set<PipeNetwork<S>> networks = ConcurrentHashMap.newKeySet();
    private final Map<BlockPos, UUID> pipeToNetworkId = new HashMap<>();
    private final TransferType<S, ?> transferType;
    private final Function<UUID, PipeNetwork<S>> networkSupplier;

    public static final PipeNetworkManager<Storage<FluidVariant>> FLUID = new PipeNetworkManager<>(
            TransferType.FLUID, FluidPipeNetwork::new);

    public static final PipeNetworkManager<Storage<SlurryVariant>> SLURRY = new PipeNetworkManager<>(
            TransferType.SLURRY, SlurryPipeNetwork::new);

    public static final PipeNetworkManager<EnergyStorage> ENERGY = new PipeNetworkManager<>(
            TransferType.ENERGY, CableNetwork::new);

    public static final PipeNetworkManager<HeatStorage> HEAT = new PipeNetworkManager<>(
            TransferType.HEAT, HeatPipeNetwork::new);

    public PipeNetworkManager(TransferType<S, ?> transferType, Function<UUID, PipeNetwork<S>> networkSupplier) {
        this.transferType = transferType;
        this.networkSupplier = networkSupplier;
    }

    public void tick(World world) {
        for (PipeNetwork<S> network : this.networks) {
            network.tick(world);
        }
    }

    public void placePipe(World world, BlockPos pos) {
        if(world.isClient)
            return;

        onPlacePipe(world, pos);

        if(world instanceof ServerWorld serverWorld) {
            WorldPipeNetworks.getOrCreate(serverWorld).markDirty();
        }
    }

    public void removePipe(World world, BlockPos pos) {
        if(world.isClient)
            return;

        onRemovePipe(world, pos);

        if(world instanceof ServerWorld serverWorld) {
            WorldPipeNetworks.getOrCreate(serverWorld).markDirty();
        }
    }

    public @Nullable PipeNetwork<S> getNetwork(UUID id) {
        for (PipeNetwork<S> network : this.networks) {
            if (network.getId().equals(id)) {
                return network;
            }
        }

        return null;
    }

    // TODO: Make this world dependent
    public @Nullable PipeNetwork<S> getNetwork(World world, BlockPos pos) {
        UUID networkId = this.pipeToNetworkId.get(pos);
        if (networkId == null)
            return null;

        return getNetwork(networkId);
    }

    @Override
    public NbtCompound writeNbt(RegistryWrapper.WrapperLookup registries) {
        var manager = new NbtCompound();

        var networks = new NbtCompound();
        for (PipeNetwork<S> network : this.networks) {
            networks.put(network.getId().toString(), network.writeNbt(registries));
        }

        manager.put("Networks", networks);

        var pipeToNetwork = new NbtList();
        for (Map.Entry<BlockPos, UUID> entry : this.pipeToNetworkId.entrySet()) {
            var pipe = new NbtCompound();
            pipe.putLong("Pos", entry.getKey().asLong());
            pipe.putString("Network", entry.getValue().toString());
            pipeToNetwork.add(pipe);
        }

        manager.put("PipeToNetwork", pipeToNetwork);

        return manager;
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        this.networks.clear();
        this.pipeToNetworkId.clear();

        NbtCompound networks = nbt.getCompound("Networks");
        for (String key : networks.getKeys()) {
            UUID id = UUID.fromString(key);
            PipeNetwork<S> network = this.networkSupplier.apply(id);
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

    protected void onPlacePipe(World world, BlockPos pos) {
        List<PipeNetwork<S>> adjacentNetworks = new ArrayList<>();
        Set<BlockPos> adjacentBlocks = new HashSet<>();

        for (Direction direction : Direction.values()) {
            BlockPos offset = pos.offset(direction);
            if (isPipe(world, offset)) {
                UUID networkId = this.pipeToNetworkId.get(offset);
                if (networkId != null) {
                    PipeNetwork<S> network = getNetwork(networkId);
                    if (network != null) {
                        adjacentNetworks.add(network);
                    }
                }
            } else if (transferType.lookup(world, offset, direction.getOpposite()) != null) {
                adjacentBlocks.add(offset);
            }
        }

        PipeNetwork<S> network;
        if (adjacentNetworks.isEmpty()) {
            network = this.networkSupplier.apply(UUID.randomUUID());
            this.networks.add(network);
        } else {
            network = adjacentNetworks.getFirst();
            for (int i = 1; i < adjacentNetworks.size(); i++) {
                PipeNetwork<S> otherNetwork = adjacentNetworks.get(i);
                mergeNetworks(network, otherNetwork);
            }
        }

        network.getPipes().add(pos);
        network.getConnectedBlocks().addAll(adjacentBlocks);
        this.pipeToNetworkId.put(pos, network.getId());
    }

    protected void onRemovePipe(World world, BlockPos pos) {
        UUID networkId = this.pipeToNetworkId.remove(pos);
        if (networkId == null)
            return;

        PipeNetwork<S> network = getNetwork(networkId);
        if (network == null)
            return;

        network.getPipes().remove(pos);
        if (network.getPipes().isEmpty()) {
            this.networks.remove(network);
            return;
        }

        updateConnectedBlocks(world, network);

        Set<BlockPos> visited = new HashSet<>();
        List<Set<BlockPos>> connectedComponents = new ArrayList<>();
        for (BlockPos pipe : network.getPipes()) {
            if (visited.contains(pipe))
                continue;

            Set<BlockPos> connectedComponent = new HashSet<>();
            floodFill(world, pipe, visited, connectedComponent);
            if (!connectedComponent.isEmpty()) {
                connectedComponents.add(connectedComponent);
            }
        }

        if (connectedComponents.size() == 1)
            return;

        this.networks.remove(network);
        int totalPipes = network.getPipes().size();
        for (Set<BlockPos> connectedComponent : connectedComponents) {
            PipeNetwork<S> newNetwork = this.networkSupplier.apply(UUID.randomUUID());
            newNetwork.getPipes().addAll(connectedComponent);
            updateConnectedBlocks(world, newNetwork);

            double fraction = (double) connectedComponent.size() / totalPipes;
            this.transferType.transferFraction(network.getStorage(), newNetwork.getStorage(), fraction);

            this.networks.add(newNetwork);
            for (BlockPos pipe : connectedComponent) {
                this.pipeToNetworkId.put(pipe, newNetwork.getId());
            }
        }
    }

    private void mergeNetworks(PipeNetwork<S> target, PipeNetwork<S> source) {
        if (target == source)
            return;

        if (!target.isOfSameType(source))
            return;

        target.getPipes().addAll(source.getPipes());
        target.getConnectedBlocks().addAll(source.getConnectedBlocks());
        for (BlockPos pipe : source.getPipes()) {
            this.pipeToNetworkId.put(pipe, target.getId());
        }

        this.transferType.transferAll(source.getStorage(), target.getStorage());

        this.networks.remove(source);
    }

    private void updateConnectedBlocks(World world, PipeNetwork<S> network) {
        network.getConnectedBlocks().clear();

        Set<BlockPos> newConnectedBlocks = new HashSet<>();
        for (BlockPos pipe : network.getPipes()) {
            for (Direction direction : Direction.values()) {
                BlockPos offset = pipe.offset(direction);
                if (!isPipe(world, offset) && transferType.lookup(world, offset, direction.getOpposite()) != null) {
                    newConnectedBlocks.add(offset);
                }
            }
        }

        network.getConnectedBlocks().addAll(newConnectedBlocks);
    }

    private void floodFill(World world, BlockPos pos, Set<BlockPos> visited, Set<BlockPos> connectedComponent) {
        Queue<BlockPos> queue = new LinkedList<>();
        queue.add(pos);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            if (!visited.add(current) || !isPipe(world, current))
                continue;

            connectedComponent.add(current);
            for (Direction direction : Direction.values()) {
                BlockPos offset = current.offset(direction);
                if (isPipe(world, offset) && !visited.contains(offset)) {
                    queue.add(offset);
                }
            }
        }
    }

    protected boolean isPipe(World world, BlockPos pos) {
        return world.getBlockState(pos).getBlock() instanceof PipeBlock<?> pipeBlock && pipeBlock.getTransferType() == this.transferType;
    }

    public void traverseCreateNetwork(World world, BlockPos pos) {
        if(!isPipe(world, pos) || this.pipeToNetworkId.containsKey(pos))
            return;

        Set<BlockPos> connectedPipes = new HashSet<>();
        traverseCreateNetwork(world, pos, connectedPipes);

        PipeNetwork<S> network = this.networkSupplier.apply(UUID.randomUUID());
        network.getPipes().addAll(connectedPipes);

        updateConnectedBlocks(world, network);

        for (BlockPos connectedPipe : connectedPipes) {
            this.pipeToNetworkId.put(connectedPipe, network.getId());
        }

        this.networks.add(network);
    }

    private void traverseCreateNetwork(World world, BlockPos pos, Set<BlockPos> visited) {
        if(!isPipe(world, pos) || visited.contains(pos))
            return;

        visited.add(pos);

        for (Direction direction : Direction.values()) {
            BlockPos offset = pos.offset(direction);
            traverseCreateNetwork(world, offset, visited);
        }
    }
}
