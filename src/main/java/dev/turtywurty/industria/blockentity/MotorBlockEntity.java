package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.abstraction.BlockEntityWithGui;
import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.SyncableTickableBlockEntity;
import dev.turtywurty.industria.blockentity.util.energy.SyncingEnergyStorage;
import dev.turtywurty.industria.blockentity.util.energy.WrappedEnergyStorage;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.screenhandler.MotorScreenHandler;
import dev.turtywurty.industria.util.ViewUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyStorage;

import java.util.List;

public class MotorBlockEntity extends IndustriaBlockEntity implements SyncableTickableBlockEntity, BlockEntityWithGui<BlockPosPayload> {
    public static final Component TITLE = Industria.containerTitle("motor");

    private final WrappedEnergyStorage wrappedEnergyStorage = new WrappedEnergyStorage();
    public float rodRotation = 0.0F; // Client only
    private float currentRotationSpeed = 0.0F, targetRotationSpeed = 0.75F;

    public MotorBlockEntity(BlockPos pos, BlockState state) {
        super(BlockInit.MOTOR, BlockEntityTypeInit.MOTOR, pos, state);

        this.wrappedEnergyStorage.addStorage(new SyncingEnergyStorage(this, 10_000, 1_000, 0));
    }

    public static long calculateEnergyForRotation(float current, float target) {
        return (long) (Math.abs(target) * 60 * 10);
    }

    public static float calculateRotationSpeed(long energy) {
        return energy / (60f * 10f);
    }

    @Override
    public List<SyncableStorage> getSyncableStorages() {
        return List.of((SyncableStorage) this.wrappedEnergyStorage.getStorage(null));
    }

    @Override
    public void onTick() {
        if (this.level == null || this.level.isClientSide())
            return;

        SimpleEnergyStorage energyStorage = (SimpleEnergyStorage) getEnergyStorage();
        float previousRotationSpeed = this.currentRotationSpeed;

        if (energyStorage.getAmount() > 0) {
            long energyRequired = calculateEnergyForRotation(this.currentRotationSpeed, this.targetRotationSpeed);
            if (energyStorage.getAmount() < energyRequired) {
                this.currentRotationSpeed = calculateRotationSpeed(energyStorage.getAmount());
                energyStorage.amount = 0;
            } else {
                energyStorage.amount -= energyRequired;
                this.currentRotationSpeed = this.targetRotationSpeed;
            }
        } else {
            this.currentRotationSpeed = Mth.clamp(this.currentRotationSpeed - 0.01F, 0.0F, this.targetRotationSpeed);
        }

        if (previousRotationSpeed != this.currentRotationSpeed)
            update();
    }

    @Override
    public BlockPosPayload getScreenOpeningData(ServerPlayer player) {
        return new BlockPosPayload(this.worldPosition);
    }

    @Override
    public Component getDisplayName() {
        return TITLE;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new MotorScreenHandler(syncId, this);
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        view.putFloat("RotationSpeed", this.currentRotationSpeed);
        view.putFloat("TargetRotationSpeed", this.targetRotationSpeed);
        ViewUtils.putChild(view, "EnergyStorage", this.wrappedEnergyStorage);
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        this.currentRotationSpeed = view.getFloatOr("RotationSpeed", 0.0F);
        this.targetRotationSpeed = view.getFloatOr("TargetRotationSpeed", 0.0F);
        ViewUtils.readChild(view, "EnergyStorage", this.wrappedEnergyStorage);
    }

    public EnergyStorage getEnergyStorage() {
        return getEnergyProvider(null);
    }

    public EnergyStorage getEnergyProvider(Direction ignored) {
        return this.wrappedEnergyStorage.getStorage(null);
    }

    public float getRotationSpeed() {
        return this.currentRotationSpeed;
    }

    public float getTargetRotationSpeed() {
        return this.targetRotationSpeed;
    }

    public void setTargetRotationSpeed(float targetRotationSpeed) {
        this.targetRotationSpeed = Mth.clamp(targetRotationSpeed, 0.0F, 1.0F);
    }
}