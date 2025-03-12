package dev.turtywurty.industria.block;

import dev.turtywurty.fabricslurryapi.api.storage.SlurryStorage;
import dev.turtywurty.industria.blockentity.SlurryPipeBlockEntity;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.Direction;

import java.util.stream.StreamSupport;

public class SlurryPipeBlock extends PipeBlock<SlurryPipeBlockEntity> {
    public SlurryPipeBlock(Settings settings) {
        super(settings, SlurryPipeBlockEntity.class, 6);
    }

    @Override
    protected BlockApiLookup<?, Direction> getStorageLookup() {
        return SlurryStorage.SIDED;
    }

    @Override
    protected BlockEntityType<SlurryPipeBlockEntity> getBlockEntityType() {
        return BlockEntityTypeInit.SLURRY_PIPE;
    }

    @Override
    protected long getAmount(SlurryPipeBlockEntity blockEntity) {
        return StreamSupport.stream(blockEntity.getStorageProvider(null).spliterator(), false).mapToLong(StorageView::getAmount).sum();
    }

    @Override
    protected long getCapacity(SlurryPipeBlockEntity blockEntity) {
        return StreamSupport.stream(blockEntity.getStorageProvider(null).spliterator(), false).mapToLong(StorageView::getCapacity).sum();
    }

    @Override
    protected String getUnit() {
        return "mB";
    }
}
