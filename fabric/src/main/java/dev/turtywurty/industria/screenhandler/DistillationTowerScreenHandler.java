package dev.turtywurty.industria.screenhandler;

import dev.turtywurty.industria.blockentity.DistillationTowerBlockEntity;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorage;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.ScreenHandlerTypeInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.screenhandler.base.IndustriaScreenHandler;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.Block;

import java.util.Collections;
import java.util.List;

public class DistillationTowerScreenHandler extends IndustriaScreenHandler<DistillationTowerBlockEntity, BlockPosPayload> {
    public DistillationTowerScreenHandler(int syncId, Inventory playerInv, BlockPosPayload payload) {
        super(ScreenHandlerTypeInit.DISTILLATION_TOWER, 2, syncId, playerInv, payload, DistillationTowerBlockEntity.class);
    }

    public DistillationTowerScreenHandler(int syncId, Inventory playerInv, DistillationTowerBlockEntity blockEntity, WrappedContainerStorage<?> wrappedContainerStorage, ContainerData properties) {
        super(ScreenHandlerTypeInit.DISTILLATION_TOWER, syncId, playerInv, blockEntity, wrappedContainerStorage, properties);
    }

    @Override
    protected int getInventorySize() {
        return 0;
    }

    @Override
    protected List<Block> getValidBlocks() {
        return Collections.singletonList(BlockInit.DISTILLATION_TOWER);
    }

    @Override
    protected void addBlockEntitySlots(Inventory playerInventory) {
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.context, player, BlockInit.DISTILLATION_TOWER);
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
