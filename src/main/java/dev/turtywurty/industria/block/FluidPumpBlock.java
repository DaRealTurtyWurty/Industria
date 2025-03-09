package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.init.BlockEntityTypeInit;

public class FluidPumpBlock extends IndustriaBlock {
    public FluidPumpBlock(Settings settings) {
        super(settings, new BlockProperties()
                .hasHorizontalFacing()
                .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(() -> BlockEntityTypeInit.FLUID_PUMP)
                        .shouldTick()
                        .rightClickToOpenGui()));
    }
}
