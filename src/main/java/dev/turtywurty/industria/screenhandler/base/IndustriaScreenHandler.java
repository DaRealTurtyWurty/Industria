package dev.turtywurty.industria.screenhandler.base;

import dev.turtywurty.industria.blockentity.util.inventory.ClientWrappedInventoryStorage;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedInventoryStorage;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedInventoryStorageHolder;
import dev.turtywurty.industria.network.HasPositionPayload;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.*;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public abstract class IndustriaScreenHandler<T extends BlockEntity & WrappedInventoryStorageHolder, P extends HasPositionPayload> extends ScreenHandler {
    protected final T blockEntity;
    protected final WrappedInventoryStorage<?> wrappedInventoryStorage;
    protected final ScreenHandlerContext context;
    protected final PropertyDelegate propertyDelegate;

    public IndustriaScreenHandler(ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, P payload, Class<T> blockEntityClass) {
        this(type, 0, syncId, playerInventory, payload, new CachedBlockEntityFetcher<>(blockEntityClass));
    }

    public IndustriaScreenHandler(ScreenHandlerType<?> type, int propertiesSize, int syncId, PlayerInventory playerInventory, P payload, Class<T> blockEntityClass) {
        this(type, propertiesSize, syncId, playerInventory, payload, new CachedBlockEntityFetcher<>(blockEntityClass));
    }

    public IndustriaScreenHandler(ScreenHandlerType<?> type, int propertiesSize, int syncId, PlayerInventory playerInventory, P payload, CachedBlockEntityFetcher<T> cachedFetcher) {
        this(type, syncId, playerInventory,
                cachedFetcher.apply(playerInventory, payload.pos()),
                ClientWrappedInventoryStorage.copyOf(cachedFetcher.apply(playerInventory, payload.pos()).getWrappedInventoryStorage()),
                propertiesSize == 0 ? null : new ArrayPropertyDelegate(propertiesSize));
    }

    public IndustriaScreenHandler(ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, T blockEntity, WrappedInventoryStorage<?> wrappedInventoryStorage) {
        this(type, syncId, playerInventory, blockEntity, wrappedInventoryStorage, null);
    }

    public IndustriaScreenHandler(ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, T blockEntity, WrappedInventoryStorage<?> wrappedInventoryStorage, PropertyDelegate properties) {
        super(type, syncId);
        if (properties != null && properties.size() >= 0) {
            this.propertyDelegate = properties;
            checkDataCount(this.propertyDelegate, properties.size());
            addProperties(this.propertyDelegate);
        } else {
            this.propertyDelegate = new ArrayPropertyDelegate(0);
        }

        this.context = ScreenHandlerContext.create(playerInventory.player.getWorld(), blockEntity.getPos());
        this.blockEntity = blockEntity;
        this.wrappedInventoryStorage = wrappedInventoryStorage;
        this.wrappedInventoryStorage.checkSize(getInventorySize());

        addBlockEntitySlots(playerInventory);
        addPlayerSlots(playerInventory, getPlayerInventoryX(), getPlayerInventoryY());

        this.wrappedInventoryStorage.onOpen(playerInventory.player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);
        if (!slot.hasStack()) {
            return stack;
        }

        ItemStack stackInSlot = slot.getStack();
        stack = stackInSlot.copy();

        if (slotIndex < getInventorySize()) {
            if (!insertItem(stackInSlot, this.slots.size() - 9, this.slots.size(), true)) {
                if (!insertItem(stackInSlot, this.slots.size() - 36, this.slots.size() - 9, false)) {
                    return ItemStack.EMPTY;
                }
            }
        } else {
            if (!insertItem(stackInSlot, 0, getInventorySize(), false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stackInSlot.isEmpty()) {
            slot.setStack(ItemStack.EMPTY);
        } else {
            slot.markDirty();
        }

        return stack;
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.wrappedInventoryStorage.onClose(player);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        boolean validBlock = false;
        for (Block block : getValidBlocks()) {
            if (canUse(this.context, player, block)) {
                validBlock = true;
                break;
            }
        }

        return validBlock;
    }

    public T getBlockEntity() {
        return this.blockEntity;
    }

    protected List<Block> getValidBlocks() {
        return List.of();
    }

    protected int getPlayerInventoryX() {
        return 8;
    }

    protected int getPlayerInventoryY() {
        return 84;
    }

    protected abstract int getInventorySize();

    protected abstract void addBlockEntitySlots(PlayerInventory playerInventory);

    public static class CachedBlockEntityFetcher<T extends BlockEntity & WrappedInventoryStorageHolder> implements BiFunction<PlayerInventory, BlockPos, T> {
        private final Class<T> blockEntityClass;
        private final Map<BlockPos, T> cache = new HashMap<>();

        public CachedBlockEntityFetcher(Class<T> blockEntityClass) {
            this.blockEntityClass = blockEntityClass;
        }

        @Override
        public T apply(PlayerInventory playerInventory, BlockPos pos) {
            if (cache.containsKey(pos)) {
                return cache.get(pos);
            }

            BlockEntity blockEntity = playerInventory.player.getWorld().getBlockEntity(pos);
            if (blockEntityClass.isInstance(blockEntity)) {
                T castedBlockEntity = blockEntityClass.cast(blockEntity);
                cache.put(pos, castedBlockEntity);
                return castedBlockEntity;
            } else {
                throw new IllegalArgumentException("Block entity at " + pos + " is not of type " + blockEntityClass.getSimpleName());
            }
        }
    }
}
