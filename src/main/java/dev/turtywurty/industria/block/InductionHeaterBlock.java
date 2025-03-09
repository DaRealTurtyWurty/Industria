package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.init.BlockEntityTypeInit;

public class InductionHeaterBlock extends IndustriaBlock {
    public InductionHeaterBlock(Settings settings) {
        super(settings, new BlockProperties()
                .hasHorizontalFacing()
                .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(() -> BlockEntityTypeInit.INDUCTION_HEATER)
                        .shouldTick()
                        .rightClickToOpenGui()));
    }
}
