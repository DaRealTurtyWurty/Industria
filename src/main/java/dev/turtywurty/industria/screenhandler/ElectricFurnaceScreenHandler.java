package dev.turtywurty.industria.screenhandler;

import dev.turtywurty.industria.blockentity.ElectricFurnaceBlockEntity;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedInventoryStorage;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.ScreenHandlerTypeInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.screenhandler.slot.OutputSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;
import team.reborn.energy.api.EnergyStorage;

public class ElectricFurnaceScreenHandler extends ScreenHandler {
    private final ElectricFurnaceBlockEntity blockEntity;
    private final ScreenHandlerContext context;

    public ElectricFurnaceScreenHandler(int syncId, PlayerInventory playerInventory, BlockPosPayload payload) {
        this(syncId, playerInventory, (ElectricFurnaceBlockEntity) playerInventory.player.getWorld().getBlockEntity(payload.pos()));
    }

    public ElectricFurnaceScreenHandler(int syncId, PlayerInventory playerInventory, ElectricFurnaceBlockEntity blockEntity) {
        super(ScreenHandlerTypeInit.ELECTRIC_FURNACE, syncId);

        this.blockEntity = blockEntity;
        this.context = ScreenHandlerContext.create(blockEntity.getWorld(), blockEntity.getPos());

        WrappedInventoryStorage<SimpleInventory> wrappedInventoryStorage = blockEntity.getWrappedInventoryStorage();
        wrappedInventoryStorage.checkSize(2);
        wrappedInventoryStorage.onOpen(playerInventory.player);

        addPlayerSlots(playerInventory, 8, 84);
        addSlot(new Slot(wrappedInventoryStorage.getInventory(0), 0, 49, 33));
        addSlot(new ExperienceOutputSlot(playerInventory.player, blockEntity, wrappedInventoryStorage.getInventory(1), 0, 108, 33));
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        blockEntity.getWrappedInventoryStorage().onClose(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(this.context, player, BlockInit.ELECTRIC_FURNACE);
    }

    public ElectricFurnaceBlockEntity getBlockEntity() {
        return this.blockEntity;
    }

    public int getProgress() {
        return this.blockEntity.getProgress();
    }

    public int getMaxProgress() {
        return this.blockEntity.getMaxProgress();
    }

    public EnergyStorage getEnergyStorage() {
        return this.blockEntity.getEnergyProvider(null);
    }

    public float getProgressPercentage() {
        int progress = getProgress();
        int maxProgress = getMaxProgress();

        if(progress == 0 || maxProgress == 0)
            return 0;

        return MathHelper.clamp((float) progress / maxProgress, 0, 1);
    }

    public static class ExperienceOutputSlot extends OutputSlot {
        private final PlayerEntity player;
        private final ElectricFurnaceBlockEntity blockEntity;
        private int amount;

        public ExperienceOutputSlot(PlayerEntity player, ElectricFurnaceBlockEntity blockEntity, SimpleInventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
            this.player = player;
            this.blockEntity = blockEntity;
        }

        @Override
        public ItemStack takeStack(int amount) {
            if(hasStack()) {
                this.amount += Math.min(amount, getStack().getCount());
            }

            return super.takeStack(amount);
        }

        @Override
        public void onTakeItem(PlayerEntity player, ItemStack stack) {
            onCrafted(stack);
            super.onTakeItem(player, stack);
        }

        @Override
        protected void onCrafted(ItemStack stack, int amount) {
            this.amount += amount;
            super.onCrafted(stack, amount);
        }

        @Override
        protected void onCrafted(ItemStack stack) {
            stack.onCraftByPlayer(this.player.getWorld(), this.player, this.amount);
            if(this.player instanceof ServerPlayerEntity serverPlayerEntity) {
                this.blockEntity.dropExperienceForRecipesUsed(serverPlayerEntity);
            }

            this.amount = 0;
        }
    }
}
