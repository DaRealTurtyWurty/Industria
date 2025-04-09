package dev.turtywurty.industria.screenhandler;

import dev.turtywurty.industria.blockentity.ElectrolyzerBlockEntity;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedInventoryStorage;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.ScreenHandlerTypeInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.screenhandler.base.IndustriaScreenHandler;
import dev.turtywurty.industria.screenhandler.slot.PredicateSlot;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.MathHelper;

import java.util.Collections;
import java.util.List;

public class ElectrolyzerScreenHandler extends IndustriaScreenHandler<ElectrolyzerBlockEntity, BlockPosPayload> {
    public ElectrolyzerScreenHandler(int syncId, PlayerInventory playerInventory, BlockPosPayload payload) {
        super(ScreenHandlerTypeInit.ELECTROLYZER, 4, syncId, playerInventory, payload, ElectrolyzerBlockEntity.class);
    }

    public ElectrolyzerScreenHandler(int syncId, PlayerInventory playerInventory, ElectrolyzerBlockEntity blockEntity, WrappedInventoryStorage<?> wrappedInventoryStorage, PropertyDelegate propertyDelegate) {
        super(ScreenHandlerTypeInit.ELECTROLYZER, syncId, playerInventory, blockEntity, wrappedInventoryStorage, propertyDelegate);
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
        return 4;
    }

    @Override
    protected List<Block> getValidBlocks() {
        return Collections.singletonList(BlockInit.ELECTROLYZER);
    }

    @Override
    protected void addBlockEntitySlots(PlayerInventory playerInventory) {
        SimpleInventory inputInventory = this.wrappedInventoryStorage.getInventory(0);
        SimpleInventory electrolyteInventory = this.wrappedInventoryStorage.getInventory(1);
        SimpleInventory anodeInventory = this.wrappedInventoryStorage.getInventory(2);
        SimpleInventory cathodeInventory = this.wrappedInventoryStorage.getInventory(3);

        addSlot(new Slot(inputInventory, 0, 22, 65));
        addSlot(new Slot(electrolyteInventory, 0, 92, 117));
        addSlot(new PredicateSlot(anodeInventory, 0, 58, 55));
        addSlot(new Slot(cathodeInventory, 0, 126, 55));

        // TODO: Add bucket output slots
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

        return MathHelper.clamp(progress / maxProgress, 0.0f, 1.0f);
    }

    public int getProgressScaled() {
        return MathHelper.ceil(getProgressPercent() * 24);
    }
}
