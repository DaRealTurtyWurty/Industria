package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.init.BlockEntityTypeInit;

public class AlloyFurnaceBlock extends IndustriaBlock {
    public AlloyFurnaceBlock(Settings settings) {
        super(settings, new BlockProperties()
                .hasHorizontalFacing()
                .hasLitProperty()
                .hasComparatorOutput()
                .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(() -> BlockEntityTypeInit.ALLOY_FURNACE)
                        .shouldTick()
                        .rightClickToOpenGui()
                        .dropContentsOnBreak()));
    }
}
