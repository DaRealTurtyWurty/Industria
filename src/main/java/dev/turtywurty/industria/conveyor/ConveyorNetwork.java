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
import dev.turtywurty.industria.util.ExtraCodecs;
import dev.turtywurty.industria.util.ExtraStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ConveyorNetwork {
    private static final Codec<Map<BlockPos, BlockPos>> CONNECTED_BLOCKS_CODEC =
            Codec.unboundedMap(ExtraCodecs.BLOCK_POS_STRING_CODEC, BlockPos.CODEC);
    private static final StreamCodec<ByteBuf, Map<BlockPos, BlockPos>> CONNECTED_BLOCKS_STREAM_CODEC =
            ByteBufCodecs.map(HashMap::new, BlockPos.STREAM_CODEC, BlockPos.STREAM_CODEC);

    public static final MapCodec<ConveyorNetwork> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    UUIDUtil.CODEC.fieldOf("id").forGetter(ConveyorNetwork::getId),
                    BlockPos.CODEC.listOf().fieldOf("conveyors").forGetter(ConveyorNetwork::getConveyors),
                    CONNECTED_BLOCKS_CODEC.fieldOf("connectedBlocks").forGetter(ConveyorNetwork::getConnectedBlocks),
                    ConveyorNetworkStorage.CODEC.fieldOf("storage").forGetter(network -> network.storage)
            ).apply(instance, (id, conveyors, connectedBlocks, storage) -> {
                var network = new ConveyorNetwork(id);
                network.conveyors.addAll(conveyors);
                network.connectedBlocks.putAll(connectedBlocks);
                network.storage.loadFrom(storage);

                return network;
            }));
    public static final StreamCodec<RegistryFriendlyByteBuf, ConveyorNetwork> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, ConveyorNetwork::getId,
            ExtraStreamCodecs.listOf(BlockPos.STREAM_CODEC), ConveyorNetwork::getConveyors,
            CONNECTED_BLOCKS_STREAM_CODEC, ConveyorNetwork::getConnectedBlocks,
            ConveyorNetworkStorage.STREAM_CODEC, ConveyorNetwork::getStorage,
            (id, conveyors, connectedBlocks, storage) -> {
                var network = new ConveyorNetwork(id);
                network.conveyors.addAll(conveyors);
                network.connectedBlocks.putAll(connectedBlocks);
                network.storage.loadFrom(storage);

                return network;
            }
    );

    protected UUID id;
    protected final List<BlockPos> conveyors = new CopyOnWriteArrayList<>();
    protected final Map<BlockPos, BlockPos> connectedBlocks = new ConcurrentHashMap<>();
    private final ConveyorNetworkStorage storage = new ConveyorNetworkStorage();

    public ConveyorNetwork(UUID id) {
        this.id = id;
    }

    public ConveyorNetwork() {
        this(UUID.randomUUID());
    }

    public UUID getId() {
        return id;
    }

    public List<BlockPos> getConveyors() {
        return conveyors;
    }

    public void addConveyor(BlockPos pos) {
        conveyors.add(pos);
    }

    public void removeConveyor(BlockPos pos) {
        conveyors.remove(pos);
    }

    public ConveyorNetworkStorage getStorage() {
        return storage;
    }

    public void moveConveyorsFrom(ConveyorNetwork oldNetwork, Set<BlockPos> conveyorsToInherit) {
        for (BlockPos conveyor : conveyorsToInherit) {
            addConveyor(conveyor);
        }
    }

    public Map<BlockPos, BlockPos> getConnectedBlocks() {
        return connectedBlocks;
    }

    protected void onConnectedBlocksChanged(Level level) {
        // NO-OP
    }

    public void clearConnectedBlocks(Level level) {
        this.connectedBlocks.clear();
        onConnectedBlocksChanged(level);
    }

    public void addConnectedBlock(Level level, BlockPos connection, BlockPos pos) {
        this.connectedBlocks.put(connection, pos);
        onConnectedBlocksChanged(level);
    }

    public void removeConnectedBlock(Level level, BlockPos pos) {
        this.connectedBlocks.entrySet().stream()
                .filter(entry -> entry.getValue().equals(pos))
                .findFirst()
                .ifPresent(entry -> this.connectedBlocks.remove(entry.getKey()));
        onConnectedBlocksChanged(level);
    }

    public void addConnectedBlocks(Level level, ConveyorNetwork network) {
        this.connectedBlocks.putAll(network.connectedBlocks);
        onConnectedBlocksChanged(level);
    }

    public void removeConnectedBlocks(Level level, BlockPos... connectedBlocks) {
        for (BlockPos blockPos : connectedBlocks) {
            this.connectedBlocks.remove(blockPos);
        }

        onConnectedBlocksChanged(level);
    }

    public void removeConnectedBlocks(Level level, ConveyorNetwork network) {
        network.connectedBlocks.forEach(this.connectedBlocks::remove);
        onConnectedBlocksChanged(level);
    }

    public void tick(Level level, ConveyorRoutingState routingState) {
        for (ConveyorStorage conveyorStorage : this.storage.getStorages().values()) {
            BlockPos pos = conveyorStorage.getPos();
            for (ConveyorItem item : conveyorStorage.getItems()) {
                int itemProgress = item.getProgress();
                if (itemProgress >= ConveyorStorage.MAX_PROGRESS) {
                    handleItemMaxProgress(conveyorStorage, item, level, pos, routingState);
                } else {
                    handleItemProgress(level, conveyorStorage, item, itemProgress, pos);
                }
            }
        }
    }

    public int getStorageStateHash() {
        int hash = 1;
        List<Map.Entry<BlockPos, ConveyorStorage>> storages = new ArrayList<>(this.storage.getStorages().entrySet());
        storages.sort(Comparator.comparingLong(entry -> entry.getKey().asLong()));
        for (Map.Entry<BlockPos, ConveyorStorage> storageEntry : storages) {
            hash = 31 * hash + storageEntry.getKey().hashCode();

            List<ConveyorItem> items = storageEntry.getValue().getItems();
            hash = 31 * hash + items.size();
            for (ConveyorItem item : items) {
                hash = 31 * hash + item.getId().hashCode();
                hash = 31 * hash + item.getProgress();
                hash = 31 * hash + item.getStack().hashCode();
            }
        }

        return hash;
    }

    private static void handleItemProgress(Level level, ConveyorStorage conveyorStorage, ConveyorItem item, int itemProgress, BlockPos pos) {
        int newProgress = Math.clamp(itemProgress + getConveyorSpeed(level, pos), 0, ConveyorStorage.MAX_PROGRESS);
        if (conveyorStorage.canMove(item, itemProgress, newProgress)) {
            item.setProgress(newProgress);
        } else {
            item.setProgress(itemProgress);
        }
    }

    private void handleItemMaxProgress(ConveyorStorage conveyorStorage, ConveyorItem item, Level level, BlockPos conveyorPos, ConveyorRoutingState routingState) {
        BlockState state = level.getBlockState(conveyorPos);
        SelectedOutput selectedOutput = selectOutput(level, conveyorPos, state, item, routingState);
        if (selectedOutput == null)
            return;

        if (selectedOutput.nextConveyorPos() != null) {
            BlockPos nextPos = selectedOutput.nextConveyorPos();
            ConveyorStorage nextStorage = this.storage.getStorageAt(level, nextPos);
            if (nextStorage != null && nextStorage.addItem(item)) {
                conveyorStorage.removeItems(item);
                onOutputUsed(level, conveyorPos, state, selectedOutput.output(), routingState);
            }

            return;
        }

        ItemStack stack = item.getStack();
        BlockPos connectedBlock = this.connectedBlocks.get(conveyorPos);
        if (connectedBlock != null && connectedBlock.equals(selectedOutput.output().deliveryPos())) {
            BlockApiLookup<Storage<ItemVariant>, @Nullable Direction> blockLookup = TransferType.ITEM.getBlockLookup();
            Direction insertSide = selectedOutput.output().inventoryInsertSide();
            Storage<ItemVariant> connectedBlockStorage = blockLookup.find(level, connectedBlock, insertSide);
            if (connectedBlockStorage != null) {
                try (Transaction transaction = Transaction.openOuter()) {
                    long inserted = connectedBlockStorage.insert(ItemVariant.of(stack), stack.getCount(), transaction);
                    if (inserted >= stack.getCount()) {
                        conveyorStorage.removeItems(item);
                        transaction.commit();
                        onOutputUsed(level, conveyorPos, state, selectedOutput.output(), routingState);
                    } else if (inserted > 0) {
                        stack.shrink((int) inserted);
                        item.setStack(stack);
                        transaction.commit();
                    } else {
                        transaction.abort();
                    }
                }

                return;
            }
        }

        if (!isConveyor(state))
            return;

        BlockPos dropPos = selectedOutput.output().deliveryPos();
        Containers.dropItemStack(level, dropPos.getX(), dropPos.getY(), dropPos.getZ(), stack);
        conveyorStorage.removeItems(item);
        onOutputUsed(level, conveyorPos, state, selectedOutput.output(), routingState);
    }

    public static boolean isConveyor(BlockState state) {
        return state.is(TagList.Blocks.CONVEYORS);
    }

    public static boolean isConveyor(Level level, BlockPos pos) {
        return isConveyor(level.getBlockState(pos));
    }

    private static List<BlockPos> getCandidateOutputTargets(BlockPos outputPos) {
        return List.of(outputPos, outputPos.above(), outputPos.below());
    }

    private static int getConveyorSpeed(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.getBlock() instanceof ConveyorLike conveyorBlock
                ? conveyorBlock.getSpeed(level, pos, state)
                : 0;
    }

    @Nullable
    private static ConveyorTopology getTopology(Level level, BlockPos pos, BlockState state) {
        return state.getBlock() instanceof ConveyorLike conveyor
                ? conveyor.getTopology(level, pos, state)
                : null;
    }

    @Nullable
    private SelectedOutput selectOutput(Level level, BlockPos conveyorPos, BlockState state, ConveyorItem item, ConveyorRoutingState routingState) {
        if (!isConveyor(state) || !(state.getBlock() instanceof ConveyorLike conveyor))
            return null;

        ConveyorTopology topology = conveyor.getTopology(level, conveyorPos, state);
        if (topology.outputs().isEmpty())
            return null;

        ConveyorOutput output = conveyor.selectOutput(level, conveyorPos, state, item, this, routingState);
        if (output == null)
            return null;

        return new SelectedOutput(output, getNextConveyor(level, output));
    }

    @Nullable
    private BlockPos getNextConveyor(Level level, ConveyorOutput output) {
        for (BlockPos nextPos : getCandidateOutputTargets(output.deliveryPos())) {
            BlockState nextState = level.getBlockState(nextPos);
            ConveyorTopology nextTopology = getTopology(level, nextPos, nextState);
            if (nextTopology != null && nextTopology.acceptsInputFrom(output.expectedInputPos()))
                return nextPos;
        }

        return null;
    }

    private static void onOutputUsed(Level level, BlockPos conveyorPos, BlockState state, ConveyorOutput output, ConveyorRoutingState routingState) {
        if (state.getBlock() instanceof ConveyorLike conveyor) {
            conveyor.onOutputUsed(level, conveyorPos, state, output, routingState);
        }
    }

    private record SelectedOutput(ConveyorOutput output, @Nullable BlockPos nextConveyorPos) {
    }

    public Storage<ItemVariant> getItemStorage(Level level, BlockPos pos) {
        ConveyorStorage storage = this.storage.getStorageAt(level, pos);
        if (storage != null)
            return storage.getItemStorage();

        return null;
    }
}
