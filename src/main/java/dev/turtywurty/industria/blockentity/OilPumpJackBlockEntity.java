package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.abstraction.BlockEntityWithGui;
import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.SyncableTickableBlockEntity;
import dev.turtywurty.industria.blockentity.util.energy.SyncingEnergyStorage;
import dev.turtywurty.industria.blockentity.util.energy.WrappedEnergyStorage;
import dev.turtywurty.industria.blockentity.util.fluid.OutputFluidStorage;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorage;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorageHolder;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.FluidInit;
import dev.turtywurty.industria.init.MultiblockTypeInit;
import dev.turtywurty.industria.multiblock.LocalDirection;
import dev.turtywurty.industria.multiblock.PortType;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.multiblock.old.AutoMultiblockable;
import dev.turtywurty.industria.multiblock.old.MultiblockType;
import dev.turtywurty.industria.multiblock.old.Multiblockable;
import dev.turtywurty.industria.multiblock.old.PositionedPortRule;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.screenhandler.OilPumpJackScreenHandler;
import dev.turtywurty.industria.util.ViewUtils;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

import java.util.*;

public class OilPumpJackBlockEntity extends IndustriaBlockEntity implements SyncableTickableBlockEntity, BlockEntityWithGui<BlockPosPayload>, AutoMultiblockable, WrappedContainerStorageHolder {
    public static final Component TITLE = Industria.containerTitle("oil_pump_jack");

    private static final List<PositionedPortRule> PORT_RULES = List.of(
            PositionedPortRule.when(p -> p.z() == -4 && p.y() == 0 && p.x() >= -1 && p.x() <= 2)
                    .on(LocalDirection.BACK)
                    .types(PortType.input(TransferType.ENERGY))
                    .build()
    );

    private final WrappedEnergyStorage wrappedEnergyStorage = new WrappedEnergyStorage();
    private final WrappedContainerStorage<?> wrappedContainerStorage = new WrappedContainerStorage<>();
    private final List<BlockPos> machinePositions = new ArrayList<>();
    public float clientRotation;
    public boolean reverseCounterWeights;
    private int ticks;
    private BlockPos wellheadPos;
    private boolean running = false;

    public OilPumpJackBlockEntity(BlockPos pos, BlockState state) {
        super(BlockInit.OIL_PUMP_JACK, BlockEntityTypeInit.OIL_PUMP_JACK, pos, state);

        this.wrappedEnergyStorage.addStorage(new SyncingEnergyStorage(this, 10_000, 1_000, 0));
    }

    private static boolean isValidPosition(Level world, BlockPos position) {
        if (world == null || position == null || !world.isLoaded(position))
            return false;

        BlockState state = world.getBlockState(position);
        return state.canBeReplaced();
    }

    @Override
    public List<SyncableStorage> getSyncableStorages() {
        return List.of(getEnergyStorage());
    }

    public EnergyStorage getEnergyProvider(Direction direction) {
        return this.wrappedEnergyStorage.getStorage(direction);
    }

    public SyncingEnergyStorage getEnergyStorage() {
        return (SyncingEnergyStorage) this.wrappedEnergyStorage.getStorage(0);
    }

    @Override
    public void onTick() {
        if (this.level == null || this.level.isClientSide())
            return;

        if (this.wellheadPos == null && this.ticks++ % 20 == 0) {
            findWellhead();
        }

        if (this.wellheadPos == null)
            return;

        if (this.level.getBlockEntity(this.wellheadPos) instanceof WellheadBlockEntity wellheadBlockEntity && this.running) {
            Map<BlockPos, Integer> drillTubes = wellheadBlockEntity.getDrillTubes();

            List<BlockPos> orderedPositions = new ArrayList<>(drillTubes.keySet());
            orderedPositions.sort(Comparator.comparingInt(Vec3i::getY).reversed());

            // move the fluid in each of the drill tubes up a block
            boolean hasPumped = false;
            for (BlockPos pos : orderedPositions) {
                int amount = drillTubes.get(pos);
                if (amount <= 0)
                    continue;

                BlockPos newPos = pos.above();
                if (drillTubes.containsKey(newPos)) {
                    int currentlyStored = drillTubes.get(newPos);
                    if (currentlyStored < FluidConstants.BUCKET) {
                        int availableSpace = (int) (FluidConstants.BUCKET - currentlyStored);
                        if (amount > availableSpace) {
                            drillTubes.put(newPos, currentlyStored + availableSpace);
                            drillTubes.put(pos, amount - availableSpace);
                        } else {
                            drillTubes.put(newPos, currentlyStored + amount);
                            drillTubes.put(pos, 0);
                        }

                        hasPumped = true;
                    }
                } else if (Objects.equals(newPos, this.wellheadPos)) {
                    OutputFluidStorage storage = wellheadBlockEntity.getFluidStorageBuffer();
                    if (storage != null) {
                        long amountToInsert = Math.min(storage.getCapacity() - storage.getAmount(), amount);
                        if (amountToInsert > 0) {
                            storage.amount += amountToInsert;
                            storage.variant = FluidVariant.of(FluidInit.CRUDE_OIL.still()); // TODO: Temporary solution, pls fix
                            storage.markDirty();
                            drillTubes.put(pos, amount - (int) amountToInsert);

                            hasPumped = true;
                        }
                    }
                }
            }

            if (hasPumped) {
                SyncingEnergyStorage energyStorage = getEnergyStorage();
                if (this.running && energyStorage.getAmount() > 50) {
                    energyStorage.amount -= 50;
                    update();
                } else if (this.running) {
                    this.running = false;
                    update();
                }
            }
        }
    }

    private void findWellhead() {
        BlockPos wellheadPos = this.worldPosition.relative(getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING), 5);
        if (this.level.getBlockEntity(wellheadPos) instanceof WellheadBlockEntity wellheadBlockEntity) {
            if (!wellheadBlockEntity.hasOilPumpJack() && !wellheadBlockEntity.isRemoved()) {
                wellheadBlockEntity.setOilPumpJackPos(this.worldPosition);
                this.wellheadPos = wellheadPos;
            }
        }
    }

    @Override
    public BlockPosPayload getScreenOpeningData(ServerPlayer player) {
        return new BlockPosPayload(this.worldPosition);
    }

    @Override
    public Component getDisplayName() {
        return TITLE;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new OilPumpJackScreenHandler(syncId, playerInventory, this, this.wrappedContainerStorage);
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        Multiblockable.read(this, view);
        this.wellheadPos = view.read("WellheadPos", BlockPos.CODEC).orElse(null);
        ViewUtils.readChild(view, "EnergyStorage", this.wrappedEnergyStorage);
        ViewUtils.readChild(view, "Inventory", this.wrappedContainerStorage);
        this.running = view.getBooleanOr("Running", false);
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        Multiblockable.write(this, view);
        if (this.wellheadPos != null) {
            view.store("WellheadPos", BlockPos.CODEC, this.wellheadPos);
        }
        ViewUtils.putChild(view, "EnergyStorage", this.wrappedEnergyStorage);
        ViewUtils.putChild(view, "Inventory", this.wrappedContainerStorage);
        view.putBoolean("Running", this.running);
    }

    @Override
    public MultiblockType<?> type() {
        return MultiblockTypeInit.OIL_PUMP_JACK;
    }

    @Override
    public List<BlockPos> getMultiblockPositions() {
        return this.machinePositions;
    }

    @Override
    public List<BlockPos> findPositions(Direction facing) {
        if (this.level == null)
            return List.of();

        List<BlockPos> correctPositions = new ArrayList<>();
        List<BlockPos> incorrectPositions = new ArrayList<>();

        var mutablePos = new BlockPos.MutableBlockPos();
        for (int k = 0; k <= 2; k++) {
            for (int i = -4; i <= 3; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (i == 0 && j == 0 && k == 0)
                        continue;

                    if (i == 3 && j == 0 && k == 2)
                        continue;

                    // check east and west sides (relative to facing)
                    if (i == 3 && j != 0 && k == 0) {
                        BlockPos checkPos = this.worldPosition.relative(facing, i)
                                .relative(j > 0 ? facing.getCounterClockWise() : facing.getClockWise(), 2);

                        if (isValidPosition(this.level, checkPos)) {
                            correctPositions.add(checkPos);
                        } else {
                            incorrectPositions.add(checkPos);
                        }
                    }

                    mutablePos.set(this.worldPosition.relative(facing, i).relative(facing.getCounterClockWise(), j).relative(Direction.UP, k));

                    if (isValidPosition(this.level, mutablePos)) {
                        correctPositions.add(mutablePos.immutable());
                    } else {
                        incorrectPositions.add(mutablePos.immutable());
                    }
                }
            }
        }

        for (int i = -3; i <= 2; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == -3 && j == 0)
                    continue;

                mutablePos.set(this.worldPosition.relative(facing, i).relative(facing.getCounterClockWise(), j).relative(Direction.UP, 3));
                if (isValidPosition(this.level, mutablePos)) {
                    correctPositions.add(mutablePos.immutable());
                } else {
                    incorrectPositions.add(mutablePos.immutable());
                }
            }
        }

        for (int i = -1; i <= 2; i++) {
            for (int j = 0; j <= 3; j++) {
                mutablePos.set(this.worldPosition.relative(facing, i).relative(Direction.UP, 4 + j));
                if (((i == -1 && j < 2) || (i == 2 && j == 0))) {
                    // check east and west sides (relative to facing)
                    BlockPos immutablePos = mutablePos.immutable();
                    BlockPos eastPos = immutablePos.relative(facing.getClockWise());
                    BlockPos westPos = immutablePos.relative(facing.getCounterClockWise());
                    if (isValidPosition(this.level, eastPos)) {
                        correctPositions.add(eastPos);
                    } else {
                        incorrectPositions.add(eastPos);
                    }

                    if (isValidPosition(this.level, westPos)) {
                        correctPositions.add(westPos);
                    } else {
                        incorrectPositions.add(westPos);
                    }
                }

                if (isValidPosition(this.level, mutablePos)) {
                    correctPositions.add(mutablePos.immutable());
                } else {
                    incorrectPositions.add(mutablePos.immutable());
                }
            }
        }

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j <= 4; j++) {
                mutablePos.set(this.worldPosition.relative(facing, 4 + i).relative(Direction.UP, 4 + j));

                if (i == 0 && j > 1 && j < 4) {
                    BlockPos checkPos = mutablePos.relative(facing.getOpposite());
                    if (isValidPosition(this.level, checkPos)) {
                        correctPositions.add(checkPos);
                    } else {
                        incorrectPositions.add(checkPos);
                    }
                }

                if (isValidPosition(this.level, mutablePos)) {
                    correctPositions.add(mutablePos.immutable());
                } else {
                    incorrectPositions.add(mutablePos.immutable());
                }
            }
        }

        return incorrectPositions.isEmpty() ? correctPositions : incorrectPositions;
    }

    @Override
    public List<PositionedPortRule> getPortRules() {
        return PORT_RULES;
    }

    public void removeWellhead() {
        this.wellheadPos = null;
        update();
    }

    public boolean isRunning() {
        return this.running;
    }

    public void setRunning(boolean running) {
        this.running = running;
        update();
    }

    @Override
    public WrappedContainerStorage<?> getWrappedContainerStorage() {
        return this.wrappedContainerStorage;
    }
}