package dev.turtywurty.industria.menu;

import dev.turtywurty.industria.blockentity.DistillationTowerBlockEntity;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorage;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.init.ModMenuTypes;
import dev.turtywurty.industria.menu.base.IndustriaScreenHandler;
import dev.turtywurty.industria.network.BlockPosPayload;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.Block;

import java.util.Collections;
import java.util.List;

public class DistillationTowerScreenHandler extends IndustriaScreenHandler<DistillationTowerBlockEntity, BlockPosPayload> {
    public DistillationTowerScreenHandler(int syncId, Inventory playerInv, BlockPosPayload payload) {
        super(ModMenuTypes.DISTILLATION_TOWER.get(), 2, syncId, playerInv, payload, DistillationTowerBlockEntity.class);
    }

    public DistillationTowerScreenHandler(int syncId, Inventory playerInv, DistillationTowerBlockEntity blockEntity, WrappedContainerStorage<?> wrappedContainerStorage, ContainerData properties) {
        super(ModMenuTypes.DISTILLATION_TOWER.get(), syncId, playerInv, blockEntity, wrappedContainerStorage, properties);
    }

    @Override
    protected int getInventorySize() {
        return 0;
    }

    @Override
    protected List<Block> getValidBlocks() {
        return Collections.singletonList(ModBlocks.DISTILLATION_TOWER.get());
    }

    @Override
    protected void addBlockEntitySlots(Inventory playerInventory) {
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.context, player, ModBlocks.DISTILLATION_TOWER.get());
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
