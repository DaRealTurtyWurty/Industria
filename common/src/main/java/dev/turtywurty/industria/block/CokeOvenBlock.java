package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.init.ModBlockEntityTypes;

public class CokeOvenBlock extends IndustriaBlock {
    public CokeOvenBlock(Properties settings) {
        super(settings, new BlockProperties()
            .hasHorizontalFacing()
            .hasBlockEntityRenderer()
            .hasLitProperty()
            .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(ModBlockEntityTypes.COKE_OVEN)
                .shouldTick()
                .dropContentsOnBreak()
                .rightClickToOpenGui()));
    }
}
