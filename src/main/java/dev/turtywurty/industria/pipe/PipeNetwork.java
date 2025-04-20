package dev.turtywurty.industria.pipe;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.turtywurty.industria.init.PipeNetworkTypesInit;
import dev.turtywurty.industria.multiblock.TransferType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class PipeNetwork<S> {
    public static final Codec<Set<PipeNetwork<?>>> SET_CODEC = Codec.list(PipeNetworkTypesInit.CODEC.codec()).xmap(Sets::newHashSet, Lists::newArrayList);
    public static final Codec<Set<BlockPos>> BLOCK_POS_SET_CODEC = Codec.list(BlockPos.CODEC).xmap(Sets::newHashSet, Lists::newArrayList);
    public static final PacketCodec<RegistryByteBuf, Set<PipeNetwork<?>>> SET_PACKET_CODEC =
            PacketCodecs.collection(HashSet::new, PacketCodecs.registryCodec(PipeNetworkTypesInit.CODEC.codec()));


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

    public void movePipesFrom(PipeNetwork<S> oldNetwork, Set<BlockPos> pipesToInherit) {
        for (BlockPos pipe : pipesToInherit) {
            addPipe(pipe);
        }
    }

    public Set<BlockPos> getConnectedBlocks() {
        return this.connectedBlocks;
    }

    public TransferType<S, ?, ?> getTransferType() {
        return this.transferType;
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
    public abstract MapCodec<? extends PipeNetwork<?>> getCodec();

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
