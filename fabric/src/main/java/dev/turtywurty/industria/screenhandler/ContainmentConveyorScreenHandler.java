package dev.turtywurty.industria.screenhandler;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorage;
import dev.turtywurty.industria.conveyor.block.impl.entity.ContainmentConveyorBlockEntity;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.ScreenHandlerTypeInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.screenhandler.base.IndustriaScreenHandler;
import dev.turtywurty.industria.screenhandler.slot.PredicateSlot;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class ContainmentConveyorScreenHandler extends IndustriaScreenHandler<ContainmentConveyorBlockEntity, BlockPosPayload> {
    public static final Component CAPTURING_STATUS_TEXT = Component.translatable("container." + Industria.MOD_ID + ".containment_conveyor.status.capturing");
    public static final Component IDLE_STATUS_TEXT = Component.translatable("container." + Industria.MOD_ID + ".containment_conveyor.status.idle");

    public ContainmentConveyorScreenHandler(int syncId, Inventory playerInventory, BlockPosPayload payload) {
        super(ScreenHandlerTypeInit.CONTAINMENT_CONVEYOR, syncId, playerInventory, payload, ContainmentConveyorBlockEntity.class);
    }

    public ContainmentConveyorScreenHandler(int syncId, Inventory playerInventory, ContainmentConveyorBlockEntity blockEntity,
                                            WrappedContainerStorage<?> wrappedContainerStorage) {
        super(ScreenHandlerTypeInit.CONTAINMENT_CONVEYOR, syncId, playerInventory, blockEntity, wrappedContainerStorage);
    }

    @Override
    protected int getInventorySize() {
        return 1;
    }

    @Override
    protected void addBlockEntitySlots(Inventory playerInventory) {
        SimpleContainer inventory = this.wrappedContainerStorage.getInventory(0);
        addSlot(new PredicateSlot(inventory, 0, 80, 35));
    }

    @Override
    protected List<Block> getValidBlocks() {
        return List.of(BlockInit.CONTAINMENT_CONVEYOR);
    }

    public float getProgressPercent() {
        int maxProgress = this.blockEntity.getMaxProgress();
        if (maxProgress <= 0)
            return 0.0F;

        return Mth.clamp((float) this.blockEntity.getProgress() / maxProgress, 0.0F, 1.0F);
    }

    public boolean isCapturingEntity() {
        return this.blockEntity.getContainingEntity() != null;
    }

    public Component getStatusText() {
        return isCapturingEntity() ? CAPTURING_STATUS_TEXT : IDLE_STATUS_TEXT;
    }

    public Component getContainingEntityName() {
        var containingEntity = this.blockEntity.getContainingEntity();
        if (containingEntity == null)
            return Component.empty();

        return containingEntity.getDisplayName();
    }
}
