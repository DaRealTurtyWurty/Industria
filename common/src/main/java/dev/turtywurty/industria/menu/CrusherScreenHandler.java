package dev.turtywurty.industria.menu;

import dev.turtywurty.industria.blockentity.CrusherBlockEntity;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorage;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.init.ModMenuTypes;
import dev.turtywurty.industria.menu.base.IndustriaScreenHandler;
import dev.turtywurty.industria.menu.slot.OutputSlot;
import dev.turtywurty.industria.network.BlockPosPayload;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;

public class CrusherScreenHandler extends IndustriaScreenHandler<CrusherBlockEntity, BlockPosPayload> {
    public CrusherScreenHandler(int syncId, Inventory playerInv, BlockPosPayload payload) {
        super(ModMenuTypes.CRUSHER.get(), 2, syncId, playerInv, payload, CrusherBlockEntity.class);
    }

    public CrusherScreenHandler(int syncId, Inventory playerInv, CrusherBlockEntity blockEntity, WrappedContainerStorage<?> wrappedContainerStorage, ContainerData propertyDelegate) {
        super(ModMenuTypes.CRUSHER.get(), syncId, playerInv, blockEntity, wrappedContainerStorage, propertyDelegate);
    }

    @Override
    protected int getInventorySize() {
        return 3;
    }

    @Override
    protected void addBlockEntitySlots(Inventory playerInventory) {
        WrappedContainerStorage<?> wrappedStorage = this.wrappedContainerStorage;
        addSlot(new Slot(wrappedStorage.getInventory(CrusherBlockEntity.INPUT_SLOT), 0, 44, 35));
        addSlot(new OutputSlot(wrappedStorage.getInventory(CrusherBlockEntity.OUTPUT_SLOT), 0, 98, 35));
        addSlot(new OutputSlot(wrappedStorage.getInventory(CrusherBlockEntity.OUTPUT_SLOT), 1, 116, 35));
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.context, player, ModBlocks.CRUSHER.get());
    }

    public int getProgress() {
        return this.propertyDelegate.get(0);
    }

    public int getMaxProgress() {
        return this.propertyDelegate.get(1);
    }

    public float getProgressPercent() {
        float progress = getProgress();
        float maxProgress = getMaxProgress();
        if (maxProgress == 0 || progress == 0) {
            return 0.0f;
        }

        return Mth.clamp(progress / maxProgress, 0.0f, 1.0f);
    }

    public long getEnergy() {
        return this.blockEntity.getEnergy().getAmount();
    }

    public long getMaxEnergy() {
        return this.blockEntity.getEnergy().getCapacity();
    }

    public float getEnergyPercent() {
        long energy = getEnergy();
        long maxEnergy = getMaxEnergy();
        if (maxEnergy == 0 || energy == 0) {
            return 0.0f;
        }

        return Mth.clamp(energy / (float) maxEnergy, 0.0f, 1.0f);
    }
}
