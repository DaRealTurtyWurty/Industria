package dev.turtywurty.industria.blockentity.util.slurry;

import dev.turtywurty.fabricslurryapi.api.SlurryVariant;
import net.minecraft.block.entity.BlockEntity;

import java.util.function.Predicate;

public class PredicateSlurryStorage extends SyncingSlurryStorage {
    private final Predicate<SlurryVariant> canInsert;
    private final Predicate<SlurryVariant> canExtract;

    public PredicateSlurryStorage(BlockEntity blockEntity, long capacity, Predicate<SlurryVariant> canInsert, Predicate<SlurryVariant> canExtract) {
        super(blockEntity, capacity);
        this.canInsert = canInsert;
        this.canExtract = canExtract;
    }

    @Override
    public boolean canInsert(SlurryVariant variant) {
        return this.canInsert.test(variant);
    }

    @Override
    public boolean canExtract(SlurryVariant variant) {
        return this.canExtract.test(variant);
    }
}
