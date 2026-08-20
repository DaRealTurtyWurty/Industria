package dev.turtywurty.industria.screenhandler;

import dev.turtywurty.industria.blockentity.MotorBlockEntity;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.ScreenHandlerTypeInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import team.reborn.energy.api.EnergyStorage;

public class MotorScreenHandler extends AbstractContainerMenu {
    private final MotorBlockEntity blockEntity;
    private final ContainerLevelAccess context;

    public MotorScreenHandler(int syncId, Inventory playerInv, BlockPosPayload payload) {
        this(syncId, (MotorBlockEntity) playerInv.player.level().getBlockEntity(payload.pos()));
    }

    public MotorScreenHandler(int syncId, MotorBlockEntity blockEntity) {
        super(ScreenHandlerTypeInit.MOTOR, syncId);

        this.blockEntity = blockEntity;
        this.context = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.context, player, BlockInit.MOTOR);
    }

    public float getEnergyPercentage() {
        EnergyStorage energyStorage = this.blockEntity.getEnergyStorage();
        long energy = energyStorage.getAmount();
        long capacity = energyStorage.getCapacity();
        if(energy == 0 || capacity == 0) return 0;

        return Mth.clamp((float) energy / capacity, 0, 1);
    }

    public MotorBlockEntity getBlockEntity() {
        return this.blockEntity;
    }

    public int getTargetRPM() {
        float targetRotationSpeed = this.blockEntity.getTargetRotationSpeed();
        return (int) (targetRotationSpeed * 60);
    }
}
