package dev.turtywurty.industria.blockentity.util.gas;

import dev.turtywurty.gasapi.api.GasVariant;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.function.Predicate;

public class PredicateGasStorage extends SyncingGasStorage {
    private final Predicate<GasVariant> canInsert;
    private final Predicate<GasVariant> canExtract;

    public PredicateGasStorage(BlockEntity blockEntity, long capacity, Predicate<GasVariant> canInsert, Predicate<GasVariant> canExtract) {
        super(blockEntity, capacity);
        this.canInsert = canInsert;
        this.canExtract = canExtract;
    }

    @Override
    public boolean canInsert(GasVariant variant) {
        return this.canInsert.test(variant);
    }

    @Override
    public boolean canExtract(GasVariant variant) {
        return this.canExtract.test(variant);
    }
}
