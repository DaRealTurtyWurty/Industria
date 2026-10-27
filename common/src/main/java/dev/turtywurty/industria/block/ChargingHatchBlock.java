package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.DoorContainerBlock;
import dev.turtywurty.industria.init.ModBlockEntityTypes;

public class ChargingHatchBlock extends DoorContainerBlock {
    public ChargingHatchBlock(Properties settings) {
        super(settings, new BlockProperties()
            .blockEntityProperties(
                new BlockProperties.BlockBlockEntityProperties<>(
                    ModBlockEntityTypes.CHARGING_HATCH)
                    .shouldTickAllowClient(true)
                    .dropContentsOnBreak()
                    .rightClickToOpenGui())
            .hasHorizontalFacing()
            .hasBlockEntityRenderer());
    }
}
