package dev.turtywurty.industria.conveyor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.turtywurty.industria.conveyor.block.ConveyorLike;
import dev.turtywurty.industria.conveyor.block.ConveyorOutput;
import dev.turtywurty.industria.conveyor.block.ConveyorRoutingState;
import dev.turtywurty.industria.conveyor.block.ConveyorTopology;
import dev.turtywurty.industria.init.list.TagList;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.network.conveyor.AddConveyorNetworkPayload;
import dev.turtywurty.industria.network.conveyor.RemoveConveyorNetworkPayload;
import dev.turtywurty.industria.persistent.LevelConveyorNetworks;
import dev.turtywurty.industria.util.ExtraCodecs;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ConveyorNetworkManager implements ConveyorRoutingState {
    private static final MapCodec<Map<BlockPos, Integer>> ROUND_ROBIN_INDICES_CODEC =
            Codec.unboundedMap(ExtraCodecs.BLOCK_POS_STRING_CODEC, Codec.INT).optionalFieldOf("round_robin_indices", Map.of());
    public static final MapCodec<ConveyorNetworkManager> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ExtraCodecs.setOf(ConveyorNetwork.CODEC).fieldOf("networks").forGetter(ConveyorNetworkManager::getNetworks),
            ExtraCodecs.BLOCK_POS_TO_UUID_CODEC.fieldOf("conveyor_to_network_id").forGetter(ConveyorNetworkManager::getConveyorToNetworkId),
            ROUND_ROBIN_INDICES_CODEC.forGetter(ConveyorNetworkManager::getRoundRobinIndices)
    ).apply(instance, ConveyorNetworkManager::new));
    protected final Set<ConveyorNetwork> networks = ConcurrentHashMap.newKeySet();
    protected final Map<BlockPos, UUID> conveyorToNetworkId = new ConcurrentHashMap<>();
    protected final Map<BlockPos, Integer> roundRobinIndices = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> networkStorageHashes = new ConcurrentHashMap<>();
    private volatile boolean routingStateChanged;

    public ConveyorNetworkManager() {
    }

    public ConveyorNetworkManager(Set<ConveyorNetwork> networks, Map<BlockPos, UUID> conveyorToNetworkId, Map<BlockPos, Integer> roundRobinIndices) {
        this.networks.addAll(networks);
        this.conveyorToNetworkId.putAll(conveyorToNetworkId);
        this.roundRobinIndices.putAll(roundRobinIndices);
    }

    public Set<ConveyorNetwork> getNetworks() {
        return networks;
    }

    @Nullable
    public ConveyorNetwork getNetwork(UUID id) {
        return networks.stream()
                .filter(network -> network.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Nullable
    public UUID getNetworkId(BlockPos pos) {
        return conveyorToNetworkId.get(pos);
    }

    @Nullable
    public ConveyorNetwork getNetworkAt(BlockPos pos) {
        UUID networkId = getNetworkId(pos);
        return networkId != null ? getNetwork(networkId) : null;
    }

    public void addNetwork(ConveyorNetwork network) {
        this.networks.add(network);
    }

    public void removeNetwork(ConveyorNetwork network) {
        this.networks.remove(network);
        this.networkStorageHashes.remove(network.getId());
    }

    public void addConveyor(BlockPos pos, ConveyorNetwork network) {
        addConveyor(pos, network.getId());
    }

    public void addConveyor(BlockPos pos, UUID networkId) {
        this.conveyorToNetworkId.put(pos, networkId);
    }

    public UUID removeConveyor(BlockPos pos) {
        this.roundRobinIndices.remove(pos);
        return this.conveyorToNetworkId.remove(pos);
    }

    public boolean hasConveyor(BlockPos pos) {
        return this.conveyorToNetworkId.containsKey(pos);
    }

    public void clear() {
        this.networks.clear();
        this.conveyorToNetworkId.clear();
        this.roundRobinIndices.clear();
        this.networkStorageHashes.clear();
        this.routingStateChanged = false;
    }

    public void syncNetwork(ServerLevel world, ConveyorNetwork network) {
        int storageHash = network.getStorageStateHash();
        this.networkStorageHashes.put(network.getId(), storageHash);
        syncAndSave(world, List.of(new AddConveyorNetworkPayload(world.dimension(), network)));
    }

    public void tick(ServerLevel level) {
        this.routingStateChanged = false;
        List<CustomPacketPayload> payloads = new ArrayList<>();
        Set<UUID> activeNetworkIds = new HashSet<>();

        for (ConveyorNetwork network : networks) {
            boolean orderChanged = reorderConveyors(level, network, new ArrayList<>(network.getConveyors()));
            boolean connectedBlocksChanged = updateConnectedBlocks(level, network);
            network.tick(level, this);

            UUID networkId = network.getId();
            activeNetworkIds.add(networkId);

            int storageHash = network.getStorageStateHash();
            Integer previousHash = this.networkStorageHashes.put(networkId, storageHash);
            if (orderChanged || connectedBlocksChanged || previousHash == null || previousHash != storageHash) {
                payloads.add(new AddConveyorNetworkPayload(level.dimension(), network));
            }
        }

        this.networkStorageHashes.keySet().removeIf(networkId -> !activeNetworkIds.contains(networkId));
        if (!payloads.isEmpty()) {
            syncAndSave(level, payloads);
        } else if (this.routingStateChanged) {
            LevelConveyorNetworks.getOrCreate(level).setDirty();
        }
    }

    public void placeConveyor(ServerLevel world, BlockPos pos) {
        if (world.isClientSide())
            return;

        onPlaceConveyor(world, pos);
    }

    public void removeConveyor(ServerLevel world, BlockPos pos) {
        if (world.isClientSide())
            return;

        onRemoveConveyor(world, pos);
    }

    public boolean recreateNetworkAt(ServerLevel world, BlockPos pos) {
        if (world.isClientSide() || !isConveyor(world, pos))
            return false;

        Set<BlockPos> connectedConveyors = new LinkedHashSet<>();
        floodFill(world, pos, new HashSet<>(), connectedConveyors);
        if (connectedConveyors.isEmpty())
            return false;

        Map<BlockPos, ConveyorStorage> preservedStorages = new HashMap<>();
        Map<BlockPos, Integer> preservedRoutingState = snapshotRoutingState(connectedConveyors);
        for (BlockPos conveyorPos : connectedConveyors) {
            UUID networkId = getNetworkId(conveyorPos);
            if (networkId == null)
                continue;

            ConveyorNetwork network = getNetwork(networkId);
            if (network == null)
                continue;

            ConveyorStorage storage = network.getStorage().getStorages().remove(conveyorPos);
            if (storage != null) {
                preservedStorages.put(conveyorPos, storage);
            }
        }

        for (BlockPos conveyorPos : connectedConveyors) {
            if (hasConveyor(conveyorPos)) {
                onRemoveConveyor(world, conveyorPos);
            }
        }

        traverseCreateNetwork(world, pos);

        ConveyorNetwork recreatedNetwork = getNetworkAt(pos);
        if (recreatedNetwork == null)
            return false;

        recreatedNetwork.getStorage().getStorages().putAll(preservedStorages);
        restoreRoutingState(recreatedNetwork.getConveyors(), preservedRoutingState);
        reorderConveyors(world, recreatedNetwork, new ArrayList<>(recreatedNetwork.getConveyors()));
        updateConnectedBlocks(world, recreatedNetwork);
        syncNetwork(world, recreatedNetwork);
        return true;
    }

    protected void onPlaceConveyor(ServerLevel world, BlockPos pos) {
        Map<ConveyorNetwork, BlockPos> adjacentNetworks = new LinkedHashMap<>();

        for (BlockPos neighbor : getConnectedConveyorNeighbors(world, pos)) {
            UUID networkId = getNetworkId(neighbor);
            if (networkId == null)
                continue;

            ConveyorNetwork network = getNetwork(networkId);
            if (network != null) {
                adjacentNetworks.putIfAbsent(network, neighbor);
            }
        }

        ConveyorNetwork network;
        boolean isNewNetwork = false;
        List<ConveyorNetwork> mergedNetworks = new ArrayList<>();
        List<BlockPos> preferredOrder = new ArrayList<>();

        if (adjacentNetworks.isEmpty()) {
            network = new ConveyorNetwork();
            addNetwork(network);
            isNewNetwork = true;
        } else {
            List<Map.Entry<ConveyorNetwork, BlockPos>> entrySet = new ArrayList<>(adjacentNetworks.entrySet());
            network = entrySet.getFirst().getKey();
            preferredOrder.addAll(network.getConveyors());

            for (int i = 1; i < entrySet.size(); i++) {
                ConveyorNetwork otherNetwork = entrySet.get(i).getKey();
                if (otherNetwork == network)
                    continue;

                preferredOrder.addAll(otherNetwork.getConveyors());
                mergeNetworks(network, otherNetwork);
                mergedNetworks.add(otherNetwork);
            }
        }

        network.addConveyor(pos);
        addConveyor(pos, network);

        preferredOrder.add(pos);
        reorderConveyors(world, network, preferredOrder);
        updateConnectedBlocks(world, network);

        List<CustomPacketPayload> payloads = new ArrayList<>();
        for (ConveyorNetwork mergedNetwork : mergedNetworks) {
            payloads.add(new RemoveConveyorNetworkPayload(
                    world.dimension(),
                    mergedNetwork.getId()));
        }

        if (isNewNetwork) {
            payloads.add(new AddConveyorNetworkPayload(
                    world.dimension(),
                    network));
        } else {
            payloads.add(new RemoveConveyorNetworkPayload(
                    world.dimension(),
                    network.getId()));

            payloads.add(new AddConveyorNetworkPayload(
                    world.dimension(),
                    network));
        }

        syncAndSave(world, payloads);
    }

    protected void onRemoveConveyor(ServerLevel world, BlockPos pos) {
        UUID networkId = removeConveyor(pos);
        if (networkId == null)
            return;

        ConveyorNetwork network = getNetwork(networkId);
        if (network == null)
            return;

        List<CustomPacketPayload> payloads = new ArrayList<>();
        network.removeConveyor(pos);
        Map<BlockPos, Integer> preservedRoutingState = snapshotRoutingState(network.getConveyors());

        if (network.getConveyors().isEmpty()) {
            removeNetwork(network);
            payloads.add(new RemoveConveyorNetworkPayload(
                    world.dimension(),
                    networkId));

            syncAndSave(world, payloads);
            return;
        }

        List<BlockPos> previousOrder = new ArrayList<>(network.getConveyors());
        Set<BlockPos> visited = new HashSet<>();
        List<List<BlockPos>> connectedComponents = new ArrayList<>();
        for (BlockPos conveyor : network.getConveyors()) {
            if (visited.contains(conveyor))
                continue;

            Set<BlockPos> connectedComponentSet = new HashSet<>();
            floodFill(world, conveyor, visited, connectedComponentSet);
            if (connectedComponentSet.isEmpty())
                continue;

            List<BlockPos> connectedComponent = previousOrder.stream()
                    .filter(connectedComponentSet::contains)
                    .toList();
            connectedComponents.add(new ArrayList<>(connectedComponent));
        }

        if (connectedComponents.size() == 1) {
            reorderConveyors(world, network, previousOrder);
            updateConnectedBlocks(world, network);

            payloads.add(new RemoveConveyorNetworkPayload(
                    world.dimension(),
                    networkId));
            payloads.add(new AddConveyorNetworkPayload(
                    world.dimension(),
                    network));

            syncAndSave(world, payloads);
            return;
        }

        payloads.add(new RemoveConveyorNetworkPayload(
                world.dimension(),
                networkId));
        removeNetwork(network);
        for (BlockPos conveyor : network.getConveyors()) {
            removeConveyor(conveyor);
        }

        for (List<BlockPos> connectedComponent : connectedComponents) {
            var newNetwork = new ConveyorNetwork();
            newNetwork.moveConveyorsFrom(network, new LinkedHashSet<>(connectedComponent));
            reorderConveyors(world, newNetwork, connectedComponent);
            updateConnectedBlocks(world, newNetwork);

            transferStorage(network, newNetwork, connectedComponent);
            restoreRoutingState(connectedComponent, preservedRoutingState);

            addNetwork(newNetwork);
            for (BlockPos conveyor : connectedComponent) {
                addConveyor(conveyor, newNetwork.getId());
            }

            payloads.add(new AddConveyorNetworkPayload(
                    world.dimension(),
                    newNetwork));
        }

        syncAndSave(world, payloads);
    }

    private static void syncAndSave(ServerLevel world, List<CustomPacketPayload> payloads) {
        for (ServerPlayer player : world.players()) {
            for (CustomPacketPayload payload : payloads) {
                ServerPlayNetworking.send(player, payload);
            }
        }

        LevelConveyorNetworks.getOrCreate(world).setDirty();
    }

    protected void mergeNetworks(ConveyorNetwork target, ConveyorNetwork source) {
        if (target == source)
            return;

        target.moveConveyorsFrom(source, new LinkedHashSet<>(source.getConveyors()));
        for (BlockPos conveyor : source.getConveyors()) {
            addConveyor(conveyor, target.getId());
        }

        transferStorage(source, target, source.getConveyors());

        removeNetwork(source);
    }

    private static void transferStorage(ConveyorNetwork source, ConveyorNetwork target, Collection<BlockPos> conveyorsToTransfer) {
        Map<BlockPos, ConveyorStorage> sourceStorages = source.getStorage().getStorages();
        Map<BlockPos, ConveyorStorage> targetStorages = target.getStorage().getStorages();
        for (BlockPos conveyorPos : conveyorsToTransfer) {
            ConveyorStorage sourceStorage = sourceStorages.remove(conveyorPos);
            if (sourceStorage == null)
                continue;

            targetStorages.merge(conveyorPos, sourceStorage, ConveyorNetworkManager::mergeStorage);
        }
    }

    private static ConveyorStorage mergeStorage(ConveyorStorage targetStorage, ConveyorStorage sourceStorage) {
        if (targetStorage == sourceStorage)
            return targetStorage;

        targetStorage.getItems().addAll(sourceStorage.getItems());
        return targetStorage;
    }

    protected boolean updateConnectedBlocks(ServerLevel world, ConveyorNetwork network) {
        Map<BlockPos, Map<String, BlockPos>> existingConnectedBlocks = copyConnectedBlocks(network.getConnectedBlocks());
        Map<BlockPos, Map<String, BlockPos>> resolvedConnectedBlocks = new HashMap<>();

        Set<BlockPos> attachedBlocks = new HashSet<>();
        for (BlockPos conveyor : network.getConveyors()) {
            Map<String, BlockPos> connectedBlocks = findConnectedBlocks(
                    world,
                    conveyor,
                    existingConnectedBlocks.getOrDefault(conveyor, Map.of()),
                    attachedBlocks
            );
            if (connectedBlocks.isEmpty())
                continue;

            resolvedConnectedBlocks.put(conveyor, connectedBlocks);
        }

        if (existingConnectedBlocks.equals(resolvedConnectedBlocks))
            return false;

        network.clearConnectedBlocks(world);
        resolvedConnectedBlocks.forEach((conveyor, connectedBlocks) ->
                connectedBlocks.forEach((outputId, connectedBlock) -> network.addConnectedBlock(world, conveyor, outputId, connectedBlock)));
        return true;
    }

    protected void floodFill(ServerLevel world, BlockPos pos, Set<BlockPos> visited, Set<BlockPos> connectedComponent) {
        Queue<BlockPos> queue = new ArrayDeque<>();
        queue.add(pos);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            if (!visited.add(current) || !isConveyor(world, current))
                continue;

            connectedComponent.add(current);
            for (BlockPos neighbor : getConnectedConveyorNeighbors(world, current)) {
                if (!visited.contains(neighbor)) {
                    queue.add(neighbor);
                }
            }
        }
    }

    public boolean isConveyor(Level world, BlockPos pos) {
        return world.getBlockState(pos).is(TagList.Blocks.CONVEYORS);
    }

    public void traverseCreateNetwork(ServerLevel world, BlockPos pos) {
        if (!isConveyor(world, pos) || hasConveyor(pos))
            return;

        Set<BlockPos> connectedConveyors = new HashSet<>();
        traverseCreateNetwork(world, pos, connectedConveyors);

        var network = new ConveyorNetwork();
        network.getConveyors().addAll(connectedConveyors);
        reorderConveyors(world, network, List.of(pos));

        updateConnectedBlocks(world, network);

        for (BlockPos connectedConveyor : connectedConveyors) {
            addConveyor(connectedConveyor, network.getId());
        }

        addNetwork(network);
    }

    protected void traverseCreateNetwork(ServerLevel world, BlockPos pos, Set<BlockPos> visited) {
        if (!isConveyor(world, pos) || visited.contains(pos))
            return;

        visited.add(pos);

        for (BlockPos neighbor : getConnectedConveyorNeighbors(world, pos)) {
            traverseCreateNetwork(world, neighbor, visited);
        }
    }

    public Map<BlockPos, UUID> getConveyorToNetworkId() {
        return this.conveyorToNetworkId;
    }

    public Map<BlockPos, Integer> getRoundRobinIndices() {
        return this.roundRobinIndices;
    }

    @Override
    public int getRoundRobinIndex(BlockPos pos, int outputCount) {
        if (outputCount <= 0)
            return 0;

        return Math.floorMod(this.roundRobinIndices.getOrDefault(pos, 0), outputCount);
    }

    @Override
    public void advanceRoundRobinIndex(BlockPos pos, int outputCount) {
        if (outputCount <= 0)
            return;

        this.roundRobinIndices.compute(pos, (_, current) -> {
            int next = current == null ? 1 : current + 1;
            return Math.floorMod(next, outputCount);
        });
        this.routingStateChanged = true;
    }

    @Override
    public void setRoundRobinIndex(BlockPos pos, int index, int outputCount) {
        if (outputCount <= 0)
            return;

        this.roundRobinIndices.put(pos, Math.floorMod(index, outputCount));
        this.routingStateChanged = true;
    }

    protected Map<String, BlockPos> findConnectedBlocks(ServerLevel world, BlockPos conveyor, Map<String, BlockPos> preferredBlocks, Set<BlockPos> attachedBlocks) {
        BlockState conveyorState = world.getBlockState(conveyor);
        ConveyorTopology topology = getConveyorTopology(world, conveyor, conveyorState);
        if (topology == null || topology.outputs().isEmpty())
            return Map.of();

        Set<ConveyorOutput> routedOutputs = getConnectedOutputConveyors(world, conveyor, null).keySet();
        Map<String, BlockPos> connectedBlocks = new LinkedHashMap<>();

        for (ConveyorOutput output : topology.outputs()) {
            if (routedOutputs.contains(output))
                continue;

            BlockPos connectedBlock = findConnectedBlock(world, output, preferredBlocks.get(output.id()), attachedBlocks);
            if (connectedBlock == null)
                continue;

            attachedBlocks.add(connectedBlock);
            connectedBlocks.put(output.id(), connectedBlock);
        }

        return connectedBlocks;
    }

    protected boolean reorderConveyors(ServerLevel world, ConveyorNetwork network, List<BlockPos> preferredOrder) {
        List<BlockPos> conveyors = network.getConveyors();
        if (conveyors.size() <= 1)
            return false;

        List<BlockPos> existingOrder = new ArrayList<>(conveyors);

        Set<BlockPos> conveyorSet = new HashSet<>(conveyors);
        Comparator<BlockPos> comparator = createBlockPosComparator(preferredOrder, conveyorSet);

        Map<BlockPos, Set<BlockPos>> outgoingConveyors = new HashMap<>();
        Map<BlockPos, Integer> inDegree = new HashMap<>();
        for (BlockPos conveyor : conveyorSet) {
            outgoingConveyors.put(conveyor, new LinkedHashSet<>());
            inDegree.put(conveyor, 0);
        }

        for (BlockPos conveyor : conveyorSet) {
            for (BlockPos downstreamConveyor : getConnectedOutputConveyors(world, conveyor, conveyorSet).values()) {
                if (outgoingConveyors.get(conveyor).add(downstreamConveyor)) {
                    inDegree.merge(downstreamConveyor, 1, Integer::sum);
                }
            }
        }

        List<BlockPos> orderedConveyors = new ArrayList<>(conveyorSet.size());
        Set<BlockPos> processed = new HashSet<>();
        PriorityQueue<BlockPos> ready = new PriorityQueue<>(comparator);

        inDegree.entrySet().stream()
                .filter(entry -> entry.getValue() == 0)
                .map(Map.Entry::getKey)
                .forEach(ready::add);

        while (orderedConveyors.size() < conveyorSet.size()) {
            if (ready.isEmpty()) {
                BlockPos cycleStart = conveyorSet.stream()
                        .filter(pos -> !processed.contains(pos))
                        .min(Comparator
                                .comparingInt((BlockPos pos) -> inDegree.getOrDefault(pos, 0))
                                .thenComparing(comparator))
                        .orElse(null);
                if (cycleStart == null)
                    break;

                ready.add(cycleStart);
            }

            BlockPos conveyor = ready.poll();
            if (!processed.add(conveyor))
                continue;

            orderedConveyors.add(conveyor);

            List<BlockPos> downstreamConveyors = new ArrayList<>(outgoingConveyors.getOrDefault(conveyor, Set.of()));
            downstreamConveyors.sort(comparator);
            for (BlockPos downstreamConveyor : downstreamConveyors) {
                if (processed.contains(downstreamConveyor))
                    continue;

                int remainingInDegree = inDegree.merge(downstreamConveyor, -1, Integer::sum);
                if (remainingInDegree == 0) {
                    ready.add(downstreamConveyor);
                }
            }
        }

        if (existingOrder.equals(orderedConveyors))
            return false;

        conveyors.clear();
        conveyors.addAll(orderedConveyors);
        return true;
    }

    private static @NonNull Comparator<BlockPos> createBlockPosComparator(List<BlockPos> preferredOrder, Set<BlockPos> conveyorSet) {
        Map<BlockPos, Integer> preferredIndices = new HashMap<>();
        int index = 0;
        for (BlockPos preferredPos : preferredOrder) {
            if (conveyorSet.contains(preferredPos)) {
                preferredIndices.putIfAbsent(preferredPos, index++);
            }
        }

        return Comparator
                .comparingInt((BlockPos blockPos) -> preferredIndices.getOrDefault(blockPos, Integer.MAX_VALUE))
                .thenComparingInt(BlockPos::getY)
                .thenComparingInt(BlockPos::getZ)
                .thenComparingInt(BlockPos::getX);
    }

    protected Map<ConveyorOutput, BlockPos> getConnectedOutputConveyors(ServerLevel world, BlockPos conveyorPos, @Nullable Set<BlockPos> conveyorSet) {
        BlockState conveyorState = world.getBlockState(conveyorPos);
        ConveyorTopology topology = getConveyorTopology(world, conveyorPos, conveyorState);
        if (topology == null || topology.outputs().isEmpty())
            return Map.of();

        Map<ConveyorOutput, BlockPos> connectedOutputs = new LinkedHashMap<>();

        for (ConveyorOutput output : topology.outputs()) {
            BlockPos connectedConveyor = findConnectedOutputConveyor(world, output, conveyorSet);
            if (connectedConveyor != null) {
                connectedOutputs.put(output, connectedConveyor);
            }
        }

        return connectedOutputs;
    }

    protected Set<BlockPos> getConnectedConveyorNeighbors(ServerLevel world, BlockPos pos) {
        Set<BlockPos> connectedNeighbors = new LinkedHashSet<>();

        for (BlockPos candidate : getCandidateNeighborPositions(pos)) {
            if (candidate.equals(pos) || !isConveyor(world, candidate))
                continue;

            if (connectsToConveyor(world, pos, candidate) || connectsToConveyor(world, candidate, pos)) {
                connectedNeighbors.add(candidate);
            }
        }

        return connectedNeighbors;
    }

    @Nullable
    protected ConveyorTopology getConveyorTopology(Level world, BlockPos conveyorPos, BlockState conveyorState) {
        return conveyorState.getBlock() instanceof ConveyorLike conveyor
                ? conveyor.getTopology(world, conveyorPos, conveyorState)
                : null;
    }

    protected boolean acceptsInputFrom(Level world, BlockState conveyorState, BlockPos conveyorPos, BlockPos expectedInputPos) {
        ConveyorTopology topology = getConveyorTopology(world, conveyorPos, conveyorState);
        return topology != null && topology.acceptsInputFrom(expectedInputPos);
    }

    protected boolean connectsToConveyor(ServerLevel world, BlockPos fromPos, BlockPos toPos) {
        BlockState fromState = world.getBlockState(fromPos);
        ConveyorTopology topology = getConveyorTopology(world, fromPos, fromState);
        if (topology == null)
            return false;

        for (ConveyorOutput output : topology.outputs()) {
            if (!getCandidateOutputTargets(output.deliveryPos()).contains(toPos))
                continue;

            if (acceptsInputFrom(world, world.getBlockState(toPos), toPos, output.expectedInputPos()))
                return true;
        }

        return false;
    }

    @Nullable
    private BlockPos findConnectedOutputConveyor(Level world, ConveyorOutput output, @Nullable Set<BlockPos> conveyorSet) {
        for (BlockPos candidatePos : getCandidateOutputTargets(output.deliveryPos())) {
            if (conveyorSet != null && !conveyorSet.contains(candidatePos))
                continue;

            if (conveyorSet == null && !isConveyor(world, candidatePos))
                continue;

            BlockState outputState = world.getBlockState(candidatePos);
            if (acceptsInputFrom(world, outputState, candidatePos, output.expectedInputPos()))
                return candidatePos;
        }

        return null;
    }

    protected List<BlockPos> getCandidateOutputTargets(BlockPos outputPos) {
        return List.of(outputPos, outputPos.above(), outputPos.below());
    }

    protected Set<BlockPos> getCandidateNeighborPositions(BlockPos pos) {
        Set<BlockPos> candidates = new LinkedHashSet<>();
        candidates.add(pos.above());
        candidates.add(pos.below());

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            for (int yOffset = -1; yOffset <= 1; yOffset++) {
                candidates.add(pos.offset(direction.getStepX(), yOffset, direction.getStepZ()));
            }
        }

        return candidates;
    }

    private Map<BlockPos, Integer> snapshotRoutingState(Collection<BlockPos> conveyors) {
        Map<BlockPos, Integer> snapshot = new HashMap<>();
        for (BlockPos conveyor : conveyors) {
            Integer index = this.roundRobinIndices.get(conveyor);
            if (index != null) {
                snapshot.put(conveyor, index);
            }
        }

        return snapshot;
    }

    private void restoreRoutingState(Collection<BlockPos> conveyors, Map<BlockPos, Integer> snapshot) {
        for (BlockPos conveyor : conveyors) {
            Integer index = snapshot.get(conveyor);
            if (index != null) {
                this.roundRobinIndices.put(conveyor, index);
            }
        }
    }

    @Nullable
    private BlockPos findConnectedBlock(ServerLevel world, ConveyorOutput output, @Nullable BlockPos preferredBlock, Set<BlockPos> attachedBlocks) {
        BlockPos conveyorPos = output.expectedInputPos();
        if (preferredBlock != null && !attachedBlocks.contains(preferredBlock) && canAttachInventory(world, conveyorPos, output, preferredBlock))
            return preferredBlock;

        BlockPos outputPos = output.deliveryPos();
        if (attachedBlocks.contains(outputPos) || isConveyor(world, outputPos))
            return null;

        return canAttachInventory(world, conveyorPos, output, outputPos) ? outputPos : null;
    }

    private boolean canAttachInventory(ServerLevel world, BlockPos conveyorPos, ConveyorOutput output, BlockPos connectedBlock) {
        if (!output.deliveryPos().equals(connectedBlock) || isConveyor(world, connectedBlock))
            return false;

        BlockState conveyorState = world.getBlockState(conveyorPos);
        if (!(conveyorState.getBlock() instanceof ConveyorLike conveyor))
            return false;

        if (!conveyor.canAttachToStorageOutput(world, conveyorPos, conveyorState, output, connectedBlock))
            return false;

        Storage<ItemVariant> storage = TransferType.ITEM.lookup(world, connectedBlock, output.inventoryInsertSide());
        return storage != null && storage.supportsInsertion();
    }

    private static Map<BlockPos, Map<String, BlockPos>> copyConnectedBlocks(Map<BlockPos, Map<String, BlockPos>> connectedBlocks) {
        Map<BlockPos, Map<String, BlockPos>> copy = new HashMap<>();
        connectedBlocks.forEach((conveyorPos, attachments) -> copy.put(conveyorPos, new HashMap<>(attachments)));
        return copy;
    }
}
