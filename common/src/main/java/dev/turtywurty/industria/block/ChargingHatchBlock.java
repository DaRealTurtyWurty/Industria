package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.init.ModBlockEntityTypes;

public class ChargingHatchBlock extends IndustriaBlock {
    public ChargingHatchBlock(Properties settings) {
        super(settings, new BlockProperties()
                .blockEntityProperties(
                        new BlockProperties.BlockBlockEntityProperties<>(
                                ModBlockEntityTypes.CHARGING_HATCH)
                                .dropContentsOnBreak()
                                .rightClickToOpenGui())
                .hasHorizontalFacing());
    }
}
