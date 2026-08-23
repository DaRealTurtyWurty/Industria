package dev.turtywurty.industria.menu;

import dev.turtywurty.industria.blockentity.FluidTankBlockEntity;
import dev.turtywurty.industria.blockentity.util.fluid.SyncingFluidStorage;
import dev.turtywurty.industria.blockentity.util.inventory.PredicateSimpleInventory;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.init.ModMenuTypes;
import dev.turtywurty.industria.menu.base.TickableScreenHandler;
import dev.turtywurty.industria.menu.slot.PredicateSlot;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.util.TransferUtils;
import dev.turtywurty.turtymultiloader.transfer.lookup.MutableItemContext;
import dev.turtywurty.turtymultiloader.transfer.lookup.StorageKeys;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import dev.turtywurty.turtymultiloader.transfer.storage.ResourceStorage;
import dev.turtywurty.turtymultiloader.transfer.transaction.TransferTransaction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

import java.util.Optional;

public class FluidTankScreenHandler extends AbstractContainerMenu implements TickableScreenHandler {
    private final FluidTankBlockEntity blockEntity;
    private final ContainerLevelAccess context;
    private final SimpleContainer inventory;

    public FluidTankScreenHandler(int syncId, Inventory playerInventory, BlockPosPayload payload) {
        this(syncId, playerInventory, (FluidTankBlockEntity) playerInventory.player.level().getBlockEntity(payload.pos()), new SimpleContainer(1));
    }

    public FluidTankScreenHandler(int syncId, Inventory playerInventory, FluidTankBlockEntity blockEntity) {
        this(syncId, playerInventory, blockEntity, new PredicateSimpleInventory(
                blockEntity,
                1,
                PredicateSimpleInventory.createEmptyFluidPredicate(() -> blockEntity.getFluidTank().getResource())));
    }

    public FluidTankScreenHandler(int syncId, Inventory playerInventory, FluidTankBlockEntity blockEntity, SimpleContainer inventory) {
        super(ModMenuTypes.FLUID_TANK.get(), syncId);

        this.blockEntity = blockEntity;
        this.context = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());
        this.inventory = inventory;

        addSlot(new PredicateSlot(this.inventory, 0, 81, 64));
        addStandardInventorySlots(playerInventory, 8, 92);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        Containers.dropContents(this.blockEntity.getLevel(), this.blockEntity.getBlockPos(), this.inventory);
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

        if (slotIndex < 1) {
            if (!moveItemStackTo(stackInSlot, this.slots.size() - 9, this.slots.size(), true)) {
                if (!moveItemStackTo(stackInSlot, this.slots.size() - 36, this.slots.size() - 9, false)) {
                    return ItemStack.EMPTY;
                }
            }
        } else {
            if (!moveItemStackTo(stackInSlot, 0, 1, false)) {
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

    public FluidTankBlockEntity getBlockEntity() {
        return this.blockEntity;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.context, player, ModBlocks.FLUID_TANK.get());
    }

    @Override
    public void tick(ServerPlayer player) {
        if (this.blockEntity == null || !this.blockEntity.hasLevel())
            return;

        boolean extractMode = this.blockEntity.isExtractMode();
        SyncingFluidStorage fluidTank = this.blockEntity.getFluidTank();
        if (fluidTank.getAmount() > 0 && extractMode) {
            ItemStack stack = this.inventory.getItem(0);
            if (stack.isEmpty())
                return;

            ResourceStorage<ResourceVariant<Fluid>> fluidStorage =
                    MutableItemContext.ofContainerSlot(this.inventory, 0).find(StorageKeys.FLUID);
            if (fluidStorage == null || !fluidStorage.supportsInsertion())
                return;

            try (TransferTransaction transaction = TransferTransaction.openRoot()) {
                long inserted = fluidStorage.insert(fluidTank.getResource(), fluidTank.getAmount(), transaction);
                if (inserted > 0) {
                    fluidTank.extractInternal(fluidTank.getResource(), inserted, transaction);
                    transaction.commit();
                    this.blockEntity.update();
                }
            }
        } else if (fluidTank.getAmount() <= 0 && !extractMode) {
            ItemStack stack = this.inventory.getItem(0);
            if (stack.isEmpty())
                return;

            ResourceStorage<ResourceVariant<Fluid>> fluidStorage =
                    MutableItemContext.ofContainerSlot(this.inventory, 0).find(StorageKeys.FLUID);
            if (fluidStorage == null || !fluidStorage.supportsExtraction())
                return;

            try (TransferTransaction transaction = TransferTransaction.openRoot()) {
                Optional<ResourceVariant<Fluid>> variantToExtract = TransferUtils.findFirstVariant(fluidStorage, fluidTank.getResource());
                if (variantToExtract.filter(variant -> !variant.isBlank()).isEmpty())
                    return;

                ResourceVariant<Fluid> fluidVariant = variantToExtract.get();
                long extracted = fluidStorage.extract(fluidVariant, fluidTank.getCapacity() - fluidTank.getAmount(), transaction);
                if (extracted > 0) {
                    fluidTank.insertInternal(fluidVariant, extracted, transaction);
                    transaction.commit();
                    this.blockEntity.update();
                }
            }
        }
    }
}
