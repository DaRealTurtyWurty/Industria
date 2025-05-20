package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.init.BlockEntityTypeInit;

public class WellheadBlock extends IndustriaBlock {
    public WellheadBlock(Settings settings) {
        super(settings, new BlockProperties()
                .hasBlockEntityRenderer()
                .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(() -> BlockEntityTypeInit.WELLHEAD)
                        .shouldTick()));
    }
}
