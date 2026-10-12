package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.init.ModBlockEntityTypes;

public class FuelboxBlock extends IndustriaBlock {
    public FuelboxBlock(Properties settings) {
        super(settings, new BlockProperties()
            .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(
                ModBlockEntityTypes.FUELBOX)
                .dropContentsOnBreak()
                .rightClickToOpenGui()
                .shouldTick())
            .hasHorizontalFacing()
            .hasLitProperty());
    }
}
