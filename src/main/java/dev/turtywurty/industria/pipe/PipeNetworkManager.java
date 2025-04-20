package dev.turtywurty.industria.pipe;

import com.mojang.serialization.Codec;
import dev.turtywurty.industria.block.PipeBlock;
import dev.turtywurty.industria.init.PipeNetworkManagerInit;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.persistent.WorldPipeNetworks;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// TODO: Splitting output between storages doesn't work
public class PipeNetworkManager<S, N extends PipeNetwork<S>> {
    public static final Codec<PipeNetworkManager<?, ?>> CODEC = PipeNetworkManagerInit.PIPE_NETWORK_MANAGERS.getCodec();
    public static final Codec<List<PipeNetworkManager<?, ?>>> LIST_CODEC = CODEC.listOf();
    public static final PacketCodec<RegistryByteBuf, PipeNetworkManager<?, ?>> PACKET_CODEC = PacketCodecs.registryCodec(CODEC);
    public static final PacketCodec<RegistryByteBuf, List<PipeNetworkManager<?, ?>>> LIST_PACKET_CODEC = PacketCodecs.collection(ArrayList::new, PACKET_CODEC);

    private final TransferType<S, ?, ?> transferType;
    private final PipeNetwork.Factory<S, N> networkSupplier;

    private final Map<RegistryKey<World>, PipeNetworksData<S, N>> pipeNetworksData = new ConcurrentHashMap<>();

    public PipeNetworkManager(TransferType<S, ?, ?> transferType, PipeNetwork.Factory<S, N> networkSupplier) {
        this.transferType = transferType;
        this.networkSupplier = networkSupplier;
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
        return this.pipeNetworksData.computeIfAbsent(dimension, PipeNetworksData::new);
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
        if (networkId == null)
            return;

        N network = pipeNetworksData.getNetwork(networkId);
        if (network == null)
            return;

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

        if (connectedComponents.size() == 1)
            return;

        pipeNetworksData.removeNetwork(network);
        int totalPipes = network.getPipes().size();
        for (Set<BlockPos> connectedComponent : connectedComponents) {
            N newNetwork = this.networkSupplier.create(UUID.randomUUID());
            newNetwork.movePipesFrom(network, connectedComponent);
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

        WorldPipeNetworks.getOrCreate(world).markDirty();
    }

    private void mergeNetworks(ServerWorld world, Map.Entry<BlockPos, N> targetEntry, Map.Entry<BlockPos, N> sourceEntry) {
        PipeNetworksData<S, N> pipeNetworksData = getPipeNetworksData(world);

        N target = targetEntry.getValue();
        N source = sourceEntry.getValue();
        if (target == source || !target.isOfSameType(source)) {
            return;
        }

        target.movePipesFrom(source, source.getPipes());
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
