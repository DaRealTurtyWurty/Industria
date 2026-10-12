package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.init.ModBlockEntityTypes;

public class FireboxBlock extends IndustriaBlock {
    public FireboxBlock(Properties settings) {
        super(settings, new BlockProperties()
            .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(
                ModBlockEntityTypes.FIREBOX)
                .dropContentsOnBreak()
                .rightClickToOpenGui()
                .shouldTick())
            .hasHorizontalFacing()
            .hasLitProperty());
    }
}
