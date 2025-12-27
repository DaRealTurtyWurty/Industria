package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.abstraction.BlockEntityWithGui;
import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.SyncableTickableBlockEntity;
import dev.turtywurty.industria.blockentity.util.energy.SyncingEnergyStorage;
import dev.turtywurty.industria.blockentity.util.energy.WrappedEnergyStorage;
import dev.turtywurty.industria.blockentity.util.fluid.OutputFluidStorage;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedInventoryStorage;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedInventoryStorageHolder;
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
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

import java.util.*;

public class OilPumpJackBlockEntity extends IndustriaBlockEntity implements SyncableTickableBlockEntity, BlockEntityWithGui<BlockPosPayload>, AutoMultiblockable, WrappedInventoryStorageHolder {
    public static final Text TITLE = Industria.containerTitle("oil_pump_jack");

    private static final List<PositionedPortRule> PORT_RULES = List.of(
            PositionedPortRule.when(p -> p.z() == -4 && p.y() == 0 && p.x() >= -1 && p.x() <= 2)
                    .on(LocalDirection.BACK)
                    .types(PortType.input(TransferType.ENERGY))
                    .build()
    );

    private final WrappedEnergyStorage wrappedEnergyStorage = new WrappedEnergyStorage();
    private final WrappedInventoryStorage<?> wrappedInventoryStorage = new WrappedInventoryStorage<>();
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

    private static boolean isValidPosition(World world, BlockPos position) {
        if (world == null || position == null || !world.isPosLoaded(position))
            return false;

        BlockState state = world.getBlockState(position);
        return state.isReplaceable();
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
        if (this.world == null || this.world.isClient())
            return;

        if (this.wellheadPos == null && this.ticks++ % 20 == 0) {
            findWellhead();
        }

        if (this.wellheadPos == null)
            return;

        if (this.world.getBlockEntity(this.wellheadPos) instanceof WellheadBlockEntity wellheadBlockEntity && this.running) {
            Map<BlockPos, Integer> drillTubes = wellheadBlockEntity.getDrillTubes();

            List<BlockPos> orderedPositions = new ArrayList<>(drillTubes.keySet());
            orderedPositions.sort(Comparator.comparingInt(Vec3i::getY).reversed());

            // move the fluid in each of the drill tubes up a block
            boolean hasPumped = false;
            for (BlockPos pos : orderedPositions) {
                int amount = drillTubes.get(pos);
                if (amount <= 0)
                    continue;

                BlockPos newPos = pos.up();
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
        BlockPos wellheadPos = this.pos.offset(getCachedState().get(Properties.HORIZONTAL_FACING), 5);
        if (this.world.getBlockEntity(wellheadPos) instanceof WellheadBlockEntity wellheadBlockEntity) {
            if (!wellheadBlockEntity.hasOilPumpJack() && !wellheadBlockEntity.isRemoved()) {
                wellheadBlockEntity.setOilPumpJackPos(this.pos);
                this.wellheadPos = wellheadPos;
            }
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

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new OilPumpJackScreenHandler(syncId, playerInventory, this, this.wrappedInventoryStorage);
    }

    @Override
    protected void readData(ReadView view) {
        Multiblockable.read(this, view);
        this.wellheadPos = view.read("WellheadPos", BlockPos.CODEC).orElse(null);
        ViewUtils.readChild(view, "EnergyStorage", this.wrappedEnergyStorage);
        ViewUtils.readChild(view, "Inventory", this.wrappedInventoryStorage);
        this.running = view.getBoolean("Running", false);
    }

    @Override
    protected void writeData(WriteView view) {
        Multiblockable.write(this, view);
        if (this.wellheadPos != null) {
            view.put("WellheadPos", BlockPos.CODEC, this.wellheadPos);
        }
        ViewUtils.putChild(view, "EnergyStorage", this.wrappedEnergyStorage);
        ViewUtils.putChild(view, "Inventory", this.wrappedInventoryStorage);
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
        if (this.world == null)
            return List.of();

        List<BlockPos> correctPositions = new ArrayList<>();
        List<BlockPos> incorrectPositions = new ArrayList<>();

        var mutablePos = new BlockPos.Mutable();
        for (int k = 0; k <= 2; k++) {
            for (int i = -4; i <= 3; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (i == 0 && j == 0 && k == 0)
                        continue;

                    if (i == 3 && j == 0 && k == 2)
                        continue;

                    // check east and west sides (relative to facing)
                    if (i == 3 && j != 0 && k == 0) {
                        BlockPos checkPos = this.pos.offset(facing, i)
                                .offset(j > 0 ? facing.rotateYCounterclockwise() : facing.rotateYClockwise(), 2);

                        if (isValidPosition(this.world, checkPos)) {
                            correctPositions.add(checkPos);
                        } else {
                            incorrectPositions.add(checkPos);
                        }
                    }

                    mutablePos.set(this.pos.offset(facing, i).offset(facing.rotateYCounterclockwise(), j).offset(Direction.UP, k));

                    if (isValidPosition(this.world, mutablePos)) {
                        correctPositions.add(mutablePos.toImmutable());
                    } else {
                        incorrectPositions.add(mutablePos.toImmutable());
                    }
                }
            }
        }

        for (int i = -3; i <= 2; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == -3 && j == 0)
                    continue;

                mutablePos.set(this.pos.offset(facing, i).offset(facing.rotateYCounterclockwise(), j).offset(Direction.UP, 3));
                if (isValidPosition(this.world, mutablePos)) {
                    correctPositions.add(mutablePos.toImmutable());
                } else {
                    incorrectPositions.add(mutablePos.toImmutable());
                }
            }
        }

        for (int i = -1; i <= 2; i++) {
            for (int j = 0; j <= 3; j++) {
                mutablePos.set(this.pos.offset(facing, i).offset(Direction.UP, 4 + j));
                if (((i == -1 && j < 2) || (i == 2 && j == 0))) {
                    // check east and west sides (relative to facing)
                    BlockPos immutablePos = mutablePos.toImmutable();
                    BlockPos eastPos = immutablePos.offset(facing.rotateYClockwise());
                    BlockPos westPos = immutablePos.offset(facing.rotateYCounterclockwise());
                    if (isValidPosition(this.world, eastPos)) {
                        correctPositions.add(eastPos);
                    } else {
                        incorrectPositions.add(eastPos);
                    }

                    if (isValidPosition(this.world, westPos)) {
                        correctPositions.add(westPos);
                    } else {
                        incorrectPositions.add(westPos);
                    }
                }

                if (isValidPosition(this.world, mutablePos)) {
                    correctPositions.add(mutablePos.toImmutable());
                } else {
                    incorrectPositions.add(mutablePos.toImmutable());
                }
            }
        }

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j <= 4; j++) {
                mutablePos.set(this.pos.offset(facing, 4 + i).offset(Direction.UP, 4 + j));

                if (i == 0 && j > 1 && j < 4) {
                    BlockPos checkPos = mutablePos.offset(facing.getOpposite());
                    if (isValidPosition(this.world, checkPos)) {
                        correctPositions.add(checkPos);
                    } else {
                        incorrectPositions.add(checkPos);
                    }
                }

                if (isValidPosition(this.world, mutablePos)) {
                    correctPositions.add(mutablePos.toImmutable());
                } else {
                    incorrectPositions.add(mutablePos.toImmutable());
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
    public WrappedInventoryStorage<?> getWrappedInventoryStorage() {
        return this.wrappedInventoryStorage;
    }
}