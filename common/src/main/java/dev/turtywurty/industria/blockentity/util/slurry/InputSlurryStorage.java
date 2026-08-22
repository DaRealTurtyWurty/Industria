package dev.turtywurty.industria.blockentity.util.slurry;

import dev.turtywurty.slurryapi.api.Slurry;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import dev.turtywurty.turtymultiloader.transfer.storage.TransferSupport;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.function.Predicate;

public class InputSlurryStorage extends PredicateSlurryStorage {
    public InputSlurryStorage(BlockEntity blockEntity, long capacity, Predicate<ResourceVariant<Slurry>> canInsert) {
        super(blockEntity, capacity, canInsert, _ -> false);
    }

    public InputSlurryStorage(BlockEntity blockEntity, long capacity) {
        this(blockEntity, capacity, _ -> true);
    }

    @Override
    public TransferSupport support(int index) {
        return TransferSupport.INSERT_ONLY;
    }
}
