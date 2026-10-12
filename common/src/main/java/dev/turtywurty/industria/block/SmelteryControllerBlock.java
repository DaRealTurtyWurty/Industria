package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.init.ModBlockEntityTypes;

public class SmelteryControllerBlock extends IndustriaBlock {
    public SmelteryControllerBlock(Properties properties) {
        super(properties, new BlockProperties()
            .blockEntityProperties(
                new BlockProperties.BlockBlockEntityProperties<>(ModBlockEntityTypes.SMELTERY_CONTROLLER)
                    .shouldTick()
                    .rightClickToOpenGui())
            .hasHorizontalFacing()
            .hasLitProperty());
    }
}
