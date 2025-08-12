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
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyStorage;

import java.util.List;

public class MotorBlockEntity extends IndustriaBlockEntity implements SyncableTickableBlockEntity, BlockEntityWithGui<BlockPosPayload> {
    public static final Text TITLE = Industria.containerTitle("motor");

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
        if (this.world == null || this.world.isClient)
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
            this.currentRotationSpeed = MathHelper.clamp(this.currentRotationSpeed - 0.01F, 0.0F, this.targetRotationSpeed);
        }

        if (previousRotationSpeed != this.currentRotationSpeed)
            update();
    }

    @Override
    public BlockPosPayload getScreenOpeningData(ServerPlayerEntity player) {
        return new BlockPosPayload(this.pos);
    }

    @Override
    public Text getDisplayName() {
        return TITLE;
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new MotorScreenHandler(syncId, this);
    }

    @Override
    protected void writeData(WriteView view) {
        view.putFloat("RotationSpeed", this.currentRotationSpeed);
        view.putFloat("TargetRotationSpeed", this.targetRotationSpeed);
        ViewUtils.putChild(view, "EnergyStorage", this.wrappedEnergyStorage);
    }

    @Override
    protected void readData(ReadView view) {
        this.currentRotationSpeed = view.getFloat("RotationSpeed", 0.0F);
        this.targetRotationSpeed = view.getFloat("TargetRotationSpeed", 0.0F);
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
        this.targetRotationSpeed = MathHelper.clamp(targetRotationSpeed, 0.0F, 1.0F);
    }
}
