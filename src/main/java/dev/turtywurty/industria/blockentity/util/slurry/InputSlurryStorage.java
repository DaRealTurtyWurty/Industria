package dev.turtywurty.industria.blockentity.util.slurry;

import dev.turtywurty.fabricslurryapi.api.SlurryVariant;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.function.Predicate;

public class InputSlurryStorage extends PredicateSlurryStorage {
    public InputSlurryStorage(BlockEntity blockEntity, long capacity, Predicate<SlurryVariant> canInsert) {
        super(blockEntity, capacity, canInsert, $ -> false);
    }

    public InputSlurryStorage(BlockEntity blockEntity, long capacity) {
        this(blockEntity, capacity, $ -> true);
    }
}
