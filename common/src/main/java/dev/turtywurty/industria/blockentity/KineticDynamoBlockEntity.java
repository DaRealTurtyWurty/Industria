package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.SyncableTickableBlockEntity;
import dev.turtywurty.industria.blockentity.util.energy.EnergySpreader;
import dev.turtywurty.industria.blockentity.util.energy.SyncingEnergyStorage;
import dev.turtywurty.industria.blockentity.util.energy.WrappedEnergyStorage;
import dev.turtywurty.industria.init.ModBlockEntityTypes;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.mechanical.MechanicalConsumer;
import dev.turtywurty.industria.mechanical.MechanicalSimulation;
import dev.turtywurty.industria.util.ViewUtils;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import dev.turtywurty.turtymultiloader.transfer.resource.UnitResource;
import dev.turtywurty.turtymultiloader.transfer.storage.ResourceStorage;
import dev.turtywurty.turtymultiloader.transfer.transaction.TransferTransaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.List;

public class KineticDynamoBlockEntity extends IndustriaBlockEntity
    implements SyncableTickableBlockEntity,
        MechanicalConsumer,
        EnergySpreader {
    private static final double MAX_TORQUE = 16.0;
    private static final double EFFICIENCY = 0.85;
    private static final double ENERGY_PER_RPM_TORQUE = 0.1;
    private static final double RATED_RPM = 20.0;
    private static final double ENERGY_PER_WORK = ENERGY_PER_RPM_TORQUE
        / (MechanicalSimulation.SECONDS_PER_TICK * MechanicalSimulation.RADIANS_PER_RPM);

    private final WrappedEnergyStorage wrappedEnergyStorage = new WrappedEnergyStorage();
    private double fractionalEnergy = 0.0;

    public KineticDynamoBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.KINETIC_DYNAMO.get(), ModBlockEntityTypes.KINETIC_DYNAMO.get(), pos, state);
        wrappedEnergyStorage.addStorage(new SyncingEnergyStorage(this, 5_000, 0, 5_000));
    }

    public WrappedEnergyStorage getWrappedEnergyStorage() {
        return wrappedEnergyStorage;
    }

    public SyncingEnergyStorage getEnergyStorage() {
        return (SyncingEnergyStorage) wrappedEnergyStorage.getStorage(0);
    }

    @Override
    public double getRequiredTorque(double rpm) {
        if (Math.abs(rpm) < 0.001)
            return 0;

        SyncingEnergyStorage energyStorage = getEnergyStorage();
        double freeSpace = energyStorage.getCapacity() - energyStorage.getAmount() - fractionalEnergy;
        if (freeSpace <= 0)
            return 0;

        double energyPerTorque = Math.abs(rpm) * ENERGY_PER_RPM_TORQUE * EFFICIENCY;
        double load = MAX_TORQUE * Math.min(1.0, Math.abs(rpm) / RATED_RPM);
        return Math.min(freeSpace / energyPerTorque, load);
    }

    @Override
    public LoadPolicy getLoadPolicy() {
        return LoadPolicy.ACCEPT_PARTIAL_TORQUE;
    }

    @Override
    public void onMechanicalUpdate(double rpm, double allocatedTorque, double work) {
        if (work <= 0.0)
            return;

        double generated = work * ENERGY_PER_WORK * EFFICIENCY;
        fractionalEnergy += generated;
        long wholeEnergy = (long) Math.floor(fractionalEnergy);
        fractionalEnergy -= wholeEnergy;

        SyncingEnergyStorage energyStorage = getEnergyStorage();
        long freeSpace = energyStorage.getCapacity() - energyStorage.getAmount();
        if (wholeEnergy > freeSpace) {
            wholeEnergy = freeSpace;
        }

        try (TransferTransaction transaction = TransferTransaction.openRoot()) {
            energyStorage.setAmount(energyStorage.getAmount() + wholeEnergy, transaction);
            transaction.commit();
        }

        setChanged();
    }

    @Override
    public List<SyncableStorage> getSyncableStorages() {
        return List.of(getEnergyStorage());
    }

    @Override
    public void onTick() {
        if (level == null || level.isClientSide())
            return;

        spread(level, worldPosition, getEnergyStorage());
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putDouble("FractionalEnergy", fractionalEnergy);
        ViewUtils.putChild(output, "EnergyStorage", this.wrappedEnergyStorage);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.fractionalEnergy = input.getDoubleOr("FractionalEnergy", 0);
        ViewUtils.readChild(input, "EnergyStorage", this.wrappedEnergyStorage);
    }

    public ResourceStorage<ResourceVariant<UnitResource>> getEnergyProvider(Direction direction) {
        return this.wrappedEnergyStorage.getStorage(direction);
    }
}
