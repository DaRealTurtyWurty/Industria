package dev.turtywurty.industria.screenhandler;

import dev.turtywurty.industria.blockentity.ElectricFurnaceBlockEntity;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorage;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.ScreenHandlerTypeInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.screenhandler.base.IndustriaScreenHandler;
import dev.turtywurty.industria.screenhandler.slot.OutputSlot;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import team.reborn.energy.api.EnergyStorage;

// TODO: Use property delegate?
public class ElectricFurnaceScreenHandler extends IndustriaScreenHandler<ElectricFurnaceBlockEntity, BlockPosPayload> {
    public ElectricFurnaceScreenHandler(int syncId, Inventory playerInventory, BlockPosPayload payload) {
        super(ScreenHandlerTypeInit.ELECTRIC_FURNACE, syncId, playerInventory, payload, ElectricFurnaceBlockEntity.class);
    }

    public ElectricFurnaceScreenHandler(int syncId, Inventory playerInventory, ElectricFurnaceBlockEntity blockEntity, WrappedContainerStorage<?> wrappedContainerStorage) {
        super(ScreenHandlerTypeInit.ELECTRIC_FURNACE, syncId, playerInventory, blockEntity, wrappedContainerStorage);
    }

    @Override
    protected void addBlockEntitySlots(Inventory playerInventory) {
        addSlot(new Slot(wrappedContainerStorage.getInventory(0), 0, 49, 33));
        addSlot(new ExperienceOutputSlot(playerInventory.player, blockEntity, wrappedContainerStorage.getInventory(1), 0, 108, 33));
    }

    @Override
    protected int getInventorySize() {
        return 2;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.context, player, BlockInit.ELECTRIC_FURNACE);
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

        return Mth.clamp((float) progress / maxProgress, 0, 1);
    }

    // TODO: Abstract out and make its own class
    public static class ExperienceOutputSlot extends OutputSlot {
        private final Player player;
        private final ElectricFurnaceBlockEntity blockEntity;
        private int amount;

        public ExperienceOutputSlot(Player player, ElectricFurnaceBlockEntity blockEntity, SimpleContainer inventory, int index, int x, int y) {
            super(inventory, index, x, y);
            this.player = player;
            this.blockEntity = blockEntity;
        }

        @Override
        public ItemStack remove(int amount) {
            if(hasItem()) {
                this.amount += Math.min(amount, getItem().getCount());
            }

            return super.remove(amount);
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            checkTakeAchievements(stack);
            super.onTake(player, stack);
        }

        @Override
        protected void onQuickCraft(ItemStack stack, int amount) {
            this.amount += amount;
            super.onQuickCraft(stack, amount);
        }

        @Override
        protected void checkTakeAchievements(ItemStack stack) {
            stack.onCraftedBy(this.player, this.amount);
            if(this.player instanceof ServerPlayer serverPlayerEntity) {
                this.blockEntity.dropExperienceForRecipesUsed(serverPlayerEntity);
            }

            this.amount = 0;
        }
    }
}
