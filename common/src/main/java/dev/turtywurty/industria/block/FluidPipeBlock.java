package dev.turtywurty.industria.block;

import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.pipe.impl.network.FluidPipeNetwork;
import dev.turtywurty.industria.pipe.PipeStorageUtil;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import dev.turtywurty.turtymultiloader.transfer.storage.ResourceStorage;
import net.minecraft.world.level.material.Fluid;

public class FluidPipeBlock extends PipeBlock<ResourceVariant<Fluid>, FluidPipeNetwork> {
    public FluidPipeBlock(Properties settings) {
        super(settings, FLUID_PIPE_SHAPE, TransferType.FLUID);
    }

    @Override
    public long getAmount(ResourceStorage<ResourceVariant<Fluid>> storage) {
        return PipeStorageUtil.amount(storage);
    }

    @Override
    public long getCapacity(ResourceStorage<ResourceVariant<Fluid>> storage) {
        return PipeStorageUtil.capacity(storage);
    }

    @Override
    public String getUnit() {
        return "mB";
    }
}
