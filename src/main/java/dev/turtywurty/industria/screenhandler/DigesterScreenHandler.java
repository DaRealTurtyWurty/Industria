package dev.turtywurty.industria.screenhandler;

import dev.turtywurty.industria.blockentity.DigesterBlockEntity;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedInventoryStorage;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.ScreenHandlerTypeInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.screenhandler.base.IndustriaScreenHandler;
import dev.turtywurty.industria.screenhandler.slot.PredicateSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.util.math.MathHelper;

public class DigesterScreenHandler extends IndustriaScreenHandler<DigesterBlockEntity, BlockPosPayload> {
    public DigesterScreenHandler(int syncId, PlayerInventory playerInventory, BlockPosPayload payload) {
        super(ScreenHandlerTypeInit.DIGESTER, 2, syncId, playerInventory, payload, DigesterBlockEntity.class);
    }

    public DigesterScreenHandler(int syncId, PlayerInventory playerInventory, DigesterBlockEntity blockEntity, WrappedInventoryStorage<?> wrappedInventoryStorage, PropertyDelegate properties) {
        super(ScreenHandlerTypeInit.DIGESTER, syncId, playerInventory, blockEntity, wrappedInventoryStorage, properties);
    }

    @Override
    protected void addBlockEntitySlots(PlayerInventory playerInventory) {
        addSlot(new PredicateSlot(this.wrappedInventoryStorage.getInventory(0), 0, 26, 60));
        addSlot(new PredicateSlot(this.wrappedInventoryStorage.getInventory(1), 0, 134, 60));
    }

    @Override
    protected int getInventorySize() {
        return 2;
    }

    @Override
    protected int getPlayerInventoryY() {
        return 92;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(this.context, player, BlockInit.DIGESTER);
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