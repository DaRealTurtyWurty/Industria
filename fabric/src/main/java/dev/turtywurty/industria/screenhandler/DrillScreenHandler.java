package dev.turtywurty.industria.screenhandler;

import dev.turtywurty.industria.blockentity.DrillBlockEntity;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorage;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.ScreenHandlerTypeInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.screenhandler.base.IndustriaScreenHandler;
import dev.turtywurty.industria.screenhandler.slot.OutputSlot;
import dev.turtywurty.industria.screenhandler.slot.PredicateSlot;
import net.minecraft.util.Mth;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import team.reborn.energy.api.EnergyStorage;

public class DrillScreenHandler extends IndustriaScreenHandler<DrillBlockEntity, BlockPosPayload> {
    public DrillScreenHandler(int syncId, Inventory playerInv, BlockPosPayload payload) {
        super(ScreenHandlerTypeInit.DRILL, syncId, playerInv, payload, DrillBlockEntity.class);
    }

    public DrillScreenHandler(int syncId, Inventory playerInv, DrillBlockEntity blockEntity, WrappedContainerStorage<?> wrappedContainerStorage) {
        super(ScreenHandlerTypeInit.DRILL, syncId, playerInv, blockEntity, wrappedContainerStorage);
    }

    @Override
    protected int getInventorySize() {
        return 14;
    }

    @Override
    protected void addBlockEntitySlots(Inventory playerInventory) {
        SimpleContainer drillHeadInventory = this.wrappedContainerStorage.getInventory(0);
        SimpleContainer motorInventory = this.wrappedContainerStorage.getInventory(1);
        SimpleContainer outputInventory = this.wrappedContainerStorage.getInventory(2);
        SimpleContainer placeableBlockInventory = this.wrappedContainerStorage.getInventory(3);
        addSlot(new PredicateSlot(drillHeadInventory, 0, 80, 35) {
            @Override
            public boolean isActive() {
                return !DrillScreenHandler.this.blockEntity.isDrilling();
            }

            @Override
            public boolean mayPlace(ItemStack stack) {
                return !DrillScreenHandler.this.blockEntity.isDrilling() && super.mayPlace(stack);
            }
        });

        addSlot(new PredicateSlot(motorInventory, 0, 80, 53));

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                addSlot(new OutputSlot(outputInventory, column + row * 3, 116 + column * 18, 17 + row * 18));
            }
        }

        for (int index = 0; index < 3; index++) {
            addSlot(new PredicateSlot(placeableBlockInventory, index, 62 + index * 18, 17));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.context, player, BlockInit.DRILL);
    }

    public float getEnergyPercentage() {
        EnergyStorage energyStorage = this.blockEntity.getEnergyStorage();
        long energy = energyStorage.getAmount();
        long capacity = energyStorage.getCapacity();
        if(energy == 0 || capacity == 0) return 0;

        return Mth.clamp((float) energy / capacity, 0, 1);
    }

    public int getTargetRPM() {
        float targetRotationSpeed = this.blockEntity.getTargetRotationSpeed();
        return (int) (targetRotationSpeed * 60);
    }
}
