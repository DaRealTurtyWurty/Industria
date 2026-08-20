package dev.turtywurty.industria.conveyor.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
public abstract class BaseConveyorBlock extends IndustriaBlock implements ConveyorLike {
    protected BaseConveyorBlock(Properties settings, BlockProperties properties) {
        super(settings, properties);
    }
}
