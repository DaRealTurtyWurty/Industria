package dev.turtywurty.industria.blockentity;

import dev.turtywurty.heatapi.api.base.SimpleHeatStorage;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.SyncableTickableBlockEntity;
import dev.turtywurty.industria.blockentity.util.UpdatableBlockEntity;
import dev.turtywurty.industria.blockentity.util.energy.SyncingEnergyStorage;
import dev.turtywurty.industria.blockentity.util.energy.WrappedEnergyStorage;
import dev.turtywurty.industria.blockentity.util.fluid.SyncingFluidStorage;
import dev.turtywurty.industria.blockentity.util.heat.FluidHeatStorage;
import dev.turtywurty.industria.blockentity.util.heat.SyncingNoLimitHeatStorage;
import dev.turtywurty.industria.blockentity.util.heat.WrappedFluidHeatStorage;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.screenhandler.InductionHeaterScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InductionHeaterBlockEntity extends UpdatableBlockEntity implements SyncableTickableBlockEntity, ExtendedScreenHandlerFactory<BlockPosPayload> {
    public static final Text TITLE = Industria.containerTitle("induction_heater");

    private final WrappedEnergyStorage energyStorage = new WrappedEnergyStorage();
    private final WrappedFluidHeatStorage heatWaterStorage = new WrappedFluidHeatStorage();

    public InductionHeaterBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypeInit.INDUCTION_HEATER, pos, state);

        this.heatWaterStorage.addStorage(new FluidHeatStorage(
                new SyncingNoLimitHeatStorage(this, false, true),
                new SyncingFluidStorage(this, FluidConstants.BUCKET * 10)));
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
        if (this.world == null || this.world.isClient)
            return;

        SimpleHeatStorage heatStorage = getHeatStorage();
        SyncingFluidStorage waterTank = getWaterTank();
        if (waterTank.amount < FluidConstants.BUCKET) {
            if (heatStorage.getAmount() > 0) {
                heatStorage.setAmount(Math.max(0, heatStorage.getAmount() - 1));
                update();
            }

            return;
        }

        SyncingEnergyStorage energyStorage = getEnergyStorage();
        if (energyStorage.amount <= 0) {
            heatStorage.setAmount(MathHelper.clamp(heatStorage.getAmount() - 1, 0, 500));
            update();
            return;
        }

        heatStorage.setAmount((long) MathHelper.clamp(heatStorage.getAmount() + 1, 0, ((float) waterTank.amount / (FluidConstants.BUCKET * 10)) * 500));
        waterTank.amount -= FluidConstants.INGOT;
        energyStorage.amount = Math.max(0, energyStorage.amount - 100);
        update();
    }

    @Override
    public void endTick() {
        SimpleHeatStorage currentHeatStorage = getHeatStorage();

        long currentTemp = currentHeatStorage.getAmount();
        if (currentHeatStorage.isAboveRoomTemperature()) {
            Map<Direction, FluidHeatStorage> storageMap = new HashMap<>();
            for (Direction direction : Direction.values()) {
                FluidHeatStorage heatStorage = FluidHeatStorage.SIDED.find(this.world, this.pos.offset(direction), direction.getOpposite());
                if (heatStorage == null || !heatStorage.heatStorage().supportsInsertion() || heatStorage.heatStorage().getAmount() >= currentTemp)
                    continue;

                storageMap.put(direction, heatStorage);
            }

            if (!storageMap.isEmpty()) {
                long tempToTransfer = currentTemp / storageMap.size();
                for (Map.Entry<Direction, FluidHeatStorage> entry : storageMap.entrySet()) {
                    FluidHeatStorage heatStorage = entry.getValue();
                    try (Transaction transaction = Transaction.openOuter()) {
                        long amount = heatStorage.heatStorage().insert(tempToTransfer, transaction);
                        currentTemp -= amount;

                        heatStorage.fluidStorage().insert(getFluidHeatStorage().fluidStorage().variant, FluidConstants.BUCKET, transaction);

                        transaction.commit();
                    }
                }

                currentHeatStorage.setAmount(currentTemp);
            }
        }

        super.endTick();
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

        nbt.put("HeatWaterStorage", this.heatWaterStorage.writeNbt(registries));
        nbt.put("EnergyStorage", this.energyStorage.writeNbt(registries));
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);

        if (nbt.contains("HeatWaterStorage", NbtElement.LIST_TYPE))
            this.heatWaterStorage.readNbt(nbt.getList("HeatWaterStorage", NbtElement.COMPOUND_TYPE), registries);

        if (nbt.contains("EnergyStorage", NbtElement.LIST_TYPE))
            this.energyStorage.readNbt(nbt.getList("EnergyStorage", NbtElement.COMPOUND_TYPE), registries);
    }

    public FluidHeatStorage getFluidHeatStorage() {
        return this.heatWaterStorage.getStorage(0);
    }

    public SyncingFluidStorage getWaterTank() {
        return (SyncingFluidStorage) getFluidHeatStorage().fluidStorage();
    }

    public SimpleHeatStorage getHeatStorage() {
        return (SimpleHeatStorage) getFluidHeatStorage().heatStorage();
    }

    public SyncingEnergyStorage getEnergyStorage() {
        return (SyncingEnergyStorage) this.energyStorage.getStorage(0);
    }

    public Storage<FluidVariant> getFluidProvider(@Nullable Direction direction) {
        return getWaterTank();
    }

    public EnergyStorage getEnergyProvider(@Nullable Direction direction) {
        return getEnergyStorage();
    }

    public @Nullable FluidHeatStorage getFluidHeatProvider(@Nullable Direction direction) {
        return getFluidHeatStorage();
    }
}
