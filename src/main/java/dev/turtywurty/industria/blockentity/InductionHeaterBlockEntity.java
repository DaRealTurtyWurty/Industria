package dev.turtywurty.industria.blockentity;

import dev.turtywurty.heatapi.api.HeatStorage;
import dev.turtywurty.heatapi.api.base.SimpleHeatStorage;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.abstraction.BlockEntityWithGui;
import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.SyncableTickableBlockEntity;
import dev.turtywurty.industria.blockentity.util.UpdatableBlockEntity;
import dev.turtywurty.industria.blockentity.util.energy.SyncingEnergyStorage;
import dev.turtywurty.industria.blockentity.util.energy.WrappedEnergyStorage;
import dev.turtywurty.industria.blockentity.util.fluid.InputFluidStorage;
import dev.turtywurty.industria.blockentity.util.fluid.WrappedFluidStorage;
import dev.turtywurty.industria.blockentity.util.heat.OutputHeatStorage;
import dev.turtywurty.industria.blockentity.util.heat.WrappedHeatStorage;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.screenhandler.InductionHeaterScreenHandler;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.NbtCompound;
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

public class InductionHeaterBlockEntity extends UpdatableBlockEntity implements SyncableTickableBlockEntity, BlockEntityWithGui<BlockPosPayload> {
    public static final Text TITLE = Industria.containerTitle("induction_heater");

    private final WrappedEnergyStorage energyStorage = new WrappedEnergyStorage();
    private final WrappedHeatStorage<SimpleHeatStorage> heatStorage = new WrappedHeatStorage<>();
    private final WrappedFluidStorage<SingleFluidStorage> waterStorage = new WrappedFluidStorage<>();

    public InductionHeaterBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypeInit.INDUCTION_HEATER, pos, state);

        this.heatStorage.addStorage(new OutputHeatStorage(this, Long.MAX_VALUE, Long.MAX_VALUE));
        this.waterStorage.addStorage(new InputFluidStorage(this, FluidConstants.BUCKET * 10, variant -> variant.isOf(Fluids.WATER)));
        this.energyStorage.addStorage(new SyncingEnergyStorage(this, 50_000, 1_000, 0));
    }

    @Override
    public List<SyncableStorage> getSyncableStorages() {
        SyncingEnergyStorage energyStorage = getEnergyStorage();
        OutputHeatStorage heatStorage = getHeatStorage();
        InputFluidStorage waterStorage = getWaterStorage();

        return List.of(energyStorage, heatStorage, waterStorage);
    }

    @Override
    public void onTick() {
        if (this.world == null || this.world.isClient)
            return;

        OutputHeatStorage heatStorage = getHeatStorage();
        InputFluidStorage waterStorage = getWaterStorage();
        if(waterStorage.amount < FluidConstants.DROPLET) {
            if(heatStorage.getAmount() > 0) {
                subtractHeat(heatStorage);
            }

            return;
        }

        SyncingEnergyStorage energyStorage = getEnergyStorage();
        if(energyStorage.amount <= 0) {
            subtractHeat(heatStorage);
            return;
        }

        increaseHeat(heatStorage, waterStorage, energyStorage);
    }

    @Override
    public void endTick() {
        OutputHeatStorage heatStorage = getHeatStorage();
        for (Direction direction : Direction.values()) {
            BlockPos targetPos = this.pos.offset(direction);
            HeatStorage targetHeat = HeatStorage.SIDED.find(this.world, targetPos, direction.getOpposite());
            if (targetHeat != null && targetHeat.supportsInsertion()) {
                double heatDifference = heatStorage.getAmount() - targetHeat.getAmount();
                if (heatDifference > 0) {
                    double transferAmount = Math.min(heatDifference / 2, heatStorage.getAmount());
                    try(Transaction transaction = Transaction.openOuter()) {
                        double extracted = heatStorage.extract(transferAmount, transaction);
                        double inserted = targetHeat.insert(extracted, transaction);

                        if (extracted == inserted) {
                            transaction.commit();
                        }
                    }
                }
            }
        }

        super.endTick();
    }

    private void subtractHeat(SimpleHeatStorage heatStorage) {
        heatStorage.setAmount(Math.max(0, heatStorage.getAmount() - 1));
        update();
    }

    private void increaseHeat(SimpleHeatStorage heatStorage, SingleFluidStorage waterTank, SyncingEnergyStorage energyStorage) {
        long energyConsumed = Math.min(energyStorage.amount, 10);
        long heatIncrease = energyConsumed / 2; // Example: 1 heat per 2 energy units
        heatStorage.setAmount((long) MathHelper.clamp(heatStorage.getAmount() + heatIncrease, 0, ((float) waterTank.amount / (FluidConstants.BUCKET * 10)) * 500));
        waterTank.amount -= FluidConstants.DROPLET;
        energyStorage.amount -= energyConsumed;
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

        nbt.put("HeatStorage", this.heatStorage.writeNbt(registries));
        nbt.put("WaterStorage", this.waterStorage.writeNbt(registries));
        nbt.put("EnergyStorage", this.energyStorage.writeNbt(registries));
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);

        if (nbt.contains("HeatStorage"))
            this.heatStorage.readNbt(nbt.getListOrEmpty("HeatStorage"), registries);

        if (nbt.contains("WaterStorage"))
            this.waterStorage.readNbt(nbt.getListOrEmpty("WaterStorage"), registries);

        if (nbt.contains("EnergyStorage"))
            this.energyStorage.readNbt(nbt.getListOrEmpty("EnergyStorage"), registries);
    }

    public EnergyStorage getEnergyProvider(Direction side) {
        return this.energyStorage.getStorage(side);
    }

    public SyncingEnergyStorage getEnergyStorage() {
        return (SyncingEnergyStorage) getEnergyProvider(null);
    }

    public OutputHeatStorage getHeatStorage() {
        return (OutputHeatStorage) this.heatStorage.getStorage(null);
    }

    public InputFluidStorage getWaterStorage() {
        return (InputFluidStorage) this.waterStorage.getStorage(null);
    }

    public HeatStorage getHeatProvider(Direction side) {
        return this.heatStorage.getStorage(null);
    }

    public Storage<FluidVariant> getFluidProvider(Direction side) {
        return this.waterStorage.getStorage(null);
    }
}
