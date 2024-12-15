package dev.turtywurty.industria.screenhandler;

import dev.turtywurty.industria.blockentity.MotorBlockEntity;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.ScreenHandlerTypeInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.util.math.MathHelper;
import team.reborn.energy.api.EnergyStorage;

public class MotorScreenHandler extends ScreenHandler {
    private final MotorBlockEntity blockEntity;
    private final ScreenHandlerContext context;

    public MotorScreenHandler(int syncId, PlayerInventory playerInv, BlockPosPayload payload) {
        this(syncId, (MotorBlockEntity) playerInv.player.getWorld().getBlockEntity(payload.pos()));
    }

    public MotorScreenHandler(int syncId, MotorBlockEntity blockEntity) {
        super(ScreenHandlerTypeInit.MOTOR, syncId);

        this.blockEntity = blockEntity;
        this.context = ScreenHandlerContext.create(blockEntity.getWorld(), blockEntity.getPos());
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(this.context, player, BlockInit.MOTOR);
    }

    public float getEnergyPercentage() {
        EnergyStorage energyStorage = this.blockEntity.getEnergyStorage();
        long energy = energyStorage.getAmount();
        long capacity = energyStorage.getCapacity();
        if(energy == 0 || capacity == 0) return 0;

        return MathHelper.clamp((float) energy / capacity, 0, 1);
    }

    public MotorBlockEntity getBlockEntity() {
        return this.blockEntity;
    }

    public int getTargetRPM() {
        float targetRotationSpeed = this.blockEntity.getTargetRotationSpeed();
        return (int) (targetRotationSpeed * 60);
    }
}
