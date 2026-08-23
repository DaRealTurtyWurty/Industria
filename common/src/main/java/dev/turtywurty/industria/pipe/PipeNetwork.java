package dev.turtywurty.industria.pipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.turtywurty.industria.init.ModPipeNetworkTypes;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.util.ExtraCodecs;
import dev.turtywurty.industria.util.ExtraStreamCodecs;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import dev.turtywurty.turtymultiloader.transfer.storage.ResourceStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

public abstract class PipeNetwork<V extends ResourceVariant<?>> {
    public static final Codec<PipeNetwork<?>> CODEC = ModPipeNetworkTypes.CODEC.dispatch(
            PipeNetwork::getType, PipeNetworkType::codec);

    public static final StreamCodec<RegistryFriendlyByteBuf, PipeNetwork<?>> STREAM_CODEC =
            ModPipeNetworkTypes.STREAM_CODEC.dispatch(PipeNetwork::getType, PipeNetworkType::packetCodec);

    protected static <V extends ResourceVariant<?>, ST, N extends PipeNetwork<V>> MapCodec<N> createCodec(
            RecordCodecBuilder<N, ST> storageApp,
            BiConsumer<ResourceStorage<V>, ST> storageModifier,
            Function<UUID, N> factory) {
        return RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        UUIDUtil.CODEC.fieldOf("id").forGetter(PipeNetwork::getId),
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

    protected static <V extends ResourceVariant<?>, ST, N extends PipeNetwork<V>> StreamCodec<RegistryFriendlyByteBuf, N> createPacketCodec(
            StreamCodec<? super RegistryFriendlyByteBuf, ST> storageTypeCodec,
            Function<N, ST> storageTypeRetriever,
            BiConsumer<ResourceStorage<V>, ST> storageModifier,
            Function<UUID, N> factory) {
        return StreamCodec.composite(
                UUIDUtil.STREAM_CODEC, PipeNetwork::getId,
                ExtraStreamCodecs.BLOCK_POS_SET_STREAM_CODEC, PipeNetwork::getPipes,
                ExtraStreamCodecs.BLOCK_POS_SET_STREAM_CODEC, PipeNetwork::getConnectedBlocks,
                TransferType.STREAM_CODEC, PipeNetwork::getTransferType,
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
    protected final TransferType<ResourceStorage<V>, V, Long> transferType;
    public final ResourceStorage<V> storage;

    public PipeNetwork(UUID id, TransferType<ResourceStorage<V>, V, Long> transferType) {
        this.id = id;
        this.transferType = transferType;
        this.storage = createStorage();
    }

    public boolean isOfSameType(@NotNull PipeNetwork<?> storage) {
        return this.transferType.equals(storage.transferType);
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

    public void movePipesFrom(PipeNetwork<V> oldNetwork, Set<BlockPos> pipesToInherit) {
        for (BlockPos pipe : pipesToInherit) {
            addPipe(pipe);
        }
    }

    public Set<BlockPos> getConnectedBlocks() {
        return this.connectedBlocks;
    }

    public TransferType<ResourceStorage<V>, V, Long> getTransferType() {
        return this.transferType;
    }

    public boolean hasCentralStorage() {
        return true;
    }

    protected void onConnectedBlocksChanged(Level world) {
        // NO-OP
    }

    public void clearConnectedBlocks(Level world) {
        this.connectedBlocks.clear();
        onConnectedBlocksChanged(world);
    }

    public void addConnectedBlock(Level world, BlockPos pos) {
        this.connectedBlocks.add(pos);
        onConnectedBlocksChanged(world);
    }

    public void removeConnectedBlock(Level world, BlockPos pos) {
        this.connectedBlocks.remove(pos);
        onConnectedBlocksChanged(world);
    }

    public void addConnectedBlocks(Level world, Collection<BlockPos> connectedBlocks) {
        this.connectedBlocks.addAll(connectedBlocks);
        onConnectedBlocksChanged(world);
    }

    public void addConnectedBlocks(Level world, PipeNetwork<?> network) {
        this.connectedBlocks.addAll(network.connectedBlocks);
        onConnectedBlocksChanged(world);
    }

    public void addConnectedBlocks(Level world, BlockPos... connectedBlocks) {
        this.connectedBlocks.addAll(Arrays.asList(connectedBlocks));
        onConnectedBlocksChanged(world);
    }

    public void removeConnectedBlocks(Level world, Collection<BlockPos> connectedBlocks) {
        this.connectedBlocks.removeAll(connectedBlocks);
        onConnectedBlocksChanged(world);
    }

    public void removeConnectedBlocks(Level world, BlockPos... connectedBlocks) {
        for (BlockPos blockPos : connectedBlocks) {
            this.connectedBlocks.remove(blockPos);
        }

        onConnectedBlocksChanged(world);
    }

    public void removeConnectedBlocks(Level world, PipeNetwork<?> network) {
        this.connectedBlocks.removeAll(network.connectedBlocks);
        onConnectedBlocksChanged(world);
    }

    protected abstract ResourceStorage<V> createStorage();

    protected abstract PipeNetworkType<V, ? extends PipeNetwork<V>> getType();

    public ResourceStorage<V> getStorage(BlockPos pos) {
        return this.storage;
    }

    public void tick(Level world) {
        for (BlockPos connectedPos : this.connectedBlocks) {
            for (Direction direction : Direction.values()) {
                BlockPos pipePos = connectedPos.relative(direction);
                if (this.pipes.contains(pipePos)) {
                    this.transferType.pushTo(world, pipePos, connectedPos, direction);
                }
            }
        }
    }
}
