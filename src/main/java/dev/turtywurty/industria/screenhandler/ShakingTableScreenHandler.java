package dev.turtywurty.industria.screenhandler;

import dev.turtywurty.industria.blockentity.ShakingTableBlockEntity;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedInventoryStorage;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.ScreenHandlerTypeInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.screenhandler.base.IndustriaScreenHandler;
import dev.turtywurty.industria.screenhandler.slot.OutputSlot;
import dev.turtywurty.industria.screenhandler.slot.PredicateSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.MathHelper;

public class ShakingTableScreenHandler extends IndustriaScreenHandler<ShakingTableBlockEntity, BlockPosPayload> {
    public ShakingTableScreenHandler(int syncId, PlayerInventory playerInventory, BlockPosPayload payload) {
        super(ScreenHandlerTypeInit.SHAKING_TABLE, 2, syncId, playerInventory, payload, ShakingTableBlockEntity.class);
    }

    public ShakingTableScreenHandler(int syncId, PlayerInventory playerInventory, ShakingTableBlockEntity blockEntity, WrappedInventoryStorage<?> wrappedInventoryStorage, PropertyDelegate properties) {
        super(ScreenHandlerTypeInit.SHAKING_TABLE, syncId, playerInventory, blockEntity, wrappedInventoryStorage, properties);
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
    protected void addBlockEntitySlots(PlayerInventory playerInventory) {
        addSlot(new Slot(this.wrappedInventoryStorage.getInventory(0), 0, 54, 35));
        addSlot(new OutputSlot(this.wrappedInventoryStorage.getInventory(1), 0, 107, 35));
        addSlot(new PredicateSlot(this.wrappedInventoryStorage.getInventory(2), 0, 26, 60));
        addSlot(new PredicateSlot(this.wrappedInventoryStorage.getInventory(3), 0, 134, 60));
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        // TODO: This doesn't seem to work beyond like a block or two
        return canUse(this.context, player, BlockInit.SHAKING_TABLE);
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

        return MathHelper.clamp(progress / maxProgress, 0.0F, 1.0F);
    }

    public int getProgressScaled() {
        return MathHelper.ceil(getProgressPercent() * 24);
    }
}
