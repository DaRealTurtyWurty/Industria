package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.init.ModBlockEntityTypes;

public class InductionHeaterBlock extends IndustriaBlock {
    public InductionHeaterBlock(Properties settings) {
        super(settings, new BlockProperties()
                .hasHorizontalFacing()
                .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(ModBlockEntityTypes.INDUCTION_HEATER)
                        .shouldTick()
                        .rightClickToOpenGui()));
    }
}
