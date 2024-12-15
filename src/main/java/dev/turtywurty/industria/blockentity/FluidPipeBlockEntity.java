package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.blockentity.util.fluid.SyncingFluidStorage;
import dev.turtywurty.industria.blockentity.util.fluid.WrappedFluidStorage;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.stream.StreamSupport;

public class FluidPipeBlockEntity extends PipeBlockEntity<Storage<FluidVariant>, WrappedFluidStorage<Storage<FluidVariant>>> {
    public FluidPipeBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypeInit.FLUID_PIPE, pos, state);
        this.wrappedStorage.addStorage(new SyncingFluidStorage(this, 1_000));
    }

    @Override
    protected WrappedFluidStorage<Storage<FluidVariant>> createWrappedStorage() {
        return new WrappedFluidStorage<>();
    }

    @Override
    protected BlockApiLookup<Storage<FluidVariant>, Direction> getApiLookup() {
        return FluidStorage.SIDED;
    }

    @Override
    protected boolean supportsInsertion(Storage<FluidVariant> storage) {
        return storage.supportsInsertion();
    }

    @Override
    protected boolean isEmpty(Storage<FluidVariant> storage) {
        if (storage instanceof SingleFluidStorage singleFluidStorage)
            return singleFluidStorage.amount <= 0 || singleFluidStorage.isResourceBlank();

        for (StorageView<FluidVariant> storageView : StreamSupport.stream(storage.spliterator(), false).toList()) {
            if (storageView.getAmount() > 0 && !storageView.isResourceBlank())
                return false;
        }

        return true;
    }

    @Override
    protected void distribute(Storage<FluidVariant> thisStorage) {
        if (!(thisStorage instanceof SingleFluidStorage singleFluidStorage))
            return;

        long amount = singleFluidStorage.getAmount() / this.connectedBlocks.size();
        try (Transaction transaction = Transaction.openOuter()) {
            for (BlockPos pos : this.connectedBlocks) {
                var direction = Direction.fromVector(this.pos.getX() - pos.getX(), this.pos.getY() - pos.getY(), this.pos.getZ() - pos.getZ(), null);
                if (direction == null)
                    continue;

                var storage = FluidStorage.SIDED.find(this.world, pos, direction);
                if (storage != null && storage.supportsInsertion()) {
                    singleFluidStorage.amount -= storage.insert(singleFluidStorage.variant, amount, transaction);
                }
            }

            transaction.commit();
        }
    }
}
