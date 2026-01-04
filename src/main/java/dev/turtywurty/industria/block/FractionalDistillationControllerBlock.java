package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.init.BlockEntityTypeInit;

public class FractionalDistillationControllerBlock extends IndustriaBlock {
    public FractionalDistillationControllerBlock(Properties settings) {
        super(settings, new BlockProperties()
                .hasHorizontalFacing()
                .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(() -> BlockEntityTypeInit.FRACTIONAL_DISTILLATION_CONTROLLER)
                        .shouldTick()
                        .rightClickToOpenGui()));
    }
}
