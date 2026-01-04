package dev.turtywurty.industria.screenhandler;

import dev.turtywurty.industria.blockentity.WindTurbineBlockEntity;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.ScreenHandlerTypeInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import team.reborn.energy.api.EnergyStorage;

public class WindTurbineScreenHandler extends AbstractContainerMenu {
    private final WindTurbineBlockEntity blockEntity;
    private final ContainerLevelAccess context;

    public WindTurbineScreenHandler(int syncId, Inventory playerInv, BlockPosPayload payload) {
        this(syncId, playerInv, (WindTurbineBlockEntity) playerInv.player.level().getBlockEntity(payload.pos()));
    }

    public WindTurbineScreenHandler(int syncId, Inventory playerInv, WindTurbineBlockEntity blockEntity) {
        super(ScreenHandlerTypeInit.WIND_TURBINE, syncId);
        this.blockEntity = blockEntity;
        this.context = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());

        addStandardInventorySlots(playerInv, 8, 84);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.context, player, BlockInit.WIND_TURBINE);
    }

    public WindTurbineBlockEntity getBlockEntity() {
        return this.blockEntity;
    }

    public long getEnergy() {
        return this.blockEntity.getEnergyStorage().getAmount();
    }

    public long getMaxEnergy() {
        return this.blockEntity.getEnergyStorage().getCapacity();
    }

    public float getEnergyPercent() {
        EnergyStorage energyStorage = this.blockEntity.getEnergyStorage();
        long energy = energyStorage.getAmount();
        long maxEnergy = energyStorage.getCapacity();
        if (maxEnergy == 0 || energy == 0)
            return 0.0F;

        return Mth.clamp((float) energy / (float) maxEnergy, 0.0F, 1.0F);
    }

    public float getWindSpeed() {
        return this.blockEntity.getWindSpeed();
    }

    public int getEnergyPerTick() {
        return this.blockEntity.getEnergyOutput();
    }

    public float getEnergyPerTickPercent() {
        int output = getEnergyPerTick();
        if (output == 0)
            return 0.0F;

        return Mth.clamp((float) output / 500.0F, 0.0F, 1.0F);
    }
}
