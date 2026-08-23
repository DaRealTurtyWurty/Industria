package dev.turtywurty.industria.blockentity.util.gas;

import dev.turtywurty.gasapi.api.Gas;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import dev.turtywurty.turtymultiloader.transfer.storage.TransferSupport;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.function.Predicate;

public class InputGasStorage extends PredicateGasStorage {
    public InputGasStorage(BlockEntity blockEntity, long capacity, Predicate<ResourceVariant<Gas>> canInsert) {
        super(blockEntity, capacity, canInsert, $ -> false);
    }

    public InputGasStorage(BlockEntity blockEntity, long capacity) {
        this(blockEntity, capacity, _ -> true);
    }

    @Override
    public TransferSupport support(int index) {
        return TransferSupport.INSERT_ONLY;
    }
}
