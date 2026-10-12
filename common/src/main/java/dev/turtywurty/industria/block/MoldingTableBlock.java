package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.init.ModBlockEntityTypes;

public class MoldingTableBlock extends IndustriaBlock {
    public MoldingTableBlock(Properties settings) {
        super(settings, new BlockProperties()
            .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(
                ModBlockEntityTypes.MOLDING_TABLE)
                .rightClickToOpenGui()
                .dropContentsOnBreak())
            .hasHorizontalFacing());
    }
}
