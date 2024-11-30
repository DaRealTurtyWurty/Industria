package dev.turtywurty.industria.screenhandler;

import dev.turtywurty.industria.blockentity.BatteryBlockEntity;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedInventoryStorage;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.ScreenHandlerTypeInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.screenhandler.slot.PredicateSlot;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.util.math.MathHelper;
import team.reborn.energy.api.base.SimpleEnergyStorage;


import java.util.List;
import java.util.Arrays;

public class BatteryScreenHandler extends ScreenHandler {
    private final BatteryBlockEntity blockEntity;
    private final ScreenHandlerContext context;

    public BatteryScreenHandler(int syncId, PlayerInventory playerInventory, BlockPosPayload payload) {
        this(syncId, playerInventory, (BatteryBlockEntity) playerInventory.player.getWorld().getBlockEntity(payload.pos()));
    }

    public BatteryScreenHandler(int syncId, PlayerInventory playerInventory, BatteryBlockEntity blockEntity) {
        super(ScreenHandlerTypeInit.BATTERY, syncId);
        this.blockEntity = blockEntity;
        this.context = ScreenHandlerContext.create(blockEntity.getWorld(), blockEntity.getPos());

        WrappedInventoryStorage<SimpleInventory> inventory = blockEntity.getWrappedInventory();
        inventory.checkSize(1);
        inventory.onOpen(playerInventory.player);

        addPlayerSlots(playerInventory, 8, 84);
        addBlockEntityInventory();
    }

    private void addBlockEntityInventory() {
        SimpleInventory inventory = this.blockEntity.getWrappedInventory().getRecipeInventory();
        addSlot(new PredicateSlot(inventory, 0, 80, 35, (stack) -> this.blockEntity.isValid(stack, 0)));
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.blockEntity.getWrappedInventory().onClose(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(player, BlockInit.BATTERIES.toArray(new Block[0]));
    }

    private boolean canUse(PlayerEntity player, Block... blocks) {
        return Arrays.stream(blocks).anyMatch(block -> canUse(this.context, player, block));
    }

    public float getEnergyPercent() {
        SimpleEnergyStorage storage = this.blockEntity.getEnergy();
        long energy = storage.getAmount();
        long capacity = storage.getCapacity();
        if(energy == 0 || capacity == 0)
            return 0.0f;

        return MathHelper.clamp((float) energy / (float) capacity, 0.0f, 1.0f);
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

    public BatteryBlockEntity getBlockEntity() {
        return this.blockEntity;
    }
}
