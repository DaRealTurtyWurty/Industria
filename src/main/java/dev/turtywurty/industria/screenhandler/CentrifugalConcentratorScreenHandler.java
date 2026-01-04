package dev.turtywurty.industria.screenhandler;

import dev.turtywurty.industria.blockentity.CentrifugalConcentratorBlockEntity;
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

public class CentrifugalConcentratorScreenHandler extends IndustriaScreenHandler<CentrifugalConcentratorBlockEntity, BlockPosPayload> {
    public CentrifugalConcentratorScreenHandler(int syncId, Inventory playerInventory, BlockPosPayload payload) {
        super(ScreenHandlerTypeInit.CENTRIFUGAL_CONCENTRATOR, 3, syncId, playerInventory, payload, CentrifugalConcentratorBlockEntity.class);
    }

    public CentrifugalConcentratorScreenHandler(int syncId, Inventory playerInventory, CentrifugalConcentratorBlockEntity blockEntity, WrappedContainerStorage<?> wrappedContainerStorage, ContainerData properties) {
        super(ScreenHandlerTypeInit.CENTRIFUGAL_CONCENTRATOR, syncId, playerInventory, blockEntity, wrappedContainerStorage, properties);
    }

    @Override
    protected int getInventorySize() {
        return 4;
    }

    @Override
    protected int getPlayerInventoryY() {
        return 92;
    }

    @Override
    protected void addBlockEntitySlots(Inventory playerInventory) {
        addSlot(new Slot(this.wrappedContainerStorage.getInventory(0), 0, 54, 35));
        addSlot(new OutputSlot(this.wrappedContainerStorage.getInventory(1), 0, 107, 35));
        addSlot(new PredicateSlot(this.wrappedContainerStorage.getInventory(2), 0, 26, 60));
        addSlot(new PredicateSlot(this.wrappedContainerStorage.getInventory(3), 0, 134, 60));
    }

    @Override
    public boolean stillValid(Player player) {
        // TODO: This doesn't seem to work beyond like a block or two
        return stillValid(this.context, player, BlockInit.CENTRIFUGAL_CONCENTRATOR);
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
        if (maxProgress == 0 || progress == 0)
            return 0.0F;

        return Mth.clamp(progress / maxProgress, 0.0F, 1.0F);
    }

    public int getProgressScaled() {
        return Mth.ceil(getProgressPercent() * 24);
    }
}
