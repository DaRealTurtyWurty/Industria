package dev.turtywurty.industria.pipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.turtywurty.industria.block.PipeBlock;
import dev.turtywurty.industria.init.PipeNetworkManagerTypeInit;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.network.AddPipeNetworkPayload;
import dev.turtywurty.industria.network.ModifyPipeNetworkPayload;
import dev.turtywurty.industria.network.RemovePipeNetworkPayload;
import dev.turtywurty.industria.persistent.WorldPipeNetworks;
import dev.turtywurty.industria.util.ExtraCodecs;
import dev.turtywurty.industria.util.ExtraPacketCodecs;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

// TODO: Splitting output between storages doesn't work
public abstract class PipeNetworkManager<S, N extends PipeNetwork<S>> {
    public static final Codec<PipeNetworkManager<?, ?>> CODEC = PipeNetworkManagerTypeInit.CODEC.dispatch(
            PipeNetworkManager::getType, PipeNetworkManagerType::codec);

    public static final Codec<List<PipeNetworkManager<?, ?>>> LIST_CODEC = CODEC.listOf().xmap(ArrayList::new, Function.identity());

    public static final StreamCodec<RegistryFriendlyByteBuf, PipeNetworkManager<?, ?>> STREAM_CODEC =
            PipeNetworkManagerTypeInit.STREAM_CODEC.dispatch(
                    PipeNetworkManager::getType, PipeNetworkManagerType::packetCodec);

    public static final StreamCodec<RegistryFriendlyByteBuf, List<PipeNetworkManager<?, ?>>> LIST_STREAM_CODEC =
            ByteBufCodecs.collection(ArrayList::new, STREAM_CODEC);

    public static final Codec<Map<BlockPos, UUID>> PIPE_TO_NETWORK_ID_CODEC = Codec.unboundedMap(
            ExtraCodecs.BLOCK_POS_STRING_CODEC, UUIDUtil.AUTHLIB_CODEC);

    public static final StreamCodec<ByteBuf, Map<BlockPos, UUID>> PIPE_TO_NETWORK_ID_STREAM_CODEC =
            ByteBufCodecs.map(HashMap::new, BlockPos.STREAM_CODEC, UUIDUtil.STREAM_CODEC);

    protected static <S, N extends PipeNetwork<S>, M extends PipeNetworkManager<S, N>> MapCodec<M> createCodec(Codec<N> networkCodec, Supplier<M> factory) {
        return RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        ExtraCodecs.setOf(networkCodec).fieldOf("networks").forGetter(PipeNetworkManager::getNetworks),
                        PIPE_TO_NETWORK_ID_CODEC.fieldOf("pipeToNetworkId").forGetter(PipeNetworkManager::getPipeToNetworkId)
                ).apply(instance, (networks, pipeToNetworkId) -> {
                    var manager = factory.get();
                    manager.networks.addAll(networks);
                    manager.pipeToNetworkId.putAll(pipeToNetworkId);
                    return manager;
                }));
    }

    protected static <S, N extends PipeNetwork<S>, M extends PipeNetworkManager<S, N>> StreamCodec<RegistryFriendlyByteBuf, M> createPacketCodec(StreamCodec<RegistryFriendlyByteBuf, N> networkCodec, Supplier<M> factory) {
        return StreamCodec.composite(
                ExtraPacketCodecs.setOf(networkCodec), PipeNetworkManager::getNetworks,
                PIPE_TO_NETWORK_ID_STREAM_CODEC, PipeNetworkManager::getPipeToNetworkId,
                (networks, pipeToNetworkId) -> {
                    var manager = factory.get();
                    manager.networks.addAll(networks);
                    manager.pipeToNetworkId.putAll(pipeToNetworkId);
                    return manager;
                });
    }

    protected final PipeNetworkManagerType<S, N> type;
    protected final TransferType<S, ?, ?> transferType;
    protected final Set<N> networks = ConcurrentHashMap.newKeySet();
    protected final Map<BlockPos, UUID> pipeToNetworkId = new ConcurrentHashMap<>();

    public PipeNetworkManager(PipeNetworkManagerType<S, N> type, TransferType<S, ?, ?> transferType) {
        this.type = type;
        this.transferType = transferType;
    }

    protected abstract N createNetwork(UUID id);

    public PipeNetworkManagerType<S, N> getType() {
        return this.type;
    }

    public TransferType<S, ?, ?> getTransferType() {
        return this.transferType;
    }

    public Set<N> getNetworks() {
        return this.networks;
    }

    public @Nullable N getNetwork(UUID id) {
        return this.networks.stream()
                .filter(network -> network.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public @Nullable UUID getNetworkId(BlockPos pos) {
        return this.pipeToNetworkId.get(pos);
    }

    public @Nullable N getNetwork(BlockPos pos) {
        UUID networkId = getNetworkId(pos);
        return networkId == null ? null : getNetwork(networkId);
    }

    public void addNetwork(N network) {
        this.networks.add(network);
    }

    public void removeNetwork(N network) {
        this.networks.remove(network);
    }

    public void addPipe(BlockPos pos, UUID networkId) {
        this.pipeToNetworkId.put(pos, networkId);
    }

    public UUID removePipe(BlockPos pos) {
        return this.pipeToNetworkId.remove(pos);
    }

    public boolean containsPipe(BlockPos pos) {
        return this.pipeToNetworkId.containsKey(pos);
    }

    public void clear() {
        this.networks.clear();
        this.pipeToNetworkId.clear();
    }

    public void tick(ServerLevel world) {
        for (PipeNetwork<S> network : this.networks) {
            network.tick(world);
        }
    }

    public void placePipe(ServerLevel world, BlockPos pos) {
        if (world.isClientSide())
            return;

        onPlacePipe(world, pos);
    }

    public void removePipe(ServerLevel world, BlockPos pos) {
        if (world.isClientSide())
            return;

        onRemovePipe(world, pos);
    }

    protected void onPlacePipe(ServerLevel world, BlockPos pos) {
        Map<BlockPos, N> adjacentNetworks = new HashMap<>();
        Set<BlockPos> adjacentBlocks = new HashSet<>();

        for (Direction direction : Direction.values()) {
            BlockPos offset = pos.relative(direction);
            if (isPipe(world, offset)) {
                UUID networkId = getNetworkId(offset);
                if (networkId != null) {
                    N network = getNetwork(networkId);
                    if (network != null) {
                        adjacentNetworks.put(offset, network);
                    }
                }
            } else if (transferType.lookup(world, offset, direction.getOpposite()) != null) {
                adjacentBlocks.add(offset);
            }
        }

        Map.Entry<BlockPos, N> networkEntry;
        boolean isNewNetwork = false;
        List<N> mergedNetworks = new ArrayList<>();
        if (adjacentNetworks.isEmpty()) {
            networkEntry = Map.entry(pos, createNetwork(UUID.randomUUID()));
            addNetwork(networkEntry.getValue());
            isNewNetwork = true;
        } else {
            List<Map.Entry<BlockPos, N>> entrySet = adjacentNetworks.entrySet().stream().toList();
            networkEntry = entrySet.getFirst();
            for (int i = 1; i < entrySet.size(); i++) {
                Map.Entry<BlockPos, N> otherNetwork = entrySet.get(i);
                mergeNetworks(world, networkEntry, otherNetwork);
                mergedNetworks.add(otherNetwork.getValue());
            }
        }

        N network = networkEntry.getValue();
        network.addPipe(pos);
        network.addConnectedBlocks(world, adjacentBlocks);
        addPipe(pos, network.getId());

        List<CustomPacketPayload> payloads = new ArrayList<>();
        if (isNewNetwork) {
            payloads.add(new AddPipeNetworkPayload(
                    world.dimension(),
                    this.transferType,
                    network));
        }

        if (!mergedNetworks.isEmpty()) {
            for (N mergedNetwork : mergedNetworks) {
                payloads.add(new RemovePipeNetworkPayload(
                        world.dimension(),
                        this.transferType,
                        mergedNetwork.id));
            }

            payloads.add(new RemovePipeNetworkPayload(
                    world.dimension(),
                    this.transferType,
                    network.id));

            payloads.add(new AddPipeNetworkPayload(
                    world.dimension(),
                    this.transferType,
                    network));
        } else {
            payloads.add(new ModifyPipeNetworkPayload(
                    ModifyPipeNetworkPayload.Operation.ADD_PIPE,
                    world.dimension(),
                    this.transferType,
                    network.id,
                    pos));

            for (BlockPos adjacentBlock : adjacentBlocks) {
                payloads.add(new ModifyPipeNetworkPayload(
                        ModifyPipeNetworkPayload.Operation.ADD_CONNECTED_BLOCK,
                        world.dimension(),
                        this.transferType,
                        network.id,
                        adjacentBlock));
            }
        }

        syncAndSave(world, payloads);
    }

    protected void onRemovePipe(ServerLevel world, BlockPos pos) {
        UUID networkId = removePipe(pos);
        if (networkId == null)
            return;

        N network = getNetwork(networkId);
        if (network == null)
            return;

        List<CustomPacketPayload> payloads = new ArrayList<>();
        network.removePipe(pos);

        payloads.add(new ModifyPipeNetworkPayload(
                ModifyPipeNetworkPayload.Operation.REMOVE_PIPE,
                world.dimension(),
                this.transferType,
                networkId,
                pos));

        if (network.getPipes().isEmpty()) {
            removeNetwork(network);
            payloads.add(new RemovePipeNetworkPayload(
                    world.dimension(),
                    this.transferType,
                    networkId));

            syncAndSave(world, payloads);
            return;
        }

        List<BlockPos> previousConnectedBlocks = new ArrayList<>(network.getConnectedBlocks());
        updateConnectedBlocks(world, network);
        List<BlockPos> removedConnectedBlocks = new ArrayList<>(previousConnectedBlocks);
        removedConnectedBlocks.removeAll(network.getConnectedBlocks());

        for (BlockPos connectedBlock : removedConnectedBlocks) {
            payloads.add(new ModifyPipeNetworkPayload(
                    ModifyPipeNetworkPayload.Operation.REMOVE_CONNECTED_BLOCK,
                    world.dimension(),
                    this.transferType,
                    networkId,
                    connectedBlock));
        }

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
            syncAndSave(world, payloads);
            return;
        }

        payloads.add(new RemovePipeNetworkPayload(
                world.dimension(),
                this.transferType,
                networkId));
        removeNetwork(network);

        int totalPipes = network.getPipes().size();
        for (Set<BlockPos> connectedComponent : connectedComponents) {
            N newNetwork = createNetwork(UUID.randomUUID());
            newNetwork.movePipesFrom(network, connectedComponent);
            updateConnectedBlocks(world, newNetwork);

            if (network.hasCentralStorage()) {
                double fraction = (double) connectedComponent.size() / totalPipes;
                List<BlockPos> adjacentPipes = new ArrayList<>();
                for (Direction direction : Direction.values()) {
                    BlockPos offset = pos.relative(direction);
                    if (isPipe(world, offset) && containsPipe(offset)) {
                        adjacentPipes.add(offset);
                    }
                }

                double newFraction = fraction / adjacentPipes.size();
                for (BlockPos adjacentPipePos : adjacentPipes) {
                    this.transferType.transferFraction(network.getStorage(pos),
                            newNetwork.getStorage(adjacentPipePos),
                            newFraction);
                }
            }

            addNetwork(newNetwork);
            for (BlockPos pipe : connectedComponent) {
                addPipe(pipe, newNetwork.getId());
            }

            payloads.add(new AddPipeNetworkPayload(
                    world.dimension(),
                    this.transferType,
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

        WorldPipeNetworks.getOrCreate(world).setDirty();
    }

    protected void mergeNetworks(ServerLevel world, Map.Entry<BlockPos, N> targetEntry, Map.Entry<BlockPos, N> sourceEntry) {
        N target = targetEntry.getValue();
        N source = sourceEntry.getValue();
        if (target == source || !target.isOfSameType(source)) {
            return;
        }

        target.movePipesFrom(source, source.getPipes());
        target.addConnectedBlocks(world, source);
        for (BlockPos pipe : source.getPipes()) {
            addPipe(pipe, target.getId());
        }

        if (target.hasCentralStorage()) {
            this.transferType.transferAll(source.getStorage(targetEntry.getKey()),
                    target.getStorage(targetEntry.getKey()));
        }

        removeNetwork(source);
    }

    protected void updateConnectedBlocks(ServerLevel world, N network) {
        network.clearConnectedBlocks(world);

        Set<BlockPos> newConnectedBlocks = new HashSet<>();
        for (BlockPos pipe : network.getPipes()) {
            for (Direction direction : Direction.values()) {
                BlockPos offset = pipe.relative(direction);
                if (!isPipe(world, offset) && transferType.lookup(world, offset, direction.getOpposite()) != null) {
                    newConnectedBlocks.add(offset);
                }
            }
        }

        network.addConnectedBlocks(world, newConnectedBlocks);
    }

    protected void floodFill(ServerLevel world, BlockPos pos, Set<BlockPos> visited, Set<BlockPos> connectedComponent) {
        Queue<BlockPos> queue = new LinkedList<>();
        queue.add(pos);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            if (!visited.add(current) || !isPipe(world, current))
                continue;

            connectedComponent.add(current);
            for (Direction direction : Direction.values()) {
                BlockPos offset = current.relative(direction);
                if (isPipe(world, offset) && !visited.contains(offset)) {
                    queue.add(offset);
                }
            }
        }
    }

    protected boolean isPipe(ServerLevel world, BlockPos pos) {
        return world.getBlockState(pos).getBlock() instanceof PipeBlock<?, ?, ?> pipeBlock && pipeBlock.getTransferType() == this.transferType;
    }

    public void traverseCreateNetwork(ServerLevel world, BlockPos pos) {
        if (!isPipe(world, pos) || containsPipe(pos))
            return;

        Set<BlockPos> connectedPipes = new HashSet<>();
        traverseCreateNetwork(world, pos, connectedPipes);

        N network = createNetwork(UUID.randomUUID());
        network.getPipes().addAll(connectedPipes);

        updateConnectedBlocks(world, network);

        for (BlockPos connectedPipe : connectedPipes) {
            addPipe(connectedPipe, network.getId());
        }

        addNetwork(network);
    }

    protected void traverseCreateNetwork(ServerLevel world, BlockPos pos, Set<BlockPos> visited) {
        if (!isPipe(world, pos) || visited.contains(pos))
            return;

        visited.add(pos);

        for (Direction direction : Direction.values()) {
            BlockPos offset = pos.relative(direction);
            traverseCreateNetwork(world, offset, visited);
        }
    }

    public Map<BlockPos, UUID> getPipeToNetworkId() {
        return this.pipeToNetworkId;
    }
}
