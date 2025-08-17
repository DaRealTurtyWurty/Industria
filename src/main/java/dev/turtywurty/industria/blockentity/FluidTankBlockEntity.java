package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.abstraction.BlockEntityWithGui;
import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.SyncableTickableBlockEntity;
import dev.turtywurty.industria.blockentity.util.fluid.PredicateFluidStorage;
import dev.turtywurty.industria.blockentity.util.fluid.SyncingFluidStorage;
import dev.turtywurty.industria.blockentity.util.fluid.WrappedFluidStorage;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.screenhandler.FluidTankScreenHandler;
import dev.turtywurty.industria.util.ViewUtils;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FluidTankBlockEntity extends IndustriaBlockEntity implements SyncableTickableBlockEntity, BlockEntityWithGui<BlockPosPayload> {
    public static final Text TITLE = Industria.containerTitle("fluid_tank");

    private final WrappedFluidStorage<SingleFluidStorage> wrappedFluidStorage = new WrappedFluidStorage<>();
    private boolean isExtractMode = false;

    public FluidTankBlockEntity(BlockPos pos, BlockState state) {
        super(BlockInit.FLUID_TANK, BlockEntityTypeInit.FLUID_TANK, pos, state);

        this.wrappedFluidStorage.addStorage(new PredicateFluidStorage(
                this,
                FluidConstants.BUCKET * 16,
                variant -> !isExtractMode,
                variant -> isExtractMode));
    }

    @Override
    public List<SyncableStorage> getSyncableStorages() {
        return Collections.singletonList(getFluidTank());
    }

    @Override
    public void onTick() {
        if (this.world == null || this.world.isClient)
            return;

        if (this.isExtractMode) {
            SyncingFluidStorage tank = getFluidTank();
            if (tank.isResourceBlank() || tank.amount <= 0)
                return;

            Map<Storage<FluidVariant>, Long> storages = new HashMap<>();
            for (Direction direction : Direction.values()) {
                Storage<FluidVariant> fluidStorage = FluidStorage.SIDED.find(this.world, this.pos.offset(direction), direction.getOpposite());
                if (fluidStorage == null || !fluidStorage.supportsInsertion())
                    continue;

                long maxInsert;
                try (Transaction transaction = Transaction.openOuter()) {
                    maxInsert = fluidStorage.insert(tank.variant, tank.amount, transaction);
                }

                if (maxInsert > 0) {
                    storages.put(fluidStorage, maxInsert);
                }
            }

            int size = storages.size();
            long totalCanTransfer = Math.min(tank.amount, storages.values().stream().mapToLong(Number::longValue).sum());
            for (Map.Entry<Storage<FluidVariant>, Long> entry : storages.entrySet()) {
                Storage<FluidVariant> storage = entry.getKey();
                long maxAmount = entry.getValue();

                long amountToTransfer = Math.min(totalCanTransfer / size, maxAmount);
                if (amountToTransfer <= 0)
                    continue;

                try (Transaction transaction = Transaction.openOuter()) {
                    long transferred = storage.insert(tank.variant, amountToTransfer, transaction);
                    if (transferred > 0) {
                        tank.amount -= transferred;
                        transaction.commit();
                        update();
                    }
                }
            }
        }
    }

    public boolean isExtractMode() {
        return this.isExtractMode;
    }

    public void setExtractMode(boolean extractMode) {
        if (this.isExtractMode != extractMode) {
            this.isExtractMode = extractMode;
            update();
        }
    }

    @Override
    protected void writeData(WriteView view) {
        view.putBoolean("ExtractMode", this.isExtractMode);
        ViewUtils.putChild(view, "FluidStorage", this.wrappedFluidStorage);
    }

    @Override
    protected void readData(ReadView view) {
        this.isExtractMode = view.getBoolean("ExtractMode", false);
        ViewUtils.readChild(view, "FluidStorage", this.wrappedFluidStorage);
    }

    public SingleFluidStorage getFluidProvider(Direction direction) {
        return this.wrappedFluidStorage.getStorage(direction);
    }

    public SyncingFluidStorage getFluidTank() {
        return (SyncingFluidStorage) getFluidProvider(null);
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
        return new FluidTankScreenHandler(syncId, playerInventory, this);
    }
}