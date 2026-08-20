package dev.turtywurty.industria.screenhandler;

import dev.turtywurty.industria.blockentity.ArcFurnaceBlockEntity;
import dev.turtywurty.industria.blockentity.util.inventory.SyncingSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorage;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.ScreenHandlerTypeInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.screenhandler.base.IndustriaScreenHandler;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class ArcFurnaceScreenHandler extends IndustriaScreenHandler<ArcFurnaceBlockEntity, BlockPosPayload> {
    public ArcFurnaceScreenHandler(int syncId, Inventory playerInventory, BlockPosPayload payload) {
        super(ScreenHandlerTypeInit.ARC_FURNACE, 19, syncId, playerInventory, payload, ArcFurnaceBlockEntity.class);
    }

    public ArcFurnaceScreenHandler(int syncId, Inventory playerInventory, ArcFurnaceBlockEntity blockEntity, WrappedContainerStorage<?> wrappedContainerStorage, ContainerData properties) {
        super(ScreenHandlerTypeInit.ARC_FURNACE, syncId, playerInventory, blockEntity, wrappedContainerStorage, properties);
    }

    @Override
    protected int getInventorySize() {
        return 18;
    }

    @Override
    protected List<Block> getValidBlocks() {
        return List.of(BlockInit.ARC_FURNACE);
    }

    @Override
    protected void addBlockEntitySlots(Inventory playerInventory) {
        SyncingSimpleInventory inputInventory = this.blockEntity.getInputInventory();
        SyncingSimpleInventory outputInventory = this.blockEntity.getOutputInventory();


        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                addSlot(new Slot(inputInventory, (x + y * 3), 26 + x * 18, 18 + y * 18));
            }
        }

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                addSlot(new Slot(outputInventory, (x + y * 3), 112 + x * 18, 18 + y * 18));
            }
        }
    }

    @Override
    protected int getPlayerInventoryY() {
        return 119;
    }

    public ArcFurnaceBlockEntity.Mode getMode() {
        return ArcFurnaceBlockEntity.Mode.values()[this.propertyDelegate.get(0)];
    }

    public int getProgress(int index) {
        return this.propertyDelegate.get(1 + index);
    }

    public int getMaxProgress(int index) {
        return this.propertyDelegate.get(10 + index);
    }

    public float getProgressPercent(int index) {
        float progress = getProgress(index);
        float maxProgress = getMaxProgress(index);
        if (maxProgress == 0 || progress == 0)
            return 0.0F;

        return Mth.clamp(progress / maxProgress, 0.0F, 1.0F);
    }

    public int getTemperature() {
        return (int) this.blockEntity.getHeatStorage().getAmount();
    }
}
