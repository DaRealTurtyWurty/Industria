package dev.turtywurty.industria.screenhandler;

import dev.turtywurty.industria.blockentity.SolarPanelBlockEntity;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.ScreenHandlerTypeInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.util.math.MathHelper;
import team.reborn.energy.api.base.SimpleEnergyStorage;

public class SolarPanelScreenHandler extends ScreenHandler {
    private final SolarPanelBlockEntity blockEntity;
    private final ScreenHandlerContext context;

    public SolarPanelScreenHandler(int syncId, PlayerInventory playerInv, BlockPosPayload payload) {
        this(syncId, playerInv, (SolarPanelBlockEntity) playerInv.player.getWorld().getBlockEntity(payload.pos()));
    }

    public SolarPanelScreenHandler(int syncId, PlayerInventory playerInv, SolarPanelBlockEntity blockEntity) {
        super(ScreenHandlerTypeInit.SOLAR_PANEL, syncId);
        this.blockEntity = blockEntity;
        this.context = ScreenHandlerContext.create(blockEntity.getWorld(), blockEntity.getPos());

        addPlayerSlots(playerInv, 8, 84);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(this.context, player, BlockInit.SOLAR_PANEL);
    }

    public SolarPanelBlockEntity getBlockEntity() {
        return this.blockEntity;
    }

    public long getEnergy() {
        return this.blockEntity.getEnergyStorage().getAmount();
    }

    public long getMaxEnergy() {
        return this.blockEntity.getEnergyStorage().getCapacity();
    }

    public float getEnergyPercent() {
        SimpleEnergyStorage energyStorage = this.blockEntity.getEnergyStorage();
        long energy = energyStorage.getAmount();
        long maxEnergy = energyStorage.getCapacity();
        if (maxEnergy == 0 || energy == 0)
            return 0.0F;

        return MathHelper.clamp((float) energy / (float) maxEnergy, 0.0F, 1.0F);
    }

    public int getEnergyPerTick() {
        return this.blockEntity.getEnergyOutput();
    }

    public float getEnergyPerTickPercent() {
        int output = getEnergyPerTick();
        if (output == 0)
            return 0.0F;

        return MathHelper.clamp(output / 35.0F, 0.0F, 1.0F);
    }
}
