package dev.turtywurty.industria.block;

import dev.turtywurty.industria.blockentity.FluidPipeBlockEntity;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.Direction;

import java.util.stream.StreamSupport;

public class FluidPipeBlock extends PipeBlock<FluidPipeBlockEntity> {
    public FluidPipeBlock(Settings settings) {
        super(settings, FluidPipeBlockEntity.class, 6);
    }

    @Override
    protected BlockApiLookup<?, Direction> getStorageLookup() {
        return FluidStorage.SIDED;
    }

    @Override
    protected BlockEntityType<FluidPipeBlockEntity> getBlockEntityType() {
        return BlockEntityTypeInit.FLUID_PIPE;
    }

    @Override
    protected long getAmount(FluidPipeBlockEntity blockEntity) {
        return StreamSupport.stream(blockEntity.getStorageProvider(null).spliterator(), false).mapToLong(StorageView::getAmount).sum();
    }

    @Override
    protected long getCapacity(FluidPipeBlockEntity blockEntity) {
        return StreamSupport.stream(blockEntity.getStorageProvider(null).spliterator(), false).mapToLong(StorageView::getCapacity).sum();
    }

    @Override
    protected String getUnit() {
        return "mB";
    }
}
