package dev.turtywurty.industria.screenhandler;

import dev.turtywurty.industria.blockentity.ElectrolyzerBlockEntity;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorage;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.ScreenHandlerTypeInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.screenhandler.base.IndustriaScreenHandler;
import dev.turtywurty.industria.screenhandler.slot.PredicateSlot;
import net.minecraft.util.Mth;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.block.Block;

import java.util.Collections;
import java.util.List;

public class ElectrolyzerScreenHandler extends IndustriaScreenHandler<ElectrolyzerBlockEntity, BlockPosPayload> {
    public ElectrolyzerScreenHandler(int syncId, Inventory playerInventory, BlockPosPayload payload) {
        super(ScreenHandlerTypeInit.ELECTROLYZER, 4, syncId, playerInventory, payload, ElectrolyzerBlockEntity.class);
    }

    public ElectrolyzerScreenHandler(int syncId, Inventory playerInventory, ElectrolyzerBlockEntity blockEntity, WrappedContainerStorage<?> wrappedContainerStorage, ContainerData propertyDelegate) {
        super(ScreenHandlerTypeInit.ELECTROLYZER, syncId, playerInventory, blockEntity, wrappedContainerStorage, propertyDelegate);
    }

    @Override
    protected int getPlayerInventoryX() {
        return 20;
    }

    @Override
    protected int getPlayerInventoryY() {
        return 139;
    }

    @Override
    protected int getInventorySize() {
        return 6;
    }

    @Override
    protected List<Block> getValidBlocks() {
        return Collections.singletonList(BlockInit.ELECTROLYZER);
    }

    @Override
    protected void addBlockEntitySlots(Inventory playerInventory) {
        SimpleContainer inputInventory = this.wrappedContainerStorage.getInventory(0);
        SimpleContainer electrolyteInventory = this.wrappedContainerStorage.getInventory(1);
        SimpleContainer anodeInventory = this.wrappedContainerStorage.getInventory(2);
        SimpleContainer cathodeInventory = this.wrappedContainerStorage.getInventory(3);

        addSlot(new Slot(inputInventory, 0, 22, 65));
        addSlot(new Slot(electrolyteInventory, 0, 92, 117));
        addSlot(new PredicateSlot(anodeInventory, 0, 58, 55));
        addSlot(new Slot(cathodeInventory, 0, 126, 55));

        addSlot(new PredicateSlot(this.wrappedContainerStorage.getInventory(4), 0, 150, 114));
        addSlot(new PredicateSlot(this.wrappedContainerStorage.getInventory(5), 0, 174, 114));
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
        if(maxProgress == 0 || progress == 0) {
            return 0.0f;
        }

        return Mth.clamp(progress / maxProgress, 0.0f, 1.0f);
    }

    public int getProgressScaled() {
        return Mth.ceil(getProgressPercent() * 24);
    }
}
