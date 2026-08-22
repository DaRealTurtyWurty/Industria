package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.init.ModBlockEntityTypes;

public class UpgradeStationBlock extends IndustriaBlock {
    public UpgradeStationBlock(Properties settings) {
        super(settings, new BlockProperties()
                .hasHorizontalFacing()
                .hasComparatorOutput()
                .hasBlockEntityRenderer()
                .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(ModBlockEntityTypes.UPGRADE_STATION)
                        .shouldTick()
                        .rightClickToOpenGui()
                        .dropContentsOnBreak()
                ));
    }
}
