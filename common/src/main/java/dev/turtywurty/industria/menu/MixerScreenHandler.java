package dev.turtywurty.industria.menu;

import dev.turtywurty.industria.blockentity.MixerBlockEntity;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorage;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.init.ModMenuTypes;
import dev.turtywurty.industria.menu.base.IndustriaScreenHandler;
import dev.turtywurty.industria.menu.slot.OutputSlot;
import dev.turtywurty.industria.menu.slot.PredicateSlot;
import dev.turtywurty.industria.network.BlockPosPayload;
import net.minecraft.util.Mth;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;

public class MixerScreenHandler extends IndustriaScreenHandler<MixerBlockEntity, BlockPosPayload> {
    public MixerScreenHandler(int syncId, Inventory playerInventory, BlockPosPayload payload) {
        super(ModMenuTypes.MIXER.get(), 2, syncId, playerInventory, payload, MixerBlockEntity.class);
    }

    public MixerScreenHandler(int syncId, Inventory playerInventory, MixerBlockEntity blockEntity, WrappedContainerStorage<?> wrappedContainerStorage, ContainerData propertyDelegate) {
        super(ModMenuTypes.MIXER.get(), syncId, playerInventory, blockEntity, wrappedContainerStorage, propertyDelegate);
    }

    @Override
    protected void addBlockEntitySlots(Inventory playerInventory) {
        SimpleContainer inputInventory = this.wrappedContainerStorage.getInventory(0);
        for (int row = 0; row < 2; row++) {
            for (int column = 0; column < 3; column++) {
                addSlot(new Slot(inputInventory, column + (row * 3), 58 + column * 18, 26 + row * 18));
            }
        }

        addSlot(new OutputSlot(this.wrappedContainerStorage.getInventory(1), 0, 143, 35));

        addSlot(new PredicateSlot(this.wrappedContainerStorage.getInventory(2), 0, 36, 83));
        addSlot(new PredicateSlot(this.wrappedContainerStorage.getInventory(3), 0, 170, 83));
    }

    @Override
    protected int getInventorySize() {
        return 9;
    }

    @Override
    protected int getPlayerInventoryX() {
        return 20;
    }

    @Override
    protected int getPlayerInventoryY() {
        return 112;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.context, player, ModBlocks.MIXER.get());
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
