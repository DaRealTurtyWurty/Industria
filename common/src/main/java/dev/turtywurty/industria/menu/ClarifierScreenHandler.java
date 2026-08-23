package dev.turtywurty.industria.menu;

import dev.turtywurty.industria.blockentity.ClarifierBlockEntity;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorage;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.init.ModMenuTypes;
import dev.turtywurty.industria.menu.base.IndustriaScreenHandler;
import dev.turtywurty.industria.menu.slot.OutputSlot;
import dev.turtywurty.industria.network.BlockPosPayload;
import net.minecraft.util.Mth;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;

public class ClarifierScreenHandler extends IndustriaScreenHandler<ClarifierBlockEntity, BlockPosPayload> {
    public ClarifierScreenHandler(int syncId, Inventory playerInv, BlockPosPayload payload) {
        super(ModMenuTypes.CLARIFIER.get(), 2, syncId, playerInv, payload, ClarifierBlockEntity.class);
    }

    public ClarifierScreenHandler(int syncId, Inventory playerInv, ClarifierBlockEntity blockEntity, WrappedContainerStorage<?> wrappedContainerStorage, ContainerData properties) {
        super(ModMenuTypes.CLARIFIER.get(), syncId, playerInv, blockEntity, wrappedContainerStorage, properties);
    }

    @Override
    protected int getInventorySize() {
        return 1;
    }

    @Override
    protected int getPlayerInventoryY() {
        return 92;
    }

    @Override
    protected void addBlockEntitySlots(Inventory playerInventory) {
        SimpleContainer inventory = this.wrappedContainerStorage.getInventory(0);
        addSlot(new OutputSlot(inventory, 0, 134, 60));
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.context, player, ModBlocks.CLARIFIER.get());
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
