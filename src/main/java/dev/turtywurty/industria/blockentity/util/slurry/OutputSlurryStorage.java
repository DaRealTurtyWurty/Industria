package dev.turtywurty.industria.blockentity.util.slurry;

import dev.turtywurty.fabricslurryapi.api.SlurryVariant;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.function.Predicate;

public class OutputSlurryStorage extends PredicateSlurryStorage {
    public OutputSlurryStorage(BlockEntity blockEntity, long capacity, Predicate<SlurryVariant> canExtract) {
        super(blockEntity, capacity, $ -> false, canExtract);
    }

    public OutputSlurryStorage(BlockEntity blockEntity, long capacity) {
        this(blockEntity, capacity, $ -> true);
    }
}
