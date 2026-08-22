package dev.turtywurty.industria.menu;

import dev.turtywurty.industria.blockentity.SolarPanelBlockEntity;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.init.ModMenuTypes;
import dev.turtywurty.industria.network.BlockPosPayload;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;

public class SolarPanelScreenHandler extends AbstractContainerMenu {
    private final SolarPanelBlockEntity blockEntity;
    private final ContainerLevelAccess context;

    public SolarPanelScreenHandler(int syncId, Inventory playerInv, BlockPosPayload payload) {
        this(syncId, playerInv, (SolarPanelBlockEntity) playerInv.player.level().getBlockEntity(payload.pos()));
    }

    public SolarPanelScreenHandler(int syncId, Inventory playerInv, SolarPanelBlockEntity blockEntity) {
        super(ModMenuTypes.SOLAR_PANEL.get(), syncId);
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
        return stillValid(this.context, player, ModBlocks.SOLAR_PANEL.get())
                || stillValid(this.context, player, ModBlocks.ADVANCED_SOLAR_PANEL.get());
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
        var energyStorage = this.blockEntity.getEnergyStorage();
        long energy = energyStorage.getAmount();
        long maxEnergy = energyStorage.getCapacity();
        if (maxEnergy == 0 || energy == 0)
            return 0.0F;

        return Mth.clamp((float) energy / (float) maxEnergy, 0.0F, 1.0F);
    }

    public int getEnergyPerTick() {
        return this.blockEntity.getEnergyOutput();
    }

    public float getEnergyPerTickPercent() {
        int output = getEnergyPerTick();
        if (output == 0)
            return 0.0F;

        return Mth.clamp((float) output / this.blockEntity.getMaximumEnergyOutput(), 0.0F, 1.0F);
    }
}
