package dev.turtywurty.industria.pipe;

import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.util.NBTSerializable;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class PipeNetwork<S> implements NBTSerializable<NbtCompound> {
    protected UUID id;
    protected final Set<BlockPos> pipes = ConcurrentHashMap.newKeySet();
    protected final Set<BlockPos> connectedBlocks = ConcurrentHashMap.newKeySet();
    protected final TransferType<S, ?, ?> transferType;
    protected final S storage;

    public PipeNetwork(UUID id, TransferType<S, ?, ?> transferType) {
        this.id = id;
        this.transferType = transferType;
        this.storage = createStorage();
    }

    @Override
    public NbtCompound writeNbt(RegistryWrapper.WrapperLookup registryLookup) {
        var nbt = new NbtCompound();
        nbt.putUuid("id", this.id);

        var pipesNbtList = new NbtList();
        for (BlockPos pipe : this.pipes) {
            var pipeNbt = new NbtCompound();
            pipeNbt.putLong("pos", pipe.asLong());
            pipesNbtList.add(pipeNbt);
        }

        nbt.put("pipes", pipesNbtList);

        var connectedBlocksNbtList = new NbtList();
        for (BlockPos connectedBlock : this.connectedBlocks) {
            var connectedBlockNbt = new NbtCompound();
            connectedBlockNbt.putLong("pos", connectedBlock.asLong());
            connectedBlocksNbtList.add(connectedBlockNbt);
        }

        nbt.put("connectedBlocks", connectedBlocksNbtList);

        return nbt;
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        this.pipes.clear();
        this.connectedBlocks.clear();

        this.id = nbt.getUuid("id");

        NbtList pipes = nbt.getList("pipes", 10);
        for (int i = 0; i < pipes.size(); i++) {
            this.pipes.add(BlockPos.fromLong(pipes.getCompound(i).getLong("pos")));
        }

        NbtList connectedBlocks = nbt.getList("connectedBlocks", 10);
        for (int i = 0; i < connectedBlocks.size(); i++) {
            this.connectedBlocks.add(BlockPos.fromLong(connectedBlocks.getCompound(i).getLong("pos")));
        }
    }

    public boolean isOfSameType(@NotNull PipeNetwork<?> storage) {
        return this.transferType == storage.transferType;
    }

    public UUID getId() {
        return this.id;
    }

    public Set<BlockPos> getPipes() {
        return this.pipes;
    }

    public void addPipe(BlockPos pos) {
        this.pipes.add(pos);
    }

    public void removePipe(BlockPos pos) {
        this.pipes.remove(pos);
    }

    public void inheritPipesFrom(PipeNetwork<S> oldNetwork, Set<BlockPos> pipesToInherit) {
        for (BlockPos pipe : pipesToInherit) {
            addPipe(pipe);
            oldNetwork.removePipe(pipe);
        }
    }

    public boolean hasCentralStorage() {
        return true;
    }

    protected void onConnectedBlocksChanged(World world) {
        // NO-OP
    }

    public void clearConnectedBlocks(World world) {
        this.connectedBlocks.clear();
        onConnectedBlocksChanged(world);
    }

    public void addConnectedBlock(World world, BlockPos pos) {
        this.connectedBlocks.add(pos);
        onConnectedBlocksChanged(world);
    }

    public void removeConnectedBlock(World world, BlockPos pos) {
        this.connectedBlocks.remove(pos);
        onConnectedBlocksChanged(world);
    }

    public void addConnectedBlocks(World world, Collection<BlockPos> connectedBlocks) {
        this.connectedBlocks.addAll(connectedBlocks);
        onConnectedBlocksChanged(world);
    }

    public void addConnectedBlocks(World world, PipeNetwork<?> network) {
        this.connectedBlocks.addAll(network.connectedBlocks);
        onConnectedBlocksChanged(world);
    }

    public void addConnectedBlocks(World world, BlockPos... connectedBlocks) {
        this.connectedBlocks.addAll(Arrays.asList(connectedBlocks));
        onConnectedBlocksChanged(world);
    }

    public void removeConnectedBlocks(World world, Collection<BlockPos> connectedBlocks) {
        this.connectedBlocks.removeAll(connectedBlocks);
        onConnectedBlocksChanged(world);
    }

    public void removeConnectedBlocks(World world, BlockPos... connectedBlocks) {
        for (BlockPos blockPos : connectedBlocks) {
            this.connectedBlocks.remove(blockPos);
        }

        onConnectedBlocksChanged(world);
    }

    public void removeConnectedBlocks(World world, PipeNetwork<?> network) {
        this.connectedBlocks.removeAll(network.connectedBlocks);
        onConnectedBlocksChanged(world);
    }

    protected abstract S createStorage();

    public S getStorage(World world, BlockPos pos) {
        return this.storage;
    }

    public void tick(World world) {
        for (BlockPos connectedPos : this.connectedBlocks) {
            for (Direction direction : Direction.values()) {
                BlockPos pipePos = connectedPos.offset(direction);
                if (this.pipes.contains(pipePos)) {
                    this.transferType.pushTo(world, pipePos, connectedPos, direction);
                }
            }
        }
    }

    @FunctionalInterface
    public interface Factory<S, N extends PipeNetwork<S>> {
        N create(UUID id);
    }
}
