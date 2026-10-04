package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;

public class CastingTableBlock extends IndustriaBlock {
    public CastingTableBlock(Properties settings) {
        super(settings, new BlockProperties()
                .hasHorizontalFacing());
    }
}
