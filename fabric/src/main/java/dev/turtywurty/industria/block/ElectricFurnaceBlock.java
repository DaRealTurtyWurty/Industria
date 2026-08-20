package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.init.BlockEntityTypeInit;

public class ElectricFurnaceBlock extends IndustriaBlock {
    public ElectricFurnaceBlock(Properties settings) {
        super(settings, new BlockProperties()
                .hasHorizontalFacing()
                .hasLitProperty()
                .hasComparatorOutput()
                .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(() -> BlockEntityTypeInit.ELECTRIC_FURNACE)
                        .shouldTick()
                        .dropContentsOnBreak()
                        .rightClickToOpenGui()));
    }
}
