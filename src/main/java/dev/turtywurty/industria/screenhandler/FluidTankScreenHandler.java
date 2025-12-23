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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ItemScatterer;

import java.util.Optional;

public class FluidTankScreenHandler extends ScreenHandler implements TickableScreenHandler {
    private final FluidTankBlockEntity blockEntity;
    private final ScreenHandlerContext context;
    private final SimpleInventory inventory;

    public FluidTankScreenHandler(int syncId, PlayerInventory playerInventory, BlockPosPayload payload) {
        this(syncId, playerInventory, (FluidTankBlockEntity) playerInventory.player.getEntityWorld().getBlockEntity(payload.pos()), new SimpleInventory(1));
    }

    public FluidTankScreenHandler(int syncId, PlayerInventory playerInventory, FluidTankBlockEntity blockEntity) {
        this(syncId, playerInventory, blockEntity, new PredicateSimpleInventory(
                blockEntity,
                1,
                PredicateSimpleInventory.createEmptyFluidPredicate(() -> blockEntity.getFluidTank().variant)));
    }

    public FluidTankScreenHandler(int syncId, PlayerInventory playerInventory, FluidTankBlockEntity blockEntity, SimpleInventory inventory) {
        super(ScreenHandlerTypeInit.FLUID_TANK, syncId);

        this.blockEntity = blockEntity;
        this.context = ScreenHandlerContext.create(blockEntity.getWorld(), blockEntity.getPos());
        this.inventory = inventory;

        addSlot(new PredicateSlot(this.inventory, 0, 81, 64));
        addPlayerSlots(playerInventory, 8, 92);
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        ItemScatterer.spawn(this.blockEntity.getWorld(), this.blockEntity.getPos(), this.inventory);
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

        if (slotIndex < 1) {
            if (!insertItem(stackInSlot, this.slots.size() - 9, this.slots.size(), true)) {
                if (!insertItem(stackInSlot, this.slots.size() - 36, this.slots.size() - 9, false)) {
                    return ItemStack.EMPTY;
                }
            }
        } else {
            if (!insertItem(stackInSlot, 0, 1, false)) {
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

    public FluidTankBlockEntity getBlockEntity() {
        return this.blockEntity;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(this.context, player, BlockInit.FLUID_TANK);
    }

    @Override
    public void tick(ServerPlayerEntity player) {
        if (this.blockEntity == null || !this.blockEntity.hasWorld())
            return;

        boolean extractMode = this.blockEntity.isExtractMode();
        SyncingFluidStorage fluidTank = this.blockEntity.getFluidTank();
        if (fluidTank.amount > 0 && extractMode) {
            ItemStack stack = this.inventory.getStack(0);
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
            ItemStack stack = this.inventory.getStack(0);
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
