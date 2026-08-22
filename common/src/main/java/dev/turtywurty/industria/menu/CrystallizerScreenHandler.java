package dev.turtywurty.industria.menu;

import dev.turtywurty.industria.blockentity.CrystallizerBlockEntity;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorage;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.init.ModMenuTypes;
import dev.turtywurty.industria.menu.base.IndustriaScreenHandler;
import dev.turtywurty.industria.menu.slot.OutputSlot;
import dev.turtywurty.industria.menu.slot.PredicateSlot;
import dev.turtywurty.industria.network.BlockPosPayload;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;

public class CrystallizerScreenHandler extends IndustriaScreenHandler<CrystallizerBlockEntity, BlockPosPayload> {
    public CrystallizerScreenHandler(int syncId, Inventory playerInv, BlockPosPayload payload) {
        super(ModMenuTypes.CRYSTALLIZER.get(), 4, syncId, playerInv, payload, CrystallizerBlockEntity.class);
    }

    public CrystallizerScreenHandler(int syncId, Inventory playerInv, CrystallizerBlockEntity blockEntity, WrappedContainerStorage<?> wrappedContainerStorage, ContainerData properties) {
        super(ModMenuTypes.CRYSTALLIZER.get(), syncId, playerInv, blockEntity, wrappedContainerStorage, properties);
    }

    @Override
    protected void addBlockEntitySlots(Inventory playerInventory) {
        WrappedContainerStorage<?> wrappedStorage = this.wrappedContainerStorage;
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
    public boolean stillValid(Player player) {
        return stillValid(this.context, player, ModBlocks.CRYSTALLIZER.get());
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
        if (maxUses == 0 || uses == 0) {
            return 0.0f;
        }

        return Mth.clamp(uses / maxUses, 0.0f, 1.0f);
    }
}
