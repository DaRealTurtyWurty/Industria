package dev.turtywurty.industria.screenhandler;

import dev.turtywurty.industria.blockentity.BatteryBlockEntity;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorage;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.ScreenHandlerTypeInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.screenhandler.base.IndustriaScreenHandler;
import dev.turtywurty.industria.screenhandler.slot.PredicateSlot;
import net.minecraft.util.Mth;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import team.reborn.energy.api.EnergyStorage;

import java.util.Arrays;

public class BatteryScreenHandler extends IndustriaScreenHandler<BatteryBlockEntity, BlockPosPayload> {
    public BatteryScreenHandler(int syncId, Inventory playerInventory, BlockPosPayload payload) {
        super(ScreenHandlerTypeInit.BATTERY, syncId, playerInventory, payload, BatteryBlockEntity.class);
    }

    public BatteryScreenHandler(int syncId, Inventory playerInventory, BatteryBlockEntity blockEntity,
                                WrappedContainerStorage<?> wrappedContainerStorage) {
        super(ScreenHandlerTypeInit.BATTERY, syncId, playerInventory, blockEntity, wrappedContainerStorage);
    }

    @Override
    protected int getInventorySize() {
        return 1;
    }

    @Override
    protected void addBlockEntitySlots(Inventory playerInventory) {
        SimpleContainer inventory = this.wrappedContainerStorage.getInventory(0);
        addSlot(new PredicateSlot(inventory, 0, 80, 35, (stack) -> this.blockEntity.isValid(stack, 0)));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return canUse(player, BlockInit.BASIC_BATTERY, BlockInit.ADVANCED_BATTERY,
                BlockInit.ELITE_BATTERY, BlockInit.ULTIMATE_BATTERY, BlockInit.CREATIVE_BATTERY);
    }

    private boolean canUse(Player player, Block... blocks) {
        return Arrays.stream(blocks).anyMatch(block -> stillValid(this.context, player, block));
    }

    public float getEnergyPercent() {
        EnergyStorage storage = this.blockEntity.getEnergy();
        long energy = storage.getAmount();
        long capacity = storage.getCapacity();
        if(energy == 0 || capacity == 0)
            return 0.0f;

        return Mth.clamp((float) energy / (float) capacity, 0.0f, 1.0f);
    }

    public long getEnergy() {
        return this.blockEntity.getEnergy().getAmount();
    }

    public long getMaxEnergy() {
        return this.blockEntity.getEnergy().getCapacity();
    }

    public BatteryBlockEntity.ChargeMode getChargeMode() {
        return this.blockEntity.getChargeMode();
    }
}
