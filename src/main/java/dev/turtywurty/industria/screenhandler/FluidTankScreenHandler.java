package dev.turtywurty.industria.screenhandler;

import dev.turtywurty.industria.blockentity.FluidTankBlockEntity;
import dev.turtywurty.industria.blockentity.util.fluid.SyncingFluidStorage;
import dev.turtywurty.industria.blockentity.util.inventory.PredicateSimpleInventory;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.ScreenHandlerTypeInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.screenhandler.base.TickableScreenHandler;
import dev.turtywurty.industria.screenhandler.slot.PredicateSlot;
import dev.turtywurty.industria.util.TransferUtils;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

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
                PredicateSimpleInventory.createEmptyFluidPredicate(() -> blockEntity.getFluidTank().variant)));
    }

    public FluidTankScreenHandler(int syncId, Inventory playerInventory, FluidTankBlockEntity blockEntity, SimpleContainer inventory) {
        super(ScreenHandlerTypeInit.FLUID_TANK, syncId);

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
        return stillValid(this.context, player, BlockInit.FLUID_TANK);
    }

    @Override
    public void tick(ServerPlayer player) {
        if (this.blockEntity == null || !this.blockEntity.hasLevel())
            return;

        boolean extractMode = this.blockEntity.isExtractMode();
        SyncingFluidStorage fluidTank = this.blockEntity.getFluidTank();
        if (fluidTank.amount > 0 && extractMode) {
            ItemStack stack = this.inventory.getItem(0);
            if(stack.isEmpty())
                return;

            Storage<FluidVariant> fluidStorage = FluidStorage.ITEM.find(stack, ContainerItemContext.withConstant(stack));
            if (fluidStorage == null || !fluidStorage.supportsInsertion())
                return;

            try (Transaction transaction = Transaction.openOuter()) {
                long inserted = fluidStorage.insert(fluidTank.variant, fluidTank.amount, transaction);
                if (inserted > 0) {
                    fluidTank.amount -= inserted;
                    transaction.commit();
                    this.blockEntity.update();
                }
            }
        } else if(fluidTank.amount <= 0 && !extractMode) {
            ItemStack stack = this.inventory.getItem(0);
            if(stack.isEmpty())
                return;

            Storage<FluidVariant> fluidStorage = FluidStorage.ITEM.find(stack, ContainerItemContext.withConstant(stack));
            if (fluidStorage == null || !fluidStorage.supportsExtraction())
                return;

            try (Transaction transaction = Transaction.openOuter()) {
                Optional<FluidVariant> variantToExtract = TransferUtils.findFirstVariant(fluidStorage, fluidTank.variant);
                if(variantToExtract.filter(variant -> !variant.isBlank()).isEmpty())
                    return;

                FluidVariant fluidVariant = variantToExtract.get();
                long extracted = fluidStorage.extract(fluidVariant, fluidTank.getCapacity() - fluidTank.amount, transaction);
                if (extracted > 0) {
                    fluidTank.variant = fluidVariant;
                    fluidTank.amount += extracted;
                    transaction.commit();
                    this.blockEntity.update();
                }
            }
        }
    }
}
