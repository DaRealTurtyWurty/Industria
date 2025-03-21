package dev.turtywurty.industria.pipe;

import dev.turtywurty.fabricslurryapi.api.SlurryVariant;
import dev.turtywurty.heatapi.api.HeatStorage;
import dev.turtywurty.industria.block.PipeBlock;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.network.SyncPipeNetworksPayload;
import dev.turtywurty.industria.persistent.WorldPipeNetworks;
import dev.turtywurty.industria.pipe.impl.CableNetwork;
import dev.turtywurty.industria.pipe.impl.FluidPipeNetwork;
import dev.turtywurty.industria.pipe.impl.HeatPipeNetwork;
import dev.turtywurty.industria.pipe.impl.SlurryPipeNetwork;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PipeNetworkManager<S, N extends PipeNetwork<S>> {
    private static final List<PipeNetworkManager<?, ?>> MANAGERS = new ArrayList<>();

    public static final PipeNetworkManager<Storage<FluidVariant>, FluidPipeNetwork> FLUID = new PipeNetworkManager<>(
            TransferType.FLUID, FluidPipeNetwork::new);

    public static final PipeNetworkManager<Storage<SlurryVariant>, SlurryPipeNetwork> SLURRY = new PipeNetworkManager<>(
            TransferType.SLURRY, SlurryPipeNetwork::new);

    public static final PipeNetworkManager<EnergyStorage, CableNetwork> ENERGY = new PipeNetworkManager<>(
            TransferType.ENERGY, CableNetwork::new);

    public static final PipeNetworkManager<HeatStorage, HeatPipeNetwork> HEAT = new PipeNetworkManager<>(
            TransferType.HEAT, HeatPipeNetwork::new);

    private final Map<RegistryKey<World>, PipeNetworksData<S, N>> pipeNetworksData = new ConcurrentHashMap<>();
    private final TransferType<S, ?, ?> transferType;
    private final PipeNetwork.Factory<S, N> networkSupplier;

    public PipeNetworkManager(TransferType<S, ?, ?> transferType, PipeNetwork.Factory<S, N> networkSupplier) {
        this.transferType = transferType;
        this.networkSupplier = networkSupplier;

        MANAGERS.add(this);
    }

    public static List<PipeNetworkManager<?, ?>> getManagers() {
        return MANAGERS;
    }

    public static void sync(ServerPlayerEntity player) {
        ServerWorld serverWorld = player.getServerWorld();
        for (PipeNetworkManager<?, ?> manager : PipeNetworkManager.getManagers()) {
            PipeNetworksData<?, ?> networksData = manager.getPipeNetworksData(serverWorld);
            String data = networksData.writeNbt(serverWorld.getRegistryManager()).toString();
            byte[] bytes = data.getBytes(StandardCharsets.UTF_8);

            // split into chunks of max size 1.75kb
            int maxChunkSize = 1792;
            int totalChunks = (bytes.length + maxChunkSize - 1) / maxChunkSize;

            UUID packetGroupId = UUID.randomUUID();
            for (int index = 0; index < totalChunks; index++) {
                int chunkSize = Math.min(maxChunkSize, bytes.length - index * maxChunkSize);
                var chunk = new String(bytes, index * maxChunkSize, chunkSize, StandardCharsets.UTF_8);

                ServerPlayNetworking.send(player, new SyncPipeNetworksPayload(
                        manager.getTransferType().getName(),
                        packetGroupId,
                        index,
                        totalChunks,
                        chunk));
            }
        }
    }

    public static void readAllNbt(ServerWorld serverWorld, NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        readAllNbt(serverWorld.getRegistryKey(), nbt, registries);
    }

    public static void readAllNbt(RegistryKey<World> dimension, NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        for (PipeNetworkManager<?, ?> manager : MANAGERS) {
            manager.getPipeNetworksData(dimension).readNbt(nbt.getCompound(manager.transferType.getName()), registries);
        }
    }

    public static void writeAllNbt(ServerWorld serverWorld, NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        writeAllNbt(serverWorld.getRegistryKey(), nbt, registries);
    }

    public static void writeAllNbt(RegistryKey<World> dimension, NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        for (PipeNetworkManager<?, ?> manager : MANAGERS) {
            nbt.put(manager.transferType.getName(), manager.getPipeNetworksData(dimension).writeNbt(registries));
        }
    }

    public void tick(ServerWorld world) {
        for (PipeNetwork<S> network : getPipeNetworksData(world).getNetworks()) {
            network.tick(world);
        }
    }

    public void placePipe(ServerWorld world, BlockPos pos) {
        if (world.isClient)
            return;

        onPlacePipe(world, pos);
        WorldPipeNetworks.getOrCreate(world).markDirty();
    }

    public void removePipe(ServerWorld world, BlockPos pos) {
        if (world.isClient)
            return;

        onRemovePipe(world, pos);
        WorldPipeNetworks.getOrCreate(world).markDirty();
    }

    public PipeNetworksData<S, N> getPipeNetworksData(World world) {
        return getPipeNetworksData(world.getRegistryKey());
    }

    public PipeNetworksData<S, N> getPipeNetworksData(RegistryKey<World> dimension) {
        return this.pipeNetworksData.computeIfAbsent(dimension, key -> new PipeNetworksData<>(key, this.networkSupplier));
    }

    public @Nullable N getNetwork(ServerWorld world, UUID id) {
        return getPipeNetworksData(world).getNetwork(id);
    }

    public @Nullable N getNetwork(World world, BlockPos pos) {
        return getPipeNetworksData(world).getNetwork(pos);
    }

    public TransferType<S, ?, ?> getTransferType() {
        return this.transferType;
    }

    protected void onPlacePipe(ServerWorld world, BlockPos pos) {
        Map<BlockPos, N> adjacentNetworks = new HashMap<>();
        Set<BlockPos> adjacentBlocks = new HashSet<>();

        PipeNetworksData<S, N> pipeNetworksData = getPipeNetworksData(world);

        for (Direction direction : Direction.values()) {
            BlockPos offset = pos.offset(direction);
            if (isPipe(world, offset)) {
                UUID networkId = pipeNetworksData.getNetworkId(offset);
                if (networkId != null) {
                    N network = pipeNetworksData.getNetwork(networkId);
                    if (network != null) {
                        adjacentNetworks.put(offset, network);
                    }
                }
            } else if (transferType.lookup(world, offset, direction.getOpposite()) != null) {
                adjacentBlocks.add(offset);
            }
        }

        Map.Entry<BlockPos, N> networkEntry;
        if (adjacentNetworks.isEmpty()) {
            networkEntry = Map.entry(pos, this.networkSupplier.create(UUID.randomUUID()));
            pipeNetworksData.addNetwork(networkEntry.getValue());
        } else {
            List<Map.Entry<BlockPos, N>> entrySet = adjacentNetworks.entrySet().stream().toList();
            networkEntry = entrySet.getFirst();
            for (int i = 1; i < entrySet.size(); i++) {
                Map.Entry<BlockPos, N> otherNetwork = entrySet.get(i);
                mergeNetworks(world, networkEntry, otherNetwork);
            }
        }

        N network = networkEntry.getValue();
        network.addPipe(pos);
        network.addConnectedBlocks(world, adjacentBlocks);
        pipeNetworksData.addPipe(pos, network.getId());
    }

    protected void onRemovePipe(ServerWorld world, BlockPos pos) {
        PipeNetworksData<S, N> pipeNetworksData = getPipeNetworksData(world);
        UUID networkId = pipeNetworksData.removePipe(pos);
        if (networkId == null) {
            return;
        }

        N network = pipeNetworksData.getNetwork(networkId);
        if (network == null) {
            return;
        }

        network.removePipe(pos);
        if (network.getPipes().isEmpty()) {
            pipeNetworksData.removeNetwork(network);
            return;
        }

        updateConnectedBlocks(world, network);

        Set<BlockPos> visited = new HashSet<>();
        List<Set<BlockPos>> connectedComponents = new ArrayList<>();
        for (BlockPos pipe : network.getPipes()) {
            if (visited.contains(pipe)) {
                continue;
            }

            Set<BlockPos> connectedComponent = new HashSet<>();
            floodFill(world, pipe, visited, connectedComponent);
            if (!connectedComponent.isEmpty()) {
                connectedComponents.add(connectedComponent);
            }
        }

        if (connectedComponents.size() == 1) {
            return;
        }

        pipeNetworksData.removeNetwork(network);
        int totalPipes = network.getPipes().size();
        for (Set<BlockPos> connectedComponent : connectedComponents) {
            N newNetwork = this.networkSupplier.create(UUID.randomUUID());
            newNetwork.inheritPipesFrom(network, connectedComponent);
            updateConnectedBlocks(world, newNetwork);

            if (network.hasCentralStorage()) {
                double fraction = (double) connectedComponent.size() / totalPipes;
                List<BlockPos> adjacentPipes = new ArrayList<>();
                for (Direction direction : Direction.values()) {
                    BlockPos offset = pos.offset(direction);
                    if (isPipe(world, offset) && pipeNetworksData.containsPipe(offset)) {
                        adjacentPipes.add(offset);
                    }
                }

                double newFraction = fraction / adjacentPipes.size();
                for (BlockPos adjacentPipePos : adjacentPipes) {
                    this.transferType.transferFraction(network.getStorage(world, pos),
                            newNetwork.getStorage(world, adjacentPipePos),
                            newFraction);
                }
            }

            pipeNetworksData.addNetwork(newNetwork);
            for (BlockPos pipe : connectedComponent) {
                pipeNetworksData.addPipe(pipe, newNetwork.getId());
            }
        }
    }

    private void mergeNetworks(ServerWorld world, Map.Entry<BlockPos, N> targetEntry, Map.Entry<BlockPos, N> sourceEntry) {
        PipeNetworksData<S, N> pipeNetworksData = getPipeNetworksData(world);

        N target = targetEntry.getValue();
        N source = sourceEntry.getValue();
        if (target == source || !target.isOfSameType(source)) {
            return;
        }

        target.inheritPipesFrom(source, source.getPipes());
        target.addConnectedBlocks(world, source);
        for (BlockPos pipe : source.getPipes()) {
            pipeNetworksData.addPipe(pipe, target.getId());
        }

        if (target.hasCentralStorage()) {
            this.transferType.transferAll(source.getStorage(world, targetEntry.getKey()),
                    target.getStorage(world, targetEntry.getKey()));
        }

        pipeNetworksData.removeNetwork(source);
    }

    private void updateConnectedBlocks(ServerWorld world, N network) {
        network.clearConnectedBlocks(world);

        Set<BlockPos> newConnectedBlocks = new HashSet<>();
        for (BlockPos pipe : network.getPipes()) {
            for (Direction direction : Direction.values()) {
                BlockPos offset = pipe.offset(direction);
                if (!isPipe(world, offset) && transferType.lookup(world, offset, direction.getOpposite()) != null) {
                    newConnectedBlocks.add(offset);
                }
            }
        }

        network.addConnectedBlocks(world, newConnectedBlocks);
    }

    private void floodFill(ServerWorld world, BlockPos pos, Set<BlockPos> visited, Set<BlockPos> connectedComponent) {
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

    protected boolean isPipe(ServerWorld world, BlockPos pos) {
        return world.getBlockState(pos).getBlock() instanceof PipeBlock<?, ?, ?> pipeBlock && pipeBlock.getTransferType() == this.transferType;
    }

    public void traverseCreateNetwork(ServerWorld world, BlockPos pos) {
        PipeNetworksData<S, N> pipeNetworksData = getPipeNetworksData(world);
        if (!isPipe(world, pos) || pipeNetworksData.containsPipe(pos))
            return;

        Set<BlockPos> connectedPipes = new HashSet<>();
        traverseCreateNetwork(world, pos, connectedPipes);

        N network = this.networkSupplier.create(UUID.randomUUID());
        network.getPipes().addAll(connectedPipes);

        updateConnectedBlocks(world, network);

        for (BlockPos connectedPipe : connectedPipes) {
            pipeNetworksData.addPipe(connectedPipe, network.getId());
        }

        pipeNetworksData.addNetwork(network);
    }

    private void traverseCreateNetwork(ServerWorld world, BlockPos pos, Set<BlockPos> visited) {
        if (!isPipe(world, pos) || visited.contains(pos))
            return;

        visited.add(pos);

        for (Direction direction : Direction.values()) {
            BlockPos offset = pos.offset(direction);
            traverseCreateNetwork(world, offset, visited);
        }
    }
}
