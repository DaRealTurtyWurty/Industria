package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.SyncableTickableBlockEntity;
import dev.turtywurty.industria.blockentity.util.UpdatableBlockEntity;
import dev.turtywurty.industria.blockentity.util.energy.SyncingEnergyStorage;
import dev.turtywurty.industria.blockentity.util.energy.WrappedEnergyStorage;
import dev.turtywurty.industria.blockentity.util.fluid.SyncingFluidStorage;
import dev.turtywurty.industria.blockentity.util.fluid.WrappedFluidStorage;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.screenhandler.InductionHeaterScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluids;
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

import java.util.List;

public class InductionHeaterBlockEntity extends UpdatableBlockEntity implements SyncableTickableBlockEntity, ExtendedScreenHandlerFactory<BlockPosPayload> {
    public static final Text TITLE = Industria.containerTitle("induction_heater");

    private final WrappedFluidStorage<SingleFluidStorage> waterStorage = new WrappedFluidStorage<>();
    private final WrappedEnergyStorage energyStorage = new WrappedEnergyStorage();

    private float temperature;

    public InductionHeaterBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypeInit.INDUCTION_HEATER, pos, state);

        this.waterStorage.addStorage(new SyncingFluidStorage(this, FluidConstants.BUCKET * 10));
        this.energyStorage.addStorage(new SyncingEnergyStorage(this, 50_000, 1_000, 0));
    }

    @Override
    public List<SyncableStorage> getSyncableStorages() {
        SyncingFluidStorage waterTank = getWaterTank();
        SyncingEnergyStorage energyStorage = getEnergyStorage();
        return List.of(waterTank, energyStorage);
    }

    @Override
    public void onTick() {
        if(this.world == null || this.world.isClient)
            return;

        SyncingFluidStorage waterTank = getWaterTank();
        if (waterTank.amount < FluidConstants.BUCKET) {
            if(this.temperature > 0) {
                this.temperature = 0;
                update();
            }

            return;
        }

        SyncingEnergyStorage energyStorage = getEnergyStorage();
        if(energyStorage.amount <= 0) {
            this.temperature = MathHelper.clamp(this.temperature - 1F, 0, 500);
            update();
            return;
        }

        this.temperature = MathHelper.clamp(this.temperature + 0.1F, 0, ((float) waterTank.amount / (FluidConstants.BUCKET * 10)) * 500);
        waterTank.amount -= FluidConstants.INGOT;
        energyStorage.amount = Math.max(0, energyStorage.amount - 100);
        update();
    }

    private void insertTestWater(SingleFluidStorage waterTank) {
        try(Transaction transaction = Transaction.openOuter()) {
            waterTank.insert(FluidVariant.of(Fluids.WATER), FluidConstants.BUCKET, transaction);
            transaction.commit();
        }
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
        return new InductionHeaterScreenHandler(syncId, this);
    }

    @Override
    public @Nullable Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries) {
        NbtCompound nbt = super.toInitialChunkDataNbt(registries);
        writeNbt(nbt, registries);
        return nbt;
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);

        nbt.put("WaterStorage", this.waterStorage.writeNbt(registries));
        nbt.put("EnergyStorage", this.energyStorage.writeNbt(registries));

        nbt.putFloat("Temperature", this.temperature);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);

        if(nbt.contains("WaterStorage", NbtElement.LIST_TYPE))
            this.waterStorage.readNbt(nbt.getList("WaterStorage", NbtElement.COMPOUND_TYPE), registries);

        if(nbt.contains("EnergyStorage", NbtElement.LIST_TYPE))
            this.energyStorage.readNbt(nbt.getList("EnergyStorage", NbtElement.COMPOUND_TYPE), registries);

        this.temperature = nbt.getFloat("Temperature");
    }

    public SingleFluidStorage getFluidProvider(Direction side) {
        return this.waterStorage.getStorage(side);
    }

    public SyncingFluidStorage getWaterTank() {
        return (SyncingFluidStorage) getFluidProvider(null);
    }

    public EnergyStorage getEnergyProvider(Direction side) {
        return this.energyStorage.getStorage(side);
    }

    public SyncingEnergyStorage getEnergyStorage() {
        return (SyncingEnergyStorage) getEnergyProvider(null);
    }

    public float getTemperature() {
        return this.temperature;
    }
}
