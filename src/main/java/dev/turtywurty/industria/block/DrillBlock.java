package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.MultiblockTypeInit;

public class DrillBlock extends IndustriaBlock {
    public DrillBlock(Settings settings) {
        super(settings, new BlockProperties()
                .hasHorizontalFacing()
                .hasComparatorOutput()
                .hasBlockEntityRenderer()
                .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(() -> BlockEntityTypeInit.DRILL)
                        .shouldTick()
                        .multiblockProperties(MultiblockTypeInit.DRILL).build()));
    }
}
