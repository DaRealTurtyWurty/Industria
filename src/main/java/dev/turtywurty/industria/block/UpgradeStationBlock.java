package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.init.BlockEntityTypeInit;

public class UpgradeStationBlock extends IndustriaBlock {
    public UpgradeStationBlock(Properties settings) {
        super(settings, new BlockProperties()
                .hasHorizontalFacing()
                .hasComparatorOutput()
                .hasBlockEntityRenderer()
                .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(() -> BlockEntityTypeInit.UPGRADE_STATION)
                        .shouldTick()
                        .rightClickToOpenGui()
                        .dropContentsOnBreak()
                ));
    }
}
