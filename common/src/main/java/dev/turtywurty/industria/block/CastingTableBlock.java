package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.init.ModBlockEntityTypes;

public class CastingTableBlock extends IndustriaBlock {
    public CastingTableBlock(Properties settings) {
        super(settings, new BlockProperties()
            .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(
                ModBlockEntityTypes.CASTING_TABLE)
                .dropContentsOnBreak()
                .rightClickToOpenGui()
                .shouldTick())
            .hasHorizontalFacing());
    }
}
