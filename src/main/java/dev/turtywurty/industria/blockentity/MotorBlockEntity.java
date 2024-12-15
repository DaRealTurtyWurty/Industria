package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.SyncableTickableBlockEntity;
import dev.turtywurty.industria.blockentity.util.UpdatableBlockEntity;
import dev.turtywurty.industria.blockentity.util.energy.SyncingEnergyStorage;
import dev.turtywurty.industria.blockentity.util.energy.WrappedEnergyStorage;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.screenhandler.MotorScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyStorage;

import java.util.List;

public class MotorBlockEntity extends UpdatableBlockEntity implements SyncableTickableBlockEntity, ExtendedScreenHandlerFactory<BlockPosPayload> {
    public static final Text TITLE = Industria.containerTitle("motor");

    private final WrappedEnergyStorage wrappedEnergyStorage = new WrappedEnergyStorage();
    private float currentRotationSpeed = 0.0F, targetRotationSpeed = 0.75F;

    public float rodRotation = 0.0F; // Client only

    public MotorBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypeInit.MOTOR, pos, state);

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
            if(energyStorage.getAmount() < energyRequired) {
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
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        nbt.putFloat("RotationSpeed", this.currentRotationSpeed);
        nbt.putFloat("TargetRotationSpeed", this.targetRotationSpeed);
        nbt.put("EnergyStorage", this.wrappedEnergyStorage.writeNbt(registryLookup));
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        if (nbt.contains("RotationSpeed"))
            this.currentRotationSpeed = nbt.getFloat("RotationSpeed");

        if (nbt.contains("TargetRotationSpeed"))
            this.targetRotationSpeed = nbt.getFloat("TargetRotationSpeed");

        if (nbt.contains("EnergyStorage"))
            this.wrappedEnergyStorage.readNbt(nbt.getList("EnergyStorage", NbtElement.COMPOUND_TYPE), registryLookup);
    }

    @Override
    public @Nullable Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        var nbt = super.toInitialChunkDataNbt(registryLookup);
        writeNbt(nbt, registryLookup);
        return nbt;
    }

    public EnergyStorage getEnergyStorage() {
        return getEnergyProvider(null);
    }

    public EnergyStorage getEnergyProvider(Direction ignored) {
        return this.wrappedEnergyStorage.getStorage(null);
    }

    public void setTargetRotationSpeed(float targetRotationSpeed) {
        this.targetRotationSpeed = MathHelper.clamp(targetRotationSpeed, 0.0F, 1.0F);
    }

    public float getRotationSpeed() {
        return this.currentRotationSpeed;
    }

    public float getTargetRotationSpeed() {
        return this.targetRotationSpeed;
    }
}
