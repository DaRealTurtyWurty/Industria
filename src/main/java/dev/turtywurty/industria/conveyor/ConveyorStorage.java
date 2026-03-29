package dev.turtywurty.industria.conveyor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.turtywurty.industria.conveyor.block.ConveyorLike;
import dev.turtywurty.industria.util.ExtraCodecs;
import dev.turtywurty.industria.util.ExtraStreamCodecs;
import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ConveyorStorage {
    public static final ConveyorStorage EMPTY = new ConveyorStorage();
    public static final MapCodec<ConveyorStorage> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(ConveyorStorage::getPos),
            Codec.INT.fieldOf("itemCount").forGetter(storage -> storage.getItemContainer().getContainerSize()),
            ExtraCodecs.listOf(ConveyorItem.CODEC).fieldOf("items").forGetter(ConveyorStorage::getItems)
    ).apply(instance, ConveyorStorage::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ConveyorStorage> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ConveyorStorage::getPos,
            ByteBufCodecs.INT, storage -> storage.getItemContainer().getContainerSize(),
            ExtraStreamCodecs.listOf(ConveyorItem.STREAM_CODEC), ConveyorStorage::getItems,
            ConveyorStorage::new
    );
    public static final int MAX_PROGRESS = 100;

    private final BlockPos pos;
    private final List<ConveyorItem> items = new CopyOnWriteArrayList<>();
    private final ConveyorItemContainer itemContainer;
    private final Storage<ItemVariant> itemStorage;

    public ConveyorStorage(BlockGetter level, BlockPos pos) {
        this.pos = pos;
        BlockState state = level.getBlockState(pos);
        this.itemContainer = new ConveyorItemContainer(
                state.getBlock() instanceof ConveyorLike conveyorBlock
                        ? conveyorBlock.getItemLimit(level, pos, state) : 0,
                this.items);
        this.itemStorage = ContainerStorage.of(this.itemContainer, null);
    }

    public ConveyorStorage(BlockPos pos, int itemLimit, List<ConveyorItem> items) {
        this.pos = pos;
        this.items.addAll(items);
        this.itemContainer = new ConveyorItemContainer(itemLimit, this.items);
        this.itemStorage = ContainerStorage.of(this.itemContainer, null);
    }

    private ConveyorStorage() {
        this.pos = null;
        this.itemContainer = null;
        this.itemStorage = null;
    }

    public BlockPos getPos() {
        return pos;
    }

    public List<ConveyorItem> getItems() {
        sanitizeItems();
        return items;
    }

    public ConveyorItemContainer getItemContainer() {
        return itemContainer;
    }

    public Storage<ItemVariant> getItemStorage() {
        return itemStorage;
    }

    public boolean addItems(ConveyorItem... items) {
        boolean insertedAny = false;
        for (ConveyorItem item : items) {
            insertedAny |= addItem(item);
        }

        return insertedAny;
    }

    public boolean addItem(ConveyorItem item) {
        if (item == null || item.getStack() == null || item.getStack().isEmpty())
            return false;

        if (isFull() || !canAcceptIncomingItem())
            return false;

        item.setPosition(this.pos);
        item.setProgress(0);
        item.setSelectedOutputId(null);
        item.setSelectedAnchorRouteId(null);
        this.items.add(item);
        return true;
    }

    public void removeItems(ConveyorItem... items) {
        this.items.removeAll(List.of(items));
    }

    public void clearItems() {
        this.items.clear();
    }

    public boolean isEmpty() {
        sanitizeItems();
        return this.items.isEmpty();
    }

    public boolean isFull() {
        sanitizeItems();
        return this.items.size() >= this.itemContainer.getContainerSize();
    }

    public boolean canAcceptIncomingItem() {
        sanitizeItems();
        if (isFull())
            return false;

        int minimumItemSpacing = getMinimumItemSpacing();
        for (ConveyorItem item : this.items) {
            if (item.getProgress() < minimumItemSpacing)
                return false;
        }

        return true;
    }

    public boolean canMove(ConveyorItem movingItem, int startProgress, int endProgress) {
        sanitizeItems();
        if (movingItem == null || startProgress == endProgress)
            return true;

        int minimumItemSpacing = getMinimumItemSpacing();
        if (endProgress > startProgress) {
            for (ConveyorItem item : this.items) {
                if (item == movingItem)
                    continue;

                int itemProgress = item.getProgress();
                if (itemProgress <= startProgress)
                    continue;

                if (itemProgress <= endProgress || itemProgress - endProgress < minimumItemSpacing)
                    return false;
            }

            return true;
        }

        for (ConveyorItem item : this.items) {
            if (item == movingItem)
                continue;

            int itemProgress = item.getProgress();
            if (itemProgress >= startProgress)
                continue;

            if (itemProgress >= endProgress || endProgress - itemProgress < minimumItemSpacing)
                return false;
        }

        return true;
    }

    public int getMinimumItemSpacing() {
        int itemLimit = this.itemContainer.getContainerSize();
        if (itemLimit <= 0)
            return MAX_PROGRESS;

        return Math.max(1, (int) Math.ceil(MAX_PROGRESS / (double) itemLimit));
    }

    public ConveyorStorage copy() {
        return new ConveyorStorage(
                this.pos,
                this.itemContainer.getContainerSize(),
                this.items.stream()
                        .map(ConveyorItem::copy)
                        .toList()
        );
    }

    private void sanitizeItems() {
        this.items.removeIf(item -> item == null || item.getStack() == null || item.getStack().isEmpty());
    }
}
