package dev.turtywurty.industria.blockentity.util.slurry;

import dev.turtywurty.slurryapi.api.Slurry;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import dev.turtywurty.turtymultiloader.transfer.storage.TransferSupport;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.function.Predicate;

public class OutputSlurryStorage extends PredicateSlurryStorage {
    public OutputSlurryStorage(BlockEntity blockEntity, long capacity, Predicate<ResourceVariant<Slurry>> canExtract) {
        super(blockEntity, capacity, _ -> false, canExtract);
    }

    public OutputSlurryStorage(BlockEntity blockEntity, long capacity) {
        this(blockEntity, capacity, _ -> true);
    }

    @Override
    public TransferSupport support(int index) {
        return TransferSupport.EXTRACT_ONLY;
    }
}
