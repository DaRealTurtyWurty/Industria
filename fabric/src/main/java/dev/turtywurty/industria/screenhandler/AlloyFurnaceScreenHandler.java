package dev.turtywurty.industria.screenhandler;

import dev.turtywurty.industria.blockentity.AlloyFurnaceBlockEntity;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorage;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.ScreenHandlerTypeInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.screenhandler.base.IndustriaScreenHandler;
import dev.turtywurty.industria.screenhandler.slot.OutputSlot;
import dev.turtywurty.industria.screenhandler.slot.PredicateSlot;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;

public class AlloyFurnaceScreenHandler extends IndustriaScreenHandler<AlloyFurnaceBlockEntity, BlockPosPayload> {
    public AlloyFurnaceScreenHandler(int syncId, Inventory playerInventory, BlockPosPayload payload) {
        super(ScreenHandlerTypeInit.ALLOY_FURNACE, 4, syncId, playerInventory, payload, AlloyFurnaceBlockEntity.class);
    }

    public AlloyFurnaceScreenHandler(int syncId, Inventory playerInventory, AlloyFurnaceBlockEntity blockEntity,
                                     WrappedContainerStorage<?> wrappedContainerStorage, ContainerData propertyDelegate) {
        super(ScreenHandlerTypeInit.ALLOY_FURNACE, syncId, playerInventory, blockEntity, wrappedContainerStorage, propertyDelegate);
    }

    @Override
    protected void addBlockEntitySlots(Inventory playerInventory) {
        WrappedContainerStorage<?> inventory = this.wrappedContainerStorage;
        addSlot(new Slot(inventory.getInventory(AlloyFurnaceBlockEntity.INPUT_SLOT_0), 0, 42, 17));
        addSlot(new Slot(inventory.getInventory(AlloyFurnaceBlockEntity.INPUT_SLOT_1), 0, 70, 17));
        addSlot(new PredicateSlot(inventory.getInventory(AlloyFurnaceBlockEntity.FUEL_SLOT), 0, 56, 53));
        addSlot(new OutputSlot(inventory.getInventory(AlloyFurnaceBlockEntity.OUTPUT_SLOT), 0, 116, 35));
    }

    @Override
    protected int getInventorySize() {
        return 4;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.context, player, BlockInit.ALLOY_FURNACE);
    }

    public int getProgress() {
        return this.propertyDelegate.get(0);
    }

    public int getMaxProgress() {
        return this.propertyDelegate.get(1);
    }

    public int getBurnTime() {
        return this.propertyDelegate.get(2);
    }

    public int getMaxBurnTime() {
        return this.propertyDelegate.get(3);
    }

    public float getProgressPercent() {
        float progress = getProgress();
        float maxProgress = getMaxProgress();
        if (maxProgress == 0 || progress == 0)
            return 0.0F;

        return Mth.clamp(progress / maxProgress, 0.0F, 1.0F);
    }

    public float getBurnTimePercent() {
        float progress = getBurnTime();
        float maxProgress = getMaxBurnTime();
        if (maxProgress == 0 || progress == 0)
            return 0.0F;

        return Mth.clamp(progress / maxProgress, 0.0F, 1.0F);
    }
}
