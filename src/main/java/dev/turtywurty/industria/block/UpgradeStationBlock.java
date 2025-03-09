package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.MultiblockTypeInit;

public class UpgradeStationBlock extends IndustriaBlock {
    public UpgradeStationBlock(Settings settings) {
        super(settings, new BlockProperties()
                .hasHorizontalFacing()
                .hasComparatorOutput()
                .hasBlockEntityRenderer()
                .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(() -> BlockEntityTypeInit.UPGRADE_STATION)
                        .shouldTick()
                        .rightClickToOpenGui()
                        .dropContentsOnBreak()
                        .multiblockProperties(MultiblockTypeInit.UPGRADE_STATION).build()));
    }
}
