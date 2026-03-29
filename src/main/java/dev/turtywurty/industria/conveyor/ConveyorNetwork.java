package dev.turtywurty.industria.conveyor;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.turtywurty.industria.conveyor.block.*;
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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ConveyorNetwork {
    private static final String LEGACY_OUTPUT_ID = "out";
    private static final Codec<Map<BlockPos, Map<String, BlockPos>>> CONNECTED_BLOCKS_CODEC = Codec.either(
            Codec.unboundedMap(ExtraCodecs.BLOCK_POS_STRING_CODEC, Codec.unboundedMap(Codec.STRING, BlockPos.CODEC)),
            Codec.unboundedMap(ExtraCodecs.BLOCK_POS_STRING_CODEC, BlockPos.CODEC)
    ).xmap(
            connectedBlocks -> connectedBlocks.map(ConveyorNetwork::copyConnectedBlocks, ConveyorNetwork::upgradeLegacyConnectedBlocks),
            connectedBlocks -> Either.left(copyConnectedBlocks(connectedBlocks))
    );
    private static final StreamCodec<ByteBuf, Map<BlockPos, Map<String, BlockPos>>> CONNECTED_BLOCKS_STREAM_CODEC =
            ByteBufCodecs.map(HashMap::new, BlockPos.STREAM_CODEC, ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, BlockPos.STREAM_CODEC));

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
    protected final Map<BlockPos, Map<String, BlockPos>> connectedBlocks = new ConcurrentHashMap<>();
    private final ConveyorNetworkStorage storage = new ConveyorNetworkStorage();

    public ConveyorNetwork(UUID id) {
        this.id = id;
    }

    public ConveyorNetwork() {
        this(UUID.randomUUID());
    }

    public ConveyorNetwork copy() {
        var network = new ConveyorNetwork(this.id);
        network.conveyors.addAll(this.conveyors);
        this.connectedBlocks.forEach((conveyorPos, attachments) ->
                network.connectedBlocks.put(conveyorPos, new ConcurrentHashMap<>(attachments)));
        this.storage.getStorages().forEach((pos, conveyorStorage) ->
                network.storage.getStorages().put(pos, conveyorStorage.copy()));
        return network;
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

    public Map<BlockPos, Map<String, BlockPos>> getConnectedBlocks() {
        return connectedBlocks;
    }

    protected void onConnectedBlocksChanged(Level level) {
        // NO-OP
    }

    public void clearConnectedBlocks(Level level) {
        this.connectedBlocks.clear();
        onConnectedBlocksChanged(level);
    }

    public void addConnectedBlock(Level level, BlockPos conveyorPos, String outputId, BlockPos pos) {
        this.connectedBlocks.computeIfAbsent(conveyorPos, _ -> new ConcurrentHashMap<>()).put(outputId, pos);
        onConnectedBlocksChanged(level);
    }

    public void removeConnectedBlock(Level level, BlockPos pos) {
        this.connectedBlocks.entrySet().removeIf(entry -> {
            entry.getValue().values().removeIf(pos::equals);
            return entry.getValue().isEmpty();
        });
        onConnectedBlocksChanged(level);
    }

    public void addConnectedBlocks(Level level, ConveyorNetwork network) {
        network.connectedBlocks.forEach((conveyorPos, attachments) ->
                this.connectedBlocks.computeIfAbsent(conveyorPos, _ -> new ConcurrentHashMap<>()).putAll(attachments));
        onConnectedBlocksChanged(level);
    }

    public void removeConnectedBlocks(Level level, BlockPos... connectedBlocks) {
        Set<BlockPos> removedBlocks = Set.of(connectedBlocks);
        for (BlockPos blockPos : connectedBlocks) {
            this.connectedBlocks.remove(blockPos);
        }

        this.connectedBlocks.entrySet().removeIf(entry -> {
            entry.getValue().values().removeIf(removedBlocks::contains);
            return entry.getValue().isEmpty();
        });

        onConnectedBlocksChanged(level);
    }

    public void removeConnectedBlocks(Level level, ConveyorNetwork network) {
        for (Map.Entry<BlockPos, Map<String, BlockPos>> entry : network.connectedBlocks.entrySet()) {
            Map<String, BlockPos> attachments = this.connectedBlocks.get(entry.getKey());
            if (attachments == null)
                continue;

            attachments.keySet().removeAll(entry.getValue().keySet());
            if (attachments.isEmpty()) {
                this.connectedBlocks.remove(entry.getKey());
            }
        }
        onConnectedBlocksChanged(level);
    }

    public void tick(Level level, ConveyorRoutingState routingState) {
        for (ConveyorStorage conveyorStorage : this.storage.getStorages().values()) {
            BlockPos pos = conveyorStorage.getPos();
            for (ConveyorItem item : conveyorStorage.getItems()) {
                prepareItemForConveyor(level, pos, item, routingState);
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
                hash = 31 * hash + Objects.hashCode(item.getSelectedOutputId());
                hash = 31 * hash + Objects.hashCode(item.getSelectedAnchorRouteId());
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
            BlockState nextState = level.getBlockState(nextPos);
            BlockPos nextInputPos = selectedOutput.output().expectedInputPos();
            if (nextStorage != null
                    && canTargetAcceptIncomingItem(level, nextPos, nextState, item, nextInputPos, routingState)
                    && nextStorage.addItem(item)) {
                onIncomingItemAccepted(level, nextPos, nextState, item, nextInputPos, routingState);
                prepareItemForInputRoute(level, nextPos, item, selectedOutput.output().expectedInputPos());
                prepareItemForConveyor(level, nextPos, item, routingState);
                conveyorStorage.removeItems(item);
                onOutputUsed(level, conveyorPos, state, selectedOutput.output(), item, routingState);
            }

            return;
        }

        ItemStack stack = item.getStack();
        BlockPos connectedBlock = getConnectedBlock(conveyorPos, selectedOutput.output());
        if (connectedBlock != null
                && connectedBlock.equals(selectedOutput.output().deliveryPos())
                && canOutputToStorage(level, conveyorPos, state, item, selectedOutput.output(), connectedBlock, routingState)) {
            BlockApiLookup<Storage<ItemVariant>, @Nullable Direction> blockLookup = TransferType.ITEM.getBlockLookup();
            Direction insertSide = selectedOutput.output().inventoryInsertSide();
            Storage<ItemVariant> connectedBlockStorage = blockLookup.find(level, connectedBlock, insertSide);
            if (connectedBlockStorage != null) {
                try (Transaction transaction = Transaction.openOuter()) {
                    long inserted = connectedBlockStorage.insert(ItemVariant.of(stack), stack.getCount(), transaction);
                    if (inserted >= stack.getCount()) {
                        conveyorStorage.removeItems(item);
                        transaction.commit();
                        onOutputUsed(level, conveyorPos, state, selectedOutput.output(), item, routingState);
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
        onOutputUsed(level, conveyorPos, state, selectedOutput.output(), item, routingState);
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

        return new SelectedOutput(output, getNextConveyor(level, conveyorPos, state, output));
    }

    @Nullable
    private BlockPos getNextConveyor(Level level, BlockPos sourcePos, BlockState sourceState, ConveyorOutput output) {
        if (!(sourceState.getBlock() instanceof ConveyorLike sourceConveyor))
            return null;

        for (BlockPos nextPos : getCandidateOutputTargets(output.deliveryPos())) {
            BlockState nextState = level.getBlockState(nextPos);
            if (sourceConveyor.canConnectToConveyor(level, sourcePos, sourceState, output, nextPos, nextState))
                return nextPos;
        }

        return null;
    }

    private static void onOutputUsed(Level level, BlockPos conveyorPos, BlockState state, ConveyorOutput output, ConveyorItem item, ConveyorRoutingState routingState) {
        if (state.getBlock() instanceof ConveyorLike conveyor) {
            conveyor.onOutputUsed(level, conveyorPos, state, output, item, routingState);
        }
    }

    private boolean canTargetAcceptIncomingItem(Level level, BlockPos conveyorPos, BlockState state, ConveyorItem item, BlockPos inputPos,
                                                ConveyorRoutingState routingState) {
        return state.getBlock() instanceof ConveyorLike conveyor
                && conveyor.canAcceptIncomingItem(level, conveyorPos, state, item, inputPos, this, routingState);
    }

    private void onIncomingItemAccepted(Level level, BlockPos conveyorPos, BlockState state, ConveyorItem item, BlockPos inputPos,
                                        ConveyorRoutingState routingState) {
        if (state.getBlock() instanceof ConveyorLike conveyor) {
            conveyor.onIncomingItemAccepted(level, conveyorPos, state, item, inputPos, this, routingState);
        }
    }

    private boolean canOutputToStorage(Level level, BlockPos conveyorPos, BlockState state, ConveyorItem item, ConveyorOutput output,
                                       BlockPos storagePos, ConveyorRoutingState routingState) {
        return state.getBlock() instanceof ConveyorLike conveyor
                && conveyor.canOutputToStorage(level, conveyorPos, state, item, output, storagePos, this, routingState);
    }

    private void prepareItemForConveyor(Level level, BlockPos conveyorPos, ConveyorItem item, ConveyorRoutingState routingState) {
        BlockState state = level.getBlockState(conveyorPos);
        if (!(state.getBlock() instanceof ConveyorLike conveyor)) {
            item.setSelectedOutputId(null);
            return;
        }

        ConveyorTopology topology = conveyor.getTopology(level, conveyorPos, state);
        if (topology.outputs().size() <= 1) {
            item.setSelectedOutputId(null);
            return;
        }

        ConveyorOutput output = conveyor.selectOutput(level, conveyorPos, state, item, this, routingState);
        if (output != null) {
            item.setSelectedOutputId(output.id());
        }
    }

    private void prepareItemForInputRoute(Level level, BlockPos conveyorPos, ConveyorItem item, BlockPos inputPos) {
        BlockState state = level.getBlockState(conveyorPos);
        if (!(state.getBlock() instanceof ConveyorLike conveyor)) {
            item.setSelectedAnchorRouteId(null);
            return;
        }

        ConveyorTopology topology = conveyor.getTopology(level, conveyorPos, state);
        ConveyorInput input = topology.getInputFrom(inputPos);
        item.setSelectedAnchorRouteId(input != null ? input.id() : null);
    }

    public boolean hasReadyItemForInput(Level level, BlockPos conveyorPos, BlockPos inputPos) {
        BlockState state = level.getBlockState(conveyorPos);
        ConveyorTopology topology = getTopology(level, conveyorPos, state);
        if (topology == null)
            return false;

        ConveyorInput input = topology.getInputFrom(inputPos);
        if (input == null)
            return false;

        ConveyorStorage sourceStorage = this.storage.getStorages().get(input.expectedSourcePos());
        if (sourceStorage == null)
            return false;

        BlockState sourceState = level.getBlockState(input.expectedSourcePos());
        for (ConveyorItem sourceItem : sourceStorage.getItems()) {
            if (sourceItem.getProgress() < ConveyorStorage.MAX_PROGRESS)
                continue;

            if (targetsConveyor(level, input.expectedSourcePos(), sourceState, sourceItem, conveyorPos))
                return true;
        }

        return false;
    }

    private boolean targetsConveyor(Level level, BlockPos sourcePos, BlockState sourceState, ConveyorItem item, BlockPos targetPos) {
        ConveyorOutput output = getSelectedOutputForItem(level, sourcePos, sourceState, item);
        if (output == null)
            return false;

        BlockPos nextConveyorPos = getNextConveyor(level, sourcePos, sourceState, output);
        return targetPos.equals(nextConveyorPos);
    }

    @Nullable
    private ConveyorOutput getSelectedOutputForItem(Level level, BlockPos conveyorPos, BlockState state, ConveyorItem item) {
        if (!isConveyor(state) || !(state.getBlock() instanceof ConveyorLike conveyor))
            return null;

        ConveyorTopology topology = conveyor.getTopology(level, conveyorPos, state);
        if (topology.outputs().isEmpty())
            return null;

        String selectedOutputId = item.getSelectedOutputId();
        if (selectedOutputId != null) {
            for (ConveyorOutput output : topology.outputs()) {
                if (output.id().equals(selectedOutputId))
                    return output;
            }
        }

        return topology.outputs().size() == 1 ? topology.outputs().getFirst() : null;
    }

    @Nullable
    public BlockPos getConnectedBlock(BlockPos conveyorPos, ConveyorOutput output) {
        Map<String, BlockPos> attachedOutputs = this.connectedBlocks.get(conveyorPos);
        if (attachedOutputs == null)
            return null;

        return attachedOutputs.get(output.id());
    }

    private record SelectedOutput(ConveyorOutput output, @Nullable BlockPos nextConveyorPos) {
    }

    public Storage<ItemVariant> getItemStorage(Level level, BlockPos pos) {
        ConveyorStorage storage = this.storage.getStorageAt(level, pos);
        if (storage != null)
            return storage.getItemStorage();

        return null;
    }

    private static Map<BlockPos, Map<String, BlockPos>> upgradeLegacyConnectedBlocks(Map<BlockPos, BlockPos> legacyConnectedBlocks) {
        Map<BlockPos, Map<String, BlockPos>> upgraded = new HashMap<>();
        legacyConnectedBlocks.forEach((conveyorPos, connectedBlock) -> upgraded.put(
                conveyorPos,
                new HashMap<>(Map.of(LEGACY_OUTPUT_ID, connectedBlock))
        ));
        return upgraded;
    }

    private static Map<BlockPos, Map<String, BlockPos>> copyConnectedBlocks(Map<BlockPos, Map<String, BlockPos>> connectedBlocks) {
        Map<BlockPos, Map<String, BlockPos>> copy = new HashMap<>();
        connectedBlocks.forEach((conveyorPos, attachments) -> copy.put(conveyorPos, new HashMap<>(attachments)));
        return copy;
    }
}
