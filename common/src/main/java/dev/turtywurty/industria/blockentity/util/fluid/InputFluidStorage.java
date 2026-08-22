package dev.turtywurty.industria.blockentity.util.fluid;

import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import dev.turtywurty.turtymultiloader.transfer.storage.TransferSupport;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;

import java.util.function.Predicate;

public class InputFluidStorage extends PredicateFluidStorage {
    public InputFluidStorage(BlockEntity blockEntity, long capacity, Predicate<ResourceVariant<Fluid>> canInsert) {
        super(blockEntity, capacity, canInsert, variant -> false);
    }

    public InputFluidStorage(BlockEntity blockEntity, long capacity) {
        this(blockEntity, capacity, _ -> true);
    }

    @Override
    public TransferSupport support(int index) {
        return TransferSupport.INSERT_ONLY;
    }
}
