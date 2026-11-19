package dev.turtywurty.industria.mechanical;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jspecify.annotations.Nullable;

import java.util.*;

public final class MechanicalNetworkManager {
    private static final Map<ServerLevel, MechanicalNetworkManager> BY_LEVEL = new WeakHashMap<>();

    public static MechanicalNetworkManager getOrCreate(ServerLevel level) {
        return BY_LEVEL.computeIfAbsent(level, _ -> new MechanicalNetworkManager());
    }

    private final Map<BlockPos, MechanicalNetwork> networksByPos = new HashMap<>();
    private final Set<MechanicalNetwork> networks = new LinkedHashSet<>();
    private final Set<BlockPos> dirtyPositions = new HashSet<>();
    private final ShaftRotationSync rotationSync = new ShaftRotationSync();

    private final Set<Long> unloadingChunks = new HashSet<>();

    private MechanicalNetworkManager() {
    }

    public void markDirty(BlockPos pos) {
        dirtyPositions.add(pos.immutable());
    }

    public @Nullable MechanicalNetwork getNetworkAt(BlockPos pos) {
        return networksByPos.get(pos);
    }

    public Set<MechanicalNetwork> getNetworks() {
        return Collections.unmodifiableSet(networks);
    }

    public void tick(ServerLevel level) {
        boolean topologyChanged = !dirtyPositions.isEmpty();
        if (topologyChanged) {
            rebuildNetworks(level);
        }

        unloadingChunks.clear();

        for (MechanicalNetwork network : new ArrayList<>(networks)) {
            network.tick(level);
        }

        if (topologyChanged) {
            rotationSync.updateTopology(level, networksByPos);
        }

        rotationSync.tick(level);
    }

    public void onChunkSent(ServerLevel level, ServerPlayer player, ChunkPos chunk) {
        rotationSync.sendInitialChunk(level, player, chunk);
    }

    public void onChunkLoaded(LevelChunk chunk) {
        unloadingChunks.remove(chunk.getPos().pack());

        chunk.findBlocks(
            MechanicalNetworkManager::isMechanicalNode,
            (pos, _) -> markDirty(pos));
    }

    public void onChunkUnloaded(ChunkPos chunkPos) {
        unloadingChunks.add(chunkPos.pack());

        for (BlockPos pos : networksByPos.keySet()) {
            if (pos.getX() >> 4 == chunkPos.x() && pos.getZ() >> 4 == chunkPos.z()) {
                markDirty(pos);
            }
        }
    }

    private void rebuildNetworks(ServerLevel level) {
        Map<BlockPos, MechanicalNetwork> previousNetworks = new HashMap<>(networksByPos);
        Set<BlockPos> seeds = new LinkedHashSet<>(networksByPos.keySet());
        for (BlockPos dirtyPos : dirtyPositions) {
            seeds.add(dirtyPos);

            for (Direction direction : Direction.values()) {
                seeds.add(dirtyPos.relative(direction));
            }
        }

        dirtyPositions.clear();
        networksByPos.clear();
        networks.clear();

        for (BlockPos seed : seeds) {
            if (networksByPos.containsKey(seed) || !isMechanicalNode(level, seed))
                continue;

            Set<BlockPos> component = findConnectedComponent(level, seed);
            if (component.isEmpty())
                continue;

            MechanicalNetwork network = recreateNetwork(component, previousNetworks);
            network.refreshParticipants(level);
            networks.add(network);
            for (BlockPos pos : component) {
                networksByPos.put(pos, network);
            }
        }
    }

    public static MechanicalNetwork recreateNetwork(
        Set<BlockPos> component,
        Map<BlockPos, MechanicalNetwork> previousNetworks
    ) {
        Map<MechanicalNetwork, Integer> overlaps = new HashMap<>();
        for (BlockPos pos : component) {
            MechanicalNetwork previous = previousNetworks.get(pos);
            if (previous != null) {
                overlaps.merge(previous, 1, Integer::sum);
            }
        }

        if (overlaps.size() == 1) {
            MechanicalNetwork previous = overlaps.keySet().iterator().next();
            if (previous.getNodes().equals(component))
                return previous;
        }

        double momentum = 0.0;
        double weight = 0.0;
        MechanicalNetwork phaseSource = null;
        int largestOverlap = 0;
        for (Map.Entry<MechanicalNetwork, Integer> entry : overlaps.entrySet()) {
            MechanicalNetwork previous = entry.getKey();
            int overlap = entry.getValue();
            double share = (double) overlap / previous.getNodes().size();
            momentum += previous.getState().rpm() * share;
            weight += share;

            if (phaseSource == null || overlap > largestOverlap
                || (overlap == largestOverlap && previous.getId() < phaseSource.getId())) {
                phaseSource = previous;
                largestOverlap = overlap;
            }
        }

        return new MechanicalNetwork(component, weight > 0.0 ? momentum / weight : 0.0,
            phaseSource == null ? 0.0 : phaseSource.getRotationRadians());
    }

    private Set<BlockPos> findConnectedComponent(ServerLevel level, BlockPos start) {
        Set<BlockPos> result = new LinkedHashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        queue.add(start);

        while (!queue.isEmpty()) {
            BlockPos pos = queue.remove();
            if (result.contains(pos) || !isAvailable(level, pos))
                continue;

            BlockState state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof MechanicalNode node) || !node.isActiveMechanicalNode(state))
                continue;

            result.add(pos);
            for (Direction direction : node.getMechanicalPorts(state)) {
                BlockPos neighborPos = pos.relative(direction);
                if (!isAvailable(level, neighborPos) || result.contains(neighborPos))
                    continue;

                BlockState neighborState = level.getBlockState(neighborPos);
                if (!(neighborState.getBlock() instanceof MechanicalNode neighborNode)
                    || !neighborNode.isActiveMechanicalNode(neighborState))
                    continue;

                if (neighborNode.getMechanicalPorts(neighborState).contains(direction.getOpposite())) {
                    queue.add(neighborPos);
                }
            }
        }

        return result;
    }

    private boolean isAvailable(ServerLevel level, BlockPos pos) {
        long chunkKey = ChunkPos.pack(pos.getX() >> 4, pos.getZ() >> 4);
        return !unloadingChunks.contains(chunkKey) && level.isLoaded(pos);
    }

    private boolean isMechanicalNode(ServerLevel level, BlockPos pos) {
        return isAvailable(level, pos) && isMechanicalNode(level.getBlockState(pos));
    }

    private static boolean isMechanicalNode(BlockState state) {
        return state.getBlock() instanceof MechanicalNode node && node.isActiveMechanicalNode(state);
    }
}
