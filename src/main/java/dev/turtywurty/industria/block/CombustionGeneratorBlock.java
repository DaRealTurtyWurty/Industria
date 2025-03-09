package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.init.BlockEntityTypeInit;

public class CombustionGeneratorBlock extends IndustriaBlock {
    public CombustionGeneratorBlock(Settings settings) {
        super(settings, new BlockProperties()
                .hasHorizontalFacing()
                .hasLitProperty()
                .hasComparatorOutput()
                .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(() -> BlockEntityTypeInit.COMBUSTION_GENERATOR)
                        .shouldTick()
                        .rightClickToOpenGui()
                        .dropContentsOnBreak()));
    }
}
