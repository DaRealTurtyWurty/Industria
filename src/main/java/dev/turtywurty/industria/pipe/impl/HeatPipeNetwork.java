package dev.turtywurty.industria.pipe.impl;

import dev.turtywurty.heatapi.api.HeatStorage;
import dev.turtywurty.heatapi.api.base.NoLimitHeatStorage;
import dev.turtywurty.heatapi.api.base.SimpleHeatStorage;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.pipe.PipeNetwork;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.*;

public class HeatPipeNetwork extends PipeNetwork<HeatStorage> {
    private final Map<BlockPos, Map<BlockPos, Integer>> pipeToSourceDistance = new HashMap<>();
    private final Map<BlockPos, HeatStorage> pipeStorages = new HashMap<>();

    public HeatPipeNetwork(UUID id) {
        super(id, TransferType.HEAT);
    }

    @Override
    protected SimpleHeatStorage createStorage() {
        return new NoLimitHeatStorage(true, true);
    }

    @Override
    public NbtCompound writeNbt(RegistryWrapper.WrapperLookup registryLookup) {
        var nbt = new NbtCompound();
        nbt.put("networkData", super.writeNbt(registryLookup));

        var storages = new NbtCompound();
        for (Map.Entry<BlockPos, HeatStorage> storageEntry : this.pipeStorages.entrySet()) {
            BlockPos pos = storageEntry.getKey();
            HeatStorage storage = storageEntry.getValue();
            storages.putDouble(String.valueOf(pos.asLong()), storage.getAmount());
        }

        nbt.put("pipeStorages", storages);

        return nbt;
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt.getCompound("networkData"), registryLookup);

        var storages = nbt.getCompound("pipeStorages");
        for (String key : storages.getKeys()) {
            BlockPos pos = BlockPos.fromLong(Long.parseLong(key));

            long amount = storages.getLong(key);
            SimpleHeatStorage heatStorage = createStorage();
            heatStorage.setAmount(amount);

            this.pipeStorages.put(pos, heatStorage);
        }
    }

    @Override
    protected void onConnectedBlocksChanged(World world) {
        super.onConnectedBlocksChanged(world);
        this.pipeToSourceDistance.clear();

        // Go through connected blocks, get the heat storage, check if it supports extraction
        Set<BlockPos> sources = new HashSet<>();
        for (BlockPos connectedBlockPos : this.connectedBlocks) {
            for (Direction direction : Direction.values()) {
                BlockPos offset = connectedBlockPos.offset(direction);
                if (!this.pipes.contains(offset))
                    continue;

                HeatStorage storage = this.transferType.lookup(world, connectedBlockPos, direction);
                if (storage != null && storage.supportsExtraction()) {
                    sources.add(connectedBlockPos);
                    break;
                }
            }
        }

        // Go through sources, calculate distance to each pipe
        for (BlockPos sourcePos : sources) {
            Map<BlockPos, Integer> pipeDistances = calculateDistancesFromSource(sourcePos);
            this.pipeToSourceDistance.put(sourcePos, pipeDistances);
        }
    }

    private Map<BlockPos, Integer> calculateDistancesFromSource(BlockPos sourcePos) {
        // Find adjacent pipes
        Set<BlockPos> adjacentPipes = new HashSet<>();
        for (Direction dir : Direction.values()) {
            BlockPos adjacentPos = sourcePos.offset(dir);
            if (this.pipes.contains(adjacentPos)) {
                adjacentPipes.add(adjacentPos);
            }
        }

        if (adjacentPipes.isEmpty()) {
            return new HashMap<>();
        }

        // BFS to compute the distances to all pipes
        Map<BlockPos, Integer> distances = new HashMap<>();
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();

        for (BlockPos startPos : adjacentPipes) {
            queue.add(startPos);
            distances.put(startPos, 0);
            visited.add(startPos);
        }

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            int currentDistance = distances.get(current);

            for (Direction dir : Direction.values()) {
                BlockPos neighbor = current.offset(dir);
                if (this.pipes.contains(neighbor) && !visited.contains(neighbor)) {
                    visited.add(neighbor);
                    distances.put(neighbor, currentDistance + 1);
                    queue.add(neighbor);
                }
            }
        }

        return distances;
    }

    @Override
    public HeatStorage getStorage(World world, BlockPos pos) {
        return this.pipeStorages.computeIfAbsent(pos, p -> createStorage());
    }

    @Override
    public void addPipe(BlockPos pos) {
        super.addPipe(pos);
        if (!this.pipeStorages.containsKey(pos)) {
            this.pipeStorages.put(pos, createStorage());
        }
    }

    @Override
    public void inheritPipesFrom(PipeNetwork<HeatStorage> oldNetwork, Set<BlockPos> pipesToInherit) {
        if (oldNetwork instanceof HeatPipeNetwork heatOldNetwork) {
            Map<BlockPos, HeatStorage> storagesToInherit = new HashMap<>();
            for (BlockPos pipe : pipesToInherit) {
                HeatStorage storage = heatOldNetwork.pipeStorages.get(pipe);
                if (storage != null) {
                    storagesToInherit.put(pipe, storage);
                }
            }

            super.inheritPipesFrom(oldNetwork, pipesToInherit);
            this.pipeStorages.putAll(storagesToInherit);
        } else {
            super.inheritPipesFrom(oldNetwork, pipesToInherit);
        }
    }

    @Override
    public boolean hasCentralStorage() {
        return false;
    }

    @Override
    public void tick(World world) {
        super.tick(world);

        Map<HeatStorage, Double> heatChanges = new HashMap<>();

        // Step 1: Transfer heat between adjacent pipes
        for (BlockPos pipePos : this.pipes) {
            HeatStorage pipeStorage = getStorage(world, pipePos);
            for (Direction dir : Direction.values()) {
                BlockPos neighborPos = pipePos.offset(dir);
                if (this.pipes.contains(neighborPos)) {
                    HeatStorage neighborStorage = getStorage(world, neighborPos);
                    double transfer = calculateTransfer(pipeStorage, neighborStorage);
                    heatChanges.merge(pipeStorage, -transfer, Double::sum);
                    heatChanges.merge(neighborStorage, transfer, Double::sum);
                }
            }
        }

        // Step 2: Transfer heat between pipes and connected blocks (sources or sinks)
        for (BlockPos connectedPos : this.connectedBlocks) {
            for (Direction dir : Direction.values()) {
                BlockPos pipePos = connectedPos.offset(dir);
                if (this.pipes.contains(pipePos)) {
                    HeatStorage pipeStorage = getStorage(world, pipePos);
                    HeatStorage connectedStorage = this.transferType.lookup(world, connectedPos, dir.getOpposite());
                    if (connectedStorage != null && connectedStorage.supportsInsertion()) {
                        double transfer = calculateTransfer(pipeStorage, connectedStorage);
                        heatChanges.merge(pipeStorage, -transfer, Double::sum);
                        heatChanges.merge(connectedStorage, transfer, Double::sum);
                    }
                }
            }
        }

        // Step 3: Apply all heat changes
        for (Map.Entry<HeatStorage, Double> entry : heatChanges.entrySet()) {
            HeatStorage storage = entry.getKey();
            double change = entry.getValue();
            ((SimpleHeatStorage) storage).setAmount(Math.max(0, storage.getAmount() + change));
        }

        // Step 4: Apply dissipation to pipes
        for (HeatStorage pipeStorage : this.pipeStorages.values()) {
            double dissipation = pipeStorage.getAmount() / 100D; // Dissipate 1% of the heat every tick
            ((SimpleHeatStorage) pipeStorage).setAmount(Math.max(0, pipeStorage.getAmount() - dissipation));
        }
    }

    private static double calculateTransfer(HeatStorage a, HeatStorage b) {
        return (a.getAmount() - b.getAmount()) / 10D; // Transfer 10% of the heat difference
    }
}
