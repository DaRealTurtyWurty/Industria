package dev.turtywurty.industria.blockentity;

import dev.turtywurty.heatapi.api.HeatStorage;
import dev.turtywurty.heatapi.api.base.SimpleHeatStorage;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.abstraction.BlockEntityWithGui;
import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.SyncableTickableBlockEntity;
import dev.turtywurty.industria.blockentity.util.energy.SyncingEnergyStorage;
import dev.turtywurty.industria.blockentity.util.energy.WrappedEnergyStorage;
import dev.turtywurty.industria.blockentity.util.fluid.InputFluidStorage;
import dev.turtywurty.industria.blockentity.util.fluid.WrappedFluidStorage;
import dev.turtywurty.industria.blockentity.util.heat.OutputHeatStorage;
import dev.turtywurty.industria.blockentity.util.heat.WrappedHeatStorage;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.screenhandler.InductionHeaterScreenHandler;
import dev.turtywurty.industria.util.ViewUtils;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

import java.util.List;

public class InductionHeaterBlockEntity extends IndustriaBlockEntity implements SyncableTickableBlockEntity, BlockEntityWithGui<BlockPosPayload> {
    public static final Component TITLE = Industria.containerTitle("induction_heater");
    private static final double HEAT_PER_ENERGY = 0.5D;
    private static final double PASSIVE_COOLING = 0.01D;
    private static final double TRANSFER_COEFFICIENT = 0.25D;
    private final WrappedEnergyStorage energyStorage = new WrappedEnergyStorage();
    private final WrappedHeatStorage<SimpleHeatStorage> heatStorage = new WrappedHeatStorage<>();
    private final WrappedFluidStorage<SingleFluidStorage> waterStorage = new WrappedFluidStorage<>();

    public InductionHeaterBlockEntity(BlockPos pos, BlockState state) {
        super(BlockInit.INDUCTION_HEATER, BlockEntityTypeInit.INDUCTION_HEATER, pos, state);

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
        if (this.level == null || this.level.isClientSide())
            return;

        OutputHeatStorage heatStorage = getHeatStorage();
        InputFluidStorage waterStorage = getWaterStorage();
        SyncingEnergyStorage energyStorage = getEnergyStorage();

        if (energyStorage.amount > 0 && waterStorage.amount >= FluidConstants.DROPLET) {
            increaseHeat(heatStorage, waterStorage, energyStorage);
        } else if (heatStorage.getAmount() > 0) {
            cool(heatStorage);
        }
    }

    @Override
    public void endTick() {
        OutputHeatStorage heatStorage = getHeatStorage();
        for (Direction direction : Direction.values()) {
            BlockPos targetPos = this.worldPosition.relative(direction);
            HeatStorage targetHeat = HeatStorage.SIDED.find(this.level, targetPos, direction.getOpposite());
            if (targetHeat != null && targetHeat.supportsInsertion()) {
                double heatDifference = heatStorage.getAmount() - targetHeat.getAmount();
                if (heatDifference > 0) {
                    double transferAmount = heatDifference * TRANSFER_COEFFICIENT;
                    transferAmount = Math.min(transferAmount, heatStorage.getAmount());
                    try (Transaction transaction = Transaction.openOuter()) {
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

    private void cool(SimpleHeatStorage heatStorage) {
        double newAmount = heatStorage.getAmount() * (1 - PASSIVE_COOLING);
        heatStorage.setAmount(Math.max(0, newAmount));
        update();
    }

    private void increaseHeat(SimpleHeatStorage heatStorage, SingleFluidStorage waterTank, SyncingEnergyStorage energyStorage) {
        long energyConsumed = Math.min(energyStorage.amount, 20);
        double heatIncrease = energyConsumed * HEAT_PER_ENERGY;
        long maxHeat = (long) (((double) waterTank.amount / (FluidConstants.BUCKET * 10)) * 500);
        heatStorage.setAmount(Mth.clamp(heatStorage.getAmount() + heatIncrease, 0, maxHeat));
        waterTank.amount -= FluidConstants.DROPLET;
        energyStorage.amount -= energyConsumed;
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
        return new InductionHeaterScreenHandler(syncId, this);
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        ViewUtils.putChild(view, "HeatStorage", this.heatStorage);
        ViewUtils.putChild(view, "WaterStorage", this.waterStorage);
        ViewUtils.putChild(view, "EnergyStorage", this.energyStorage);
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        ViewUtils.readChild(view, "HeatStorage", this.heatStorage);
        ViewUtils.readChild(view, "WaterStorage", this.waterStorage);
        ViewUtils.readChild(view, "EnergyStorage", this.energyStorage);
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