package dev.turtywurty.industria.screenhandler.base;

import dev.turtywurty.industria.blockentity.util.inventory.ClientWrappedContainerStorage;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorage;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorageHolder;
import dev.turtywurty.industria.network.HasPositionPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public abstract class IndustriaScreenHandler<T extends BlockEntity & WrappedContainerStorageHolder, P extends HasPositionPayload> extends AbstractContainerMenu {
    protected final T blockEntity;
    protected final WrappedContainerStorage<?> wrappedContainerStorage;
    protected final ContainerLevelAccess context;
    protected final ContainerData propertyDelegate;

    public IndustriaScreenHandler(MenuType<?> type, int syncId, Inventory playerInventory, P payload, Class<T> blockEntityClass) {
        this(type, 0, syncId, playerInventory, payload, new CachedBlockEntityFetcher<>(blockEntityClass));
    }

    public IndustriaScreenHandler(MenuType<?> type, int propertiesSize, int syncId, Inventory playerInventory, P payload, Class<T> blockEntityClass) {
        this(type, propertiesSize, syncId, playerInventory, payload, new CachedBlockEntityFetcher<>(blockEntityClass));
    }

    public IndustriaScreenHandler(MenuType<?> type, int propertiesSize, int syncId, Inventory playerInventory, P payload, CachedBlockEntityFetcher<T> cachedFetcher) {
        this(type, syncId, playerInventory,
                cachedFetcher.apply(playerInventory, payload.pos()),
                ClientWrappedContainerStorage.copyOf(cachedFetcher.apply(playerInventory, payload.pos()).getWrappedContainerStorage()),
                propertiesSize == 0 ? null : new SimpleContainerData(propertiesSize));
    }

    public IndustriaScreenHandler(MenuType<?> type, int syncId, Inventory playerInventory, T blockEntity, WrappedContainerStorage<?> wrappedContainerStorage) {
        this(type, syncId, playerInventory, blockEntity, wrappedContainerStorage, null);
    }

    public IndustriaScreenHandler(MenuType<?> type, int syncId, Inventory playerInventory, T blockEntity, WrappedContainerStorage<?> wrappedContainerStorage, ContainerData properties) {
        super(type, syncId);
        if (properties != null && properties.getCount() >= 0) {
            this.propertyDelegate = properties;
            checkContainerDataCount(this.propertyDelegate, properties.getCount());
            addDataSlots(this.propertyDelegate);
        } else {
            this.propertyDelegate = new SimpleContainerData(0);
        }

        this.context = ContainerLevelAccess.create(playerInventory.player.level(), blockEntity.getBlockPos());
        this.blockEntity = blockEntity;
        this.wrappedContainerStorage = wrappedContainerStorage;
        this.wrappedContainerStorage.checkSize(getInventorySize());

        addBlockEntitySlots(playerInventory);
        addStandardInventorySlots(playerInventory, getPlayerInventoryX(), getPlayerInventoryY());

        this.wrappedContainerStorage.onOpen(playerInventory.player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);
        if (!slot.hasItem()) {
            return stack;
        }

        ItemStack stackInSlot = slot.getItem();
        stack = stackInSlot.copy();

        if (slotIndex < getInventorySize()) {
            if (!moveItemStackTo(stackInSlot, this.slots.size() - 9, this.slots.size(), true)) {
                if (!moveItemStackTo(stackInSlot, this.slots.size() - 36, this.slots.size() - 9, false)) {
                    return ItemStack.EMPTY;
                }
            }
        } else {
            if (!moveItemStackTo(stackInSlot, 0, getInventorySize(), false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stackInSlot.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        return stack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.wrappedContainerStorage.onClose(player);
    }

    @Override
    public boolean stillValid(Player player) {
        boolean validBlock = false;
        for (Block block : getValidBlocks()) {
            if (stillValid(this.context, player, block)) {
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

    protected abstract void addBlockEntitySlots(Inventory playerInventory);

    public static class CachedBlockEntityFetcher<T extends BlockEntity & WrappedContainerStorageHolder> implements BiFunction<Inventory, BlockPos, T> {
        private final Class<T> blockEntityClass;
        private final Map<BlockPos, T> cache = new HashMap<>();

        public CachedBlockEntityFetcher(Class<T> blockEntityClass) {
            this.blockEntityClass = blockEntityClass;
        }

        @Override
        public T apply(Inventory playerInventory, BlockPos pos) {
            if (cache.containsKey(pos)) {
                return cache.get(pos);
            }

            BlockEntity blockEntity = playerInventory.player.level().getBlockEntity(pos);
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
