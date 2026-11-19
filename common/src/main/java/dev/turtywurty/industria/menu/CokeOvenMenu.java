package dev.turtywurty.industria.menu;

import dev.turtywurty.industria.blockentity.CokeOvenBlockEntity;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorage;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.init.ModMenuTypes;
import dev.turtywurty.industria.menu.base.IndustriaScreenHandler;
import dev.turtywurty.industria.menu.slot.OutputSlot;
import dev.turtywurty.industria.menu.slot.PredicateSlot;
import dev.turtywurty.industria.network.BlockPosPayload;
import net.minecraft.core.Direction;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;

import java.util.Objects;

public class CokeOvenMenu extends IndustriaScreenHandler<CokeOvenBlockEntity, BlockPosPayload> {
    public CokeOvenMenu(int syncId, Inventory playerInventory, BlockPosPayload payload) {
        super(ModMenuTypes.COKE_OVEN.get(), 5, syncId, playerInventory, payload, CokeOvenBlockEntity.class);
    }

    public CokeOvenMenu(
        int syncId,
        Inventory playerInventory,
        CokeOvenBlockEntity blockEntity,
        WrappedContainerStorage<?> wrappedContainerStorage,
        ContainerData containerData
    ) {
        super(ModMenuTypes.COKE_OVEN.get(), syncId, playerInventory, blockEntity, wrappedContainerStorage,
            containerData);
    }

    @Override
    protected int getInventorySize() {
        return 3;
    }

    @Override
    protected void addBlockEntitySlots(Inventory playerInventory) {
        SimpleContainer fuelInventory = this.wrappedContainerStorage.getInventory(Direction.SOUTH);
        SimpleContainer inputInventory = this.wrappedContainerStorage.getInventory(Direction.UP);
        SimpleContainer outputInventory = this.wrappedContainerStorage.getInventory(Direction.DOWN);
        Objects.requireNonNull(fuelInventory, "Fuel inventory cannot be null");
        Objects.requireNonNull(inputInventory, "Input inventory cannot be null");
        Objects.requireNonNull(outputInventory, "Output inventory cannot be null");

        addSlot(new PredicateSlot(fuelInventory, 0, 56, 53, this.blockEntity::isFuel));
        addSlot(new Slot(inputInventory, 0, 56, 17));
        addSlot(new OutputSlot(outputInventory, 0, 116, 35));
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.context, player, ModBlocks.COKE_OVEN.get());
    }

    public int getTemperature() {
        return this.propertyDelegate.get(0);
    }

    public float getProgressPercent() {
        int maxProgress = this.propertyDelegate.get(2);
        return maxProgress <= 0 ? 0 : Math.clamp((float) this.propertyDelegate.get(1) / maxProgress, 0F, 1F);
    }

    public float getBurnTimePercent() {
        int maxBurnTime = this.propertyDelegate.get(4);
        return maxBurnTime <= 0 ? 0 : Math.clamp((float) this.propertyDelegate.get(3) / maxBurnTime, 0F, 1F);
    }
}
