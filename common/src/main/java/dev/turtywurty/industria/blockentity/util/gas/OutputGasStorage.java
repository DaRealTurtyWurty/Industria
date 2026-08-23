package dev.turtywurty.industria.blockentity.util.gas;

import dev.turtywurty.gasapi.api.Gas;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import dev.turtywurty.turtymultiloader.transfer.storage.TransferSupport;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.function.Predicate;

public class OutputGasStorage extends PredicateGasStorage {
    public OutputGasStorage(BlockEntity blockEntity, long capacity, Predicate<ResourceVariant<Gas>> canExtract) {
        super(blockEntity, capacity, _ -> false, canExtract);
    }

    public OutputGasStorage(BlockEntity blockEntity, long capacity) {
        this(blockEntity, capacity, _ -> true);
    }

    @Override
    public TransferSupport support(int index) {
        return TransferSupport.EXTRACT_ONLY;
    }
}
