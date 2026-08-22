package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.abstraction.BlockEntityWithGui;
import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.SyncableTickableBlockEntity;
import dev.turtywurty.industria.blockentity.util.energy.SyncingEnergyStorage;
import dev.turtywurty.industria.blockentity.util.energy.WrappedEnergyStorage;
import dev.turtywurty.industria.blockentity.util.fluid.OutputFluidStorage;
import dev.turtywurty.industria.blockentity.util.fluid.SyncingFluidStorage;
import dev.turtywurty.industria.blockentity.util.fluid.WrappedFluidStorage;
import dev.turtywurty.industria.init.ModBlockEntityTypes;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.menu.FluidPumpScreenHandler;
import dev.turtywurty.industria.network.BlockPosPayload;
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
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.turtywurty.industria.blockentity.util.StorageOperations.extract;
import static dev.turtywurty.industria.blockentity.util.StorageOperations.set;

public class FluidPumpBlockEntity extends IndustriaBlockEntity implements SyncableTickableBlockEntity, BlockEntityWithGui<BlockPosPayload> {
    public static final Component TITLE = Industria.containerTitle("fluid_pump");

    private static final Direction[] CHECK_DIRECTIONS = {Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.DOWN};

    private final WrappedFluidStorage<SyncingFluidStorage> wrappedFluidStorage = new WrappedFluidStorage<>();
    private final WrappedEnergyStorage wrappedEnergyStorage = new WrappedEnergyStorage();

    public FluidPumpBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.FLUID_PUMP.get(), ModBlockEntityTypes.FLUID_PUMP.get(), pos, state);
        this.wrappedFluidStorage.addStorage(new OutputFluidStorage(this, FluidAmounts.BUCKET * 10), Direction.EAST);
        this.wrappedEnergyStorage.addStorage(new SyncingEnergyStorage(this, 50_000, 1_000, 0), Direction.UP);
    }

    private static @Nullable FluidState getMostCommon(Map<Direction, FluidState> fluidStateMap) {
        FluidState mostCommon = null;
        int mostCommonCount = 0;

        for (FluidState state : fluidStateMap.values()) {
            int count = 0;
            for (FluidState value : fluidStateMap.values()) {
                if (value.getType() == state.getType())
                    count++;
            }

            if (count > mostCommonCount) {
                mostCommon = state;
                mostCommonCount = count;
            }
        }

        return mostCommon;
    }

    private static boolean isEmpty(SyncingFluidStorage storage) {
        return storage.getAmount() <= 0 || storage.getResource().isBlank();
    }

    @Override
    public List<SyncableStorage> getSyncableStorages() {
        return List.of((SyncableStorage) this.wrappedFluidStorage.getStorage(0), (SyncableStorage) this.wrappedEnergyStorage.getStorage(0));
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

        // check surrounding blocks for fluid
        if (this.level.getGameTime() % 10 == 0) {
            SimpleEnergyStorage energyStorage = (SimpleEnergyStorage) this.wrappedEnergyStorage.getStorage(Direction.UP);
            if (energyStorage.getAmount() <= 10)
                return;

            Direction direction = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
            Map<Direction, FluidState> fluidStateMap = new HashMap<>();
            for (Direction checkDirection : CHECK_DIRECTIONS) {
                Direction relative = MathUtils.getRelativeDirection(checkDirection, direction);
                BlockPos checkPos = this.worldPosition.relative(relative);
                FluidState fluidState = this.level.getFluidState(checkPos);
                if (fluidState.isEmpty())
                    break;

                fluidStateMap.put(relative, fluidState);
            }

            long storedFluidAmount = fluidStorage.getAmount();
            if (storedFluidAmount >= fluidStorage.getCapacity())
                return;

            if (!fluidStateMap.isEmpty()) {
                // find either a fluid that we can insert (assuming we're not empty) or the fluid that is the most common
                if (isEmpty(fluidStorage)) {
                    FluidState mostCommon = getMostCommon(fluidStateMap);

                    if (mostCommon != null) {
                        set(fluidStorage, ResourceTypes.FLUID.of(mostCommon.getType().builtInRegistryHolder()),
                                Math.min(fluidStorage.getCapacity(), fluidStorage.getAmount() + FluidAmounts.BOTTLE));
                    }
                } else {
                    for (FluidState state : fluidStateMap.values()) {
                        if (state.getType() == fluidStorage.getResource().value()) {
                            set(fluidStorage, fluidStorage.getResource(),
                                    Math.min(fluidStorage.getCapacity(), fluidStorage.getAmount() + FluidAmounts.BOTTLE));
                            break;
                        }
                    }
                }
            }

            if (storedFluidAmount != fluidStorage.getAmount()) {
                extract(energyStorage, 10);
                update();
            }
        }
    }

    @Override
    public BlockPosPayload getMenuOpeningData(ServerPlayer player) {
        return new BlockPosPayload(this.worldPosition);
    }

    @Override
    public Component getDisplayName() {
        return TITLE;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new FluidPumpScreenHandler(syncId, this);
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
}
