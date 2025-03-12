package dev.turtywurty.industria.blockentity;

import dev.turtywurty.fabricslurryapi.api.SlurryVariant;
import dev.turtywurty.fabricslurryapi.api.storage.SingleSlurryStorage;
import dev.turtywurty.fabricslurryapi.api.storage.SlurryStorage;
import dev.turtywurty.industria.blockentity.util.slurry.SyncingSlurryStorage;
import dev.turtywurty.industria.blockentity.util.slurry.WrappedSlurryStorage;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.Map;
import java.util.stream.StreamSupport;

public class SlurryPipeBlockEntity extends PipeBlockEntity<Storage<SlurryVariant>, WrappedSlurryStorage<Storage<SlurryVariant>>> {
    public SlurryPipeBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypeInit.SLURRY_PIPE, pos, state);
        this.wrappedStorage.addStorage(new SyncingSlurryStorage(this, FluidConstants.BUCKET));
    }

    @Override
    protected WrappedSlurryStorage<Storage<SlurryVariant>> createWrappedStorage() {
        return new WrappedSlurryStorage<>();
    }

    @Override
    protected BlockApiLookup<Storage<SlurryVariant>, Direction> getApiLookup() {
        return SlurryStorage.SIDED;
    }

    @Override
    protected boolean supportsInsertion(Storage<SlurryVariant> storage) {
        return storage.supportsInsertion();
    }

    @Override
    protected boolean isEmpty(Storage<SlurryVariant> storage) {
        if (storage instanceof SingleSlurryStorage singleFluidStorage)
            return singleFluidStorage.amount <= 0 || singleFluidStorage.isResourceBlank();

        for (StorageView<SlurryVariant> storageView : StreamSupport.stream(storage.spliterator(), false).toList()) {
            if (storageView.getAmount() > 0 && !storageView.isResourceBlank())
                return false;
        }

        return true;
    }

    @Override
    protected void distribute(Storage<SlurryVariant> thisStorage) {
        if (!(thisStorage instanceof SingleSlurryStorage singleSlurryStorage))
            return;

        long amount = singleSlurryStorage.getAmount() / this.connectedBlocks.size();
        try (Transaction transaction = Transaction.openOuter()) {
            for (BlockPos pos : this.connectedBlocks) {
                Map<Direction, BlockPos> connectingPipes = findConnectingPipes(this.world, pos);
                for (Map.Entry<Direction, BlockPos> entry : connectingPipes.entrySet()) {
                    Storage<SlurryVariant> storage = SlurryStorage.SIDED.find(this.world, pos, entry.getKey());
                    if (storage != null && storage.supportsInsertion()) {
                        singleSlurryStorage.amount -= storage.insert(singleSlurryStorage.variant, amount, transaction);
                    }
                }
            }

            transaction.commit();
        }
    }
}
