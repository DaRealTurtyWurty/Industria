package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.SyncableTickableBlockEntity;
import dev.turtywurty.industria.blockentity.util.energy.SyncingEnergyStorage;
import dev.turtywurty.industria.blockentity.util.energy.WrappedEnergyStorage;
import dev.turtywurty.industria.blockentity.util.fluid.OutputFluidStorage;
import dev.turtywurty.industria.blockentity.util.fluid.SyncingFluidStorage;
import dev.turtywurty.industria.blockentity.util.fluid.WrappedFluidStorage;
import dev.turtywurty.industria.init.ModBlockEntityTypes;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.util.FluidAmounts;
import dev.turtywurty.industria.util.MathUtils;
import dev.turtywurty.industria.util.ViewUtils;
import dev.turtywurty.turtymultiloader.transfer.TransferService;
import dev.turtywurty.turtymultiloader.transfer.lookup.StorageKeys;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceTypes;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import dev.turtywurty.turtymultiloader.transfer.resource.UnitResource;
import dev.turtywurty.turtymultiloader.transfer.storage.ResourceStorage;
import dev.turtywurty.turtymultiloader.transfer.storage.SimpleEnergyStorage;
import dev.turtywurty.turtymultiloader.transfer.transaction.TransferTransaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static dev.turtywurty.industria.blockentity.util.StorageOperations.extract;
import static dev.turtywurty.industria.blockentity.util.StorageOperations.set;

public class FluidPumpBlockEntity extends IndustriaBlockEntity implements SyncableTickableBlockEntity {
    private static final Direction[] HORIZONTAL_DIRECTIONS = {
            Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST
    };

    private final WrappedFluidStorage<SyncingFluidStorage> wrappedFluidStorage = new WrappedFluidStorage<>();
    private final WrappedEnergyStorage wrappedEnergyStorage = new WrappedEnergyStorage();
    private int nextDrainIndex;

    public FluidPumpBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.FLUID_PUMP.get(), ModBlockEntityTypes.FLUID_PUMP.get(), pos, state);
        this.wrappedFluidStorage.addStorage(new OutputFluidStorage(this, FluidAmounts.BUCKET * 10), Direction.EAST);
        this.wrappedEnergyStorage.addStorage(new SyncingEnergyStorage(this, 50_000, 1_000, 0), Direction.UP);
    }

    private static boolean isEmpty(SyncingFluidStorage storage) {
        return storage.getAmount() <= 0 || storage.getResource().isBlank();
    }

    @Override
    public List<SyncableStorage> getSyncableStorages() {
        return List.of(this.wrappedFluidStorage.getStorage(0), (SyncableStorage) this.wrappedEnergyStorage.getStorage(0));
    }

    @Override
    public void onTick() {
        if (this.level == null || this.level.isClientSide())
            return;

        SyncingFluidStorage fluidStorage = this.wrappedFluidStorage.getStorage(Direction.EAST);
        if (!isEmpty(fluidStorage)) {
            Direction outputDirection = getFluidOutputDirection();
            BlockPos outputPos = this.worldPosition.relative(outputDirection);
            ResourceStorage<ResourceVariant<Fluid>> storage = TransferService.get().findBlock(StorageKeys.FLUID, this.level, outputPos, outputDirection.getOpposite());
            if (storage != null) {
                try (TransferTransaction transaction = TransferTransaction.openRoot()) {
                    long inserted = storage.insert(fluidStorage.getResource(), fluidStorage.getAmount(), transaction);
                    if (inserted > 0) {
                        fluidStorage.extractInternal(fluidStorage.getResource(), inserted, transaction);
                        update();
                    }

                    transaction.commit();
                }
            }
        }

        if (this.level.getGameTime() % 10 != 0)
            return;

        SimpleEnergyStorage energyStorage = (SimpleEnergyStorage) this.wrappedEnergyStorage.getStorage(Direction.UP);
        if (energyStorage.getAmount() < 10
                || fluidStorage.getCapacity() - fluidStorage.getAmount() < FluidAmounts.BUCKET)
            return;

        Fluid surroundingFluid = getSurroundingFluid(fluidStorage);
        if (surroundingFluid == null)
            return;

        DrainTarget drainTarget = findSourceBlock(surroundingFluid);
        if (drainTarget == null)
            return;

        BlockPos sourcePos = drainTarget.pos();
        BlockState sourceState = this.level.getBlockState(sourcePos);
        if (!(sourceState.getBlock() instanceof BucketPickup bucketPickup))
            return;

        ItemStack pickedUp = bucketPickup.pickupBlock(null, this.level, sourcePos, sourceState);
        if (pickedUp.isEmpty())
            return;

        this.nextDrainIndex = (drainTarget.index() + 1) % HORIZONTAL_DIRECTIONS.length;
        FluidState sourceFluidState = sourceState.getFluidState();
        ResourceVariant<Fluid> pumpedFluid = ResourceTypes.FLUID.of(sourceFluidState.getType().builtInRegistryHolder());
        set(fluidStorage, pumpedFluid, fluidStorage.getAmount() + FluidAmounts.BUCKET);
        extract(energyStorage, 10);
        update();
    }

    private @Nullable Fluid getSurroundingFluid(SyncingFluidStorage fluidStorage) {
        Fluid requiredFluid = isEmpty(fluidStorage) ? null : fluidStorage.getResource().value();

        for (Direction direction : HORIZONTAL_DIRECTIONS) {
            FluidState fluidState = this.level.getFluidState(this.worldPosition.relative(direction));
            if (fluidState.isEmpty())
                return null;

            if (requiredFluid == null) {
                requiredFluid = fluidState.getType();
            } else if (!requiredFluid.isSame(fluidState.getType())) {
                return null;
            }
        }

        return requiredFluid;
    }

    private @Nullable DrainTarget findSourceBlock(Fluid requiredFluid) {
        for (int checked = 0; checked < HORIZONTAL_DIRECTIONS.length; checked++) {
            int index = (this.nextDrainIndex + checked) % HORIZONTAL_DIRECTIONS.length;
            BlockPos candidate = this.worldPosition.relative(HORIZONTAL_DIRECTIONS[index]);

            FluidState fluidState = this.level.getFluidState(candidate);
            if (!fluidState.isSource() || !requiredFluid.isSame(fluidState.getType()))
                continue;

            BlockState state = this.level.getBlockState(candidate);
            if (!(state.getBlock() instanceof BucketPickup))
                continue;

            return new DrainTarget(candidate, index);
        }

        return null;
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        ViewUtils.putChild(view, "FluidTank", this.wrappedFluidStorage);
        ViewUtils.putChild(view, "Energy", this.wrappedEnergyStorage);
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        ViewUtils.readChild(view, "FluidTank", this.wrappedFluidStorage);
        ViewUtils.readChild(view, "Energy", this.wrappedEnergyStorage);
    }

    public @Nullable SyncingFluidStorage getFluidProvider(Direction side) {
        return side == getFluidOutputDirection()
                ? this.wrappedFluidStorage.getStorage(Direction.EAST)
                : null;
    }

    public @Nullable ResourceStorage<ResourceVariant<UnitResource>> getEnergyProvider(Direction side) {
        return side == Direction.UP
                ? this.wrappedEnergyStorage.getStorage(Direction.UP)
                : null;
    }

    public SyncingEnergyStorage getEnergyStorage() {
        return (SyncingEnergyStorage) this.wrappedEnergyStorage.getStorage(Direction.UP);
    }

    public SyncingFluidStorage getFluidTank() {
        return this.wrappedFluidStorage.getStorage(Direction.EAST);
    }

    private Direction getFluidOutputDirection() {
        return MathUtils.getRelativeDirection(
                Direction.EAST,
                getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING));
    }

    private record DrainTarget(BlockPos pos, int index) {
    }
}
