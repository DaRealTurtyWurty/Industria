package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.DoorContainerBlock;
import dev.turtywurty.industria.init.ModBlockEntityTypes;

public class FuelboxBlock extends DoorContainerBlock {
    public FuelboxBlock(Properties settings) {
        super(settings, new BlockProperties()
            .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(
                ModBlockEntityTypes.FUELBOX)
                .dropContentsOnBreak()
                .rightClickToOpenGui()
                .shouldTickAllowClient(true))
            .hasHorizontalFacing()
            .hasBlockEntityRenderer()
            .hasLitProperty());
    }
}
