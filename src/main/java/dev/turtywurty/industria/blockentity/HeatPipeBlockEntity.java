package dev.turtywurty.industria.blockentity;

import dev.turtywurty.heatapi.api.HeatStorage;
import dev.turtywurty.heatapi.api.base.SimpleHeatStorage;
import dev.turtywurty.industria.blockentity.util.heat.FluidHeatStorage;
import dev.turtywurty.industria.blockentity.util.heat.WrappedFluidHeatStorage;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.Map;
import java.util.stream.StreamSupport;

public class HeatPipeBlockEntity extends PipeBlockEntity<FluidHeatStorage, WrappedFluidHeatStorage> {
    public HeatPipeBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypeInit.HEAT_PIPE, pos, state);
    }

    @Override
    protected WrappedFluidHeatStorage createWrappedStorage() {
        return new WrappedFluidHeatStorage();
    }

    @Override
    protected BlockApiLookup<FluidHeatStorage, Direction> getApiLookup() {
        return FluidHeatStorage.SIDED;
    }

    @Override
    protected boolean supportsInsertion(FluidHeatStorage storage) {
        return getFluidHeatStorage().fluidStorage().supportsInsertion();
    }

    @Override
    protected boolean isEmpty(FluidHeatStorage storage) {
        if (storage.fluidStorage() instanceof SingleFluidStorage singleFluidStorage)
            return singleFluidStorage.amount <= 0 || singleFluidStorage.isResourceBlank();

        for (StorageView<FluidVariant> storageView : StreamSupport.stream(storage.fluidStorage().spliterator(), false).toList()) {
            if (storageView.getAmount() > 0 && !storageView.isResourceBlank())
                return false;
        }

        return true;
    }

    @Override
    protected void distribute(FluidHeatStorage thisStorage) {
        if (!(thisStorage.fluidStorage() instanceof SingleFluidStorage fluidStorage) || !(thisStorage.heatStorage() instanceof SimpleHeatStorage heatStorage))
            return;

        long amount = fluidStorage.amount / this.connectedBlocks.size();
        long heat = heatStorage.getAmount() / this.connectedBlocks.size();
        try (Transaction transaction = Transaction.openOuter()) {
            for (BlockPos pos : this.connectedBlocks) {
                Map<Direction, BlockPos> connectingPipes = findConnectingPipes(this.world, pos);
                for (Map.Entry<Direction, BlockPos> entry : connectingPipes.entrySet()) {
                    FluidHeatStorage storage = FluidHeatStorage.SIDED.find(this.world, pos, entry.getKey());
                    if (storage != null && storage.fluidStorage().supportsInsertion()) {
                        long fluidInsert = storage.fluidStorage().insert(fluidStorage.variant, amount, transaction);
                        fluidStorage.amount -= fluidInsert;
                        amount += (amount - fluidInsert > 0) ? amount - fluidInsert / this.connectedBlocks.size() : 0;

                        long heatInsert = storage.heatStorage().insert(heat, transaction);
                        heatStorage.amount -= heatInsert;
                        heat += (heat - heatInsert > 0) ? heat - heatInsert / this.connectedBlocks.size() : 0;
                    }
                }
            }

            transaction.commit();
        }
    }

    public FluidHeatStorage getFluidHeatStorage() {
        return this.wrappedStorage.getStorage(null);
    }

    public HeatStorage getHeatStorage() {
        return getFluidHeatStorage().heatStorage();
    }
}
