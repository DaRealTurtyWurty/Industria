package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.abstraction.BlockEntityWithGui;
import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.SyncableTickableBlockEntity;
import dev.turtywurty.industria.blockentity.util.UpdatableBlockEntity;
import dev.turtywurty.industria.blockentity.util.energy.SyncingEnergyStorage;
import dev.turtywurty.industria.blockentity.util.energy.WrappedEnergyStorage;
import dev.turtywurty.industria.blockentity.util.fluid.SyncingFluidStorage;
import dev.turtywurty.industria.blockentity.util.fluid.WrappedFluidStorage;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.screenhandler.FluidPumpScreenHandler;
import dev.turtywurty.industria.util.MathUtils;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyStorage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FluidPumpBlockEntity extends UpdatableBlockEntity implements SyncableTickableBlockEntity, BlockEntityWithGui<BlockPosPayload> {
    public static final Text TITLE = Industria.containerTitle("fluid_pump");

    private static final Direction[] CHECK_DIRECTIONS = {Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.DOWN};

    private final WrappedFluidStorage<SingleFluidStorage> wrappedFluidStorage = new WrappedFluidStorage<>();
    private final WrappedEnergyStorage wrappedEnergyStorage = new WrappedEnergyStorage();

    public FluidPumpBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypeInit.FLUID_PUMP, pos, state);
        this.wrappedFluidStorage.addStorage(new SyncingFluidStorage(this, FluidConstants.BUCKET * 10), Direction.SOUTH);
        this.wrappedEnergyStorage.addStorage(new SyncingEnergyStorage(this, 50_000, 1_000, 0), Direction.UP);
    }

    @Override
    public List<SyncableStorage> getSyncableStorages() {
        return List.of((SyncableStorage) this.wrappedFluidStorage.getStorage(0), (SyncableStorage) this.wrappedEnergyStorage.getStorage(0));
    }

    @Override
    public void onTick() {
        if (this.world == null || this.world.isClient)
            return;

        SingleFluidStorage fluidStorage = this.wrappedFluidStorage.getStorage(Direction.SOUTH);
        if (!isEmpty(fluidStorage)) { // TODO: Fix this
            Direction relativeSouth = MathUtils.getRelativeDirection(Direction.SOUTH, getCachedState().get(Properties.HORIZONTAL_FACING));
            BlockPos southPos = this.pos.offset(relativeSouth);
            Storage<FluidVariant> storage = FluidStorage.SIDED.find(this.world, southPos, relativeSouth.getOpposite());
            if (storage != null) {
                try (Transaction transaction = Transaction.openOuter()) {
                    long inserted = storage.insert(fluidStorage.variant, fluidStorage.amount, transaction);
                    if (inserted > 0) {
                        fluidStorage.amount -= inserted;
                        update();
                    }

                    transaction.commit();
                }
            }
        }

        // check surrounding blocks for fluid
        if (this.world.getTime() % 10 == 0) {
            SimpleEnergyStorage energyStorage = (SimpleEnergyStorage) this.wrappedEnergyStorage.getStorage(Direction.UP);
            if (energyStorage.getAmount() <= 10)
                return;

            Direction direction = getCachedState().get(Properties.HORIZONTAL_FACING);
            Map<Direction, FluidState> fluidStateMap = new HashMap<>();
            for (Direction checkDirection : CHECK_DIRECTIONS) {
                Direction relative = MathUtils.getRelativeDirection(checkDirection, direction);
                BlockPos checkPos = this.pos.offset(relative);
                FluidState fluidState = this.world.getFluidState(checkPos);
                if (fluidState.isEmpty())
                    break;

                fluidStateMap.put(relative, fluidState);
            }

            long storedFluidAmount = fluidStorage.amount;
            if (storedFluidAmount >= fluidStorage.getCapacity())
                return;

            if (!fluidStateMap.isEmpty()) {
                // find either a fluid that we can insert (assuming we're not empty) or the fluid that is the most common
                if (isEmpty(fluidStorage)) {
                    FluidState mostCommon = getMostCommon(fluidStateMap);

                    if (mostCommon != null) {
                        fluidStorage.variant = FluidVariant.of(mostCommon.getFluid());
                        fluidStorage.amount = Math.min(fluidStorage.getCapacity(), fluidStorage.amount + FluidConstants.BOTTLE);
                    }
                } else {
                    for (FluidState state : fluidStateMap.values()) {
                        if (state.getFluid() == fluidStorage.variant.getFluid()) {
                            fluidStorage.amount = Math.min(fluidStorage.getCapacity(), fluidStorage.amount + FluidConstants.BOTTLE);
                            break;
                        }
                    }
                }
            }

            if (storedFluidAmount != fluidStorage.amount) {
                energyStorage.amount -= 10;
                update();
            }
        }
    }

    private static @Nullable FluidState getMostCommon(Map<Direction, FluidState> fluidStateMap) {
        FluidState mostCommon = null;
        int mostCommonCount = 0;

        for (FluidState state : fluidStateMap.values()) {
            int count = 0;
            for (FluidState value : fluidStateMap.values()) {
                if (value.getFluid() == state.getFluid())
                    count++;
            }

            if (count > mostCommonCount) {
                mostCommon = state;
                mostCommonCount = count;
            }
        }

        return mostCommon;
    }

    private static boolean isEmpty(SingleFluidStorage storage) {
        return storage.amount <= 0 || storage.isResourceBlank();
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
        return new FluidPumpScreenHandler(syncId, this);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        nbt.put("FluidTank", this.wrappedFluidStorage.writeNbt(registries));
        nbt.put("Energy", this.wrappedEnergyStorage.writeNbt(registries));
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        if (nbt.contains("FluidTank", NbtElement.LIST_TYPE))
            this.wrappedFluidStorage.readNbt(nbt.getList("FluidTank", NbtElement.COMPOUND_TYPE), registries);
        if (nbt.contains("Energy", NbtElement.LIST_TYPE))
            this.wrappedEnergyStorage.readNbt(nbt.getList("Energy", NbtElement.COMPOUND_TYPE), registries);
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

    public SingleFluidStorage getFluidProvider(Direction side) {
        return this.wrappedFluidStorage.getStorage(side);
    }

    public EnergyStorage getEnergyProvider(Direction side) {
        return this.wrappedEnergyStorage.getStorage(side);
    }

    public EnergyStorage getEnergyStorage() {
        return getEnergyProvider(Direction.UP);
    }

    public SingleFluidStorage getFluidTank() {
        return getFluidProvider(Direction.SOUTH);
    }
}
