package dev.turtywurty.industria.screenhandler;

import dev.turtywurty.industria.blockentity.DrillBlockEntity;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedInventoryStorage;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.ScreenHandlerTypeInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.screenhandler.base.IndustriaScreenHandler;
import dev.turtywurty.industria.screenhandler.slot.OutputSlot;
import dev.turtywurty.industria.screenhandler.slot.PredicateSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import team.reborn.energy.api.EnergyStorage;

public class DrillScreenHandler extends IndustriaScreenHandler<DrillBlockEntity, BlockPosPayload> {
    public DrillScreenHandler(int syncId, PlayerInventory playerInv, BlockPosPayload payload) {
        super(ScreenHandlerTypeInit.DRILL, syncId, playerInv, payload, DrillBlockEntity.class);
    }

    public DrillScreenHandler(int syncId, PlayerInventory playerInv, DrillBlockEntity blockEntity, WrappedInventoryStorage<?> wrappedInventoryStorage) {
        super(ScreenHandlerTypeInit.DRILL, syncId, playerInv, blockEntity, wrappedInventoryStorage);
    }

    @Override
    protected int getInventorySize() {
        return 14;
    }

    @Override
    protected void addBlockEntitySlots(PlayerInventory playerInventory) {
        SimpleInventory drillHeadInventory = this.wrappedInventoryStorage.getInventory(0);
        SimpleInventory motorInventory = this.wrappedInventoryStorage.getInventory(1);
        SimpleInventory outputInventory = this.wrappedInventoryStorage.getInventory(2);
        SimpleInventory placeableBlockInventory = this.wrappedInventoryStorage.getInventory(3);
        addSlot(new PredicateSlot(drillHeadInventory, 0, 80, 35) {
            @Override
            public boolean isEnabled() {
                return !DrillScreenHandler.this.blockEntity.isDrilling();
            }

            @Override
            public boolean canInsert(ItemStack stack) {
                return !DrillScreenHandler.this.blockEntity.isDrilling() && super.canInsert(stack);
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
    public boolean canUse(PlayerEntity player) {
        return canUse(this.context, player, BlockInit.DRILL);
    }

    public float getEnergyPercentage() {
        EnergyStorage energyStorage = this.blockEntity.getEnergyStorage();
        long energy = energyStorage.getAmount();
        long capacity = energyStorage.getCapacity();
        if (energy == 0 || capacity == 0) return 0;

        return MathHelper.clamp((float) energy / capacity, 0, 1);
    }

    public int getTargetRPM() {
        float targetRotationSpeed = this.blockEntity.getTargetRotationSpeed();
        return (int) (targetRotationSpeed * 60);
    }
}
