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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FluidTankBlockEntity extends IndustriaBlockEntity implements SyncableTickableBlockEntity, BlockEntityWithGui<BlockPosPayload> {
    public static final Component TITLE = Industria.containerTitle("fluid_tank");

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
        if (this.level == null || this.level.isClientSide())
            return;

        if (this.isExtractMode) {
            SyncingFluidStorage tank = getFluidTank();
            if (tank.isResourceBlank() || tank.amount <= 0)
                return;

            Map<Storage<FluidVariant>, Long> storages = new HashMap<>();
            for (Direction direction : Direction.values()) {
                Storage<FluidVariant> fluidStorage = FluidStorage.SIDED.find(this.level, this.worldPosition.relative(direction), direction.getOpposite());
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
    protected void saveAdditional(ValueOutput view) {
        view.putBoolean("ExtractMode", this.isExtractMode);
        ViewUtils.putChild(view, "FluidStorage", this.wrappedFluidStorage);
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        this.isExtractMode = view.getBooleanOr("ExtractMode", false);
        ViewUtils.readChild(view, "FluidStorage", this.wrappedFluidStorage);
    }

    public SingleFluidStorage getFluidProvider(Direction direction) {
        return this.wrappedFluidStorage.getStorage(direction);
    }

    public SyncingFluidStorage getFluidTank() {
        return (SyncingFluidStorage) getFluidProvider(null);
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
        return new FluidTankScreenHandler(syncId, playerInventory, this);
    }
}