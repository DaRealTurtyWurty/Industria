package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.blockentity.util.energy.SyncingEnergyStorage;
import dev.turtywurty.industria.blockentity.util.energy.WrappedEnergyStorage;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyStorage;

public class CableBlockEntity extends PipeBlockEntity<EnergyStorage, WrappedEnergyStorage> {
    public CableBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypeInit.CABLE, pos, state);
        this.wrappedStorage.addStorage(new SyncingEnergyStorage(this, 1_000, 1_000, 0));
    }

    @Override
    protected WrappedEnergyStorage createWrappedStorage() {
        return new WrappedEnergyStorage();
    }

    @Override
    protected BlockApiLookup<EnergyStorage, Direction> getApiLookup() {
        return EnergyStorage.SIDED;
    }

    @Override
    protected boolean supportsInsertion(EnergyStorage storage) {
        return storage.supportsInsertion();
    }

    @Override
    protected boolean isEmpty(EnergyStorage storage) {
        return storage.getAmount() <= 0;
    }

    @Override
    protected void distribute(EnergyStorage thisStorage) {
        if(!(thisStorage instanceof SimpleEnergyStorage simpleEnergyStorage))
            return;

        long amount = simpleEnergyStorage.getAmount() / this.connectedBlocks.size();
        try (Transaction transaction = Transaction.openOuter()) {
            for (BlockPos pos : this.connectedBlocks) {
                var direction = Direction.fromVector(this.pos.getX() - pos.getX(), this.pos.getY() - pos.getY(), this.pos.getZ() - pos.getZ(), null);
                if(direction == null)
                    continue;

                var storage = EnergyStorage.SIDED.find(this.world, pos, direction);
                if (storage != null && storage.supportsInsertion()) {
                    simpleEnergyStorage.amount -= storage.insert(amount, transaction);
                }
            }

            transaction.commit();
        }
    }
}
