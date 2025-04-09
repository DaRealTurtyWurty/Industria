package dev.turtywurty.industria.screenhandler;

import dev.turtywurty.industria.blockentity.CrystallizerBlockEntity;
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
import net.minecraft.util.math.MathHelper;

public class CrystallizerScreenHandler extends IndustriaScreenHandler<CrystallizerBlockEntity, BlockPosPayload> {
    public CrystallizerScreenHandler(int syncId, PlayerInventory playerInv, BlockPosPayload payload) {
        super(ScreenHandlerTypeInit.CRYSTALLIZER, 4, syncId, playerInv, payload, CrystallizerBlockEntity.class);
    }

    public CrystallizerScreenHandler(int syncId, PlayerInventory playerInv, CrystallizerBlockEntity blockEntity, WrappedInventoryStorage<?> wrappedInventoryStorage, PropertyDelegate properties) {
        super(ScreenHandlerTypeInit.CRYSTALLIZER, syncId, playerInv, blockEntity, wrappedInventoryStorage, properties);
    }

    @Override
    protected void addBlockEntitySlots(PlayerInventory playerInventory) {
        WrappedInventoryStorage<?> wrappedStorage = this.wrappedInventoryStorage;
        addSlot(new PredicateSlot(wrappedStorage.getInventory(0), 0, 54, 12, $ -> !blockEntity.isRunning()));
        addSlot(new OutputSlot(wrappedStorage.getInventory(1), 0, 148, 27));
        addSlot(new OutputSlot(wrappedStorage.getInventory(2), 0, 148, 51));
    }

    @Override
    protected int getPlayerInventoryY() {
        return 92;
    }

    @Override
    protected int getInventorySize() {
        return 3;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(this.context, player, BlockInit.CRYSTALLIZER);
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
        return (int) (getProgressPercent() * 24);
    }

    public int getCatalystUses() {
        return this.propertyDelegate.get(2);
    }

    public int getMaxCatalystUses() {
        return this.propertyDelegate.get(3);
    }

    public float getCatalystUsesPercent() {
        float uses = getCatalystUses();
        float maxUses = getMaxCatalystUses();
        if(maxUses == 0 || uses == 0) {
            return 0.0f;
        }

        return MathHelper.clamp(uses / maxUses, 0.0f, 1.0f);
    }
}
