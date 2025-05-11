package dev.turtywurty.industria.pipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.turtywurty.industria.init.PipeNetworkTypeInit;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.util.ExtraCodecs;
import dev.turtywurty.industria.util.ExtraPacketCodecs;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

public abstract class PipeNetwork<S> {
    public static final Codec<PipeNetwork<?>> CODEC = PipeNetworkTypeInit.CODEC.dispatch(
            PipeNetwork::getType, PipeNetworkType::codec);

    public static final PacketCodec<RegistryByteBuf, PipeNetwork<?>> PACKET_CODEC =
            PipeNetworkTypeInit.PACKET_CODEC.dispatch(PipeNetwork::getType, PipeNetworkType::packetCodec);

    protected static <S, ST, N extends PipeNetwork<S>> MapCodec<N> createCodec(RecordCodecBuilder<N, ST> storageApp, BiConsumer<S, ST> storageModifier, Function<UUID, N> factory) {
        return RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        Uuids.CODEC.fieldOf("id").forGetter(PipeNetwork::getId),
                        ExtraCodecs.BLOCK_POS_SET_CODEC.fieldOf("pipes").forGetter(PipeNetwork::getPipes),
                        ExtraCodecs.BLOCK_POS_SET_CODEC.fieldOf("connectedBlocks").forGetter(PipeNetwork::getConnectedBlocks),
                        TransferType.CODEC.fieldOf("transferType").forGetter(PipeNetwork::getTransferType),
                        storageApp
                ).apply(instance, (id, pipes, connectedBlocks, transferType, storage) -> {
                    var network = factory.apply(id);
                    network.pipes.addAll(pipes);
                    network.connectedBlocks.addAll(connectedBlocks);
                    storageModifier.accept(network.storage, storage);

                    return network;
                }));
    }

    protected static <S, ST, N extends PipeNetwork<S>> PacketCodec<RegistryByteBuf, N> createPacketCodec(
            PacketCodec<? super RegistryByteBuf, ST> storageTypeCodec,
            Function<N, ST> storageTypeRetriever,
            BiConsumer<S, ST> storageModifier,
            Function<UUID, N> factory) {
        return PacketCodec.tuple(
                Uuids.PACKET_CODEC, PipeNetwork::getId,
                ExtraPacketCodecs.BLOCK_POS_SET_PACKET_CODEC, PipeNetwork::getPipes,
                ExtraPacketCodecs.BLOCK_POS_SET_PACKET_CODEC, PipeNetwork::getConnectedBlocks,
                TransferType.PACKET_CODEC, PipeNetwork::getTransferType,
                storageTypeCodec, storageTypeRetriever,
                (id, pipes, connectedBlocks, transferType, storage) -> {
                    var network = factory.apply(id);
                    network.pipes.addAll(pipes);
                    network.connectedBlocks.addAll(connectedBlocks);
                    storageModifier.accept(network.storage, storage);

                    return network;
                });
    }

    protected UUID id;
    protected final Set<BlockPos> pipes = ConcurrentHashMap.newKeySet();
    protected final Set<BlockPos> connectedBlocks = ConcurrentHashMap.newKeySet();
    protected final TransferType<S, ?, ?> transferType;
    public final S storage;

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

    protected abstract PipeNetworkType<S, ? extends PipeNetwork<S>> getType();

    public S getStorage(BlockPos pos) {
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
}
