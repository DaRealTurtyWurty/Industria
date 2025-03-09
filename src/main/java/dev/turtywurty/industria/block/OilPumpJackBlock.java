package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.MultiblockTypeInit;

public class OilPumpJackBlock extends IndustriaBlock {
    public OilPumpJackBlock(Settings settings) {
        super(settings, new BlockProperties()
                .hasHorizontalFacing()
                .hasBlockEntityRenderer()
                .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(() -> BlockEntityTypeInit.OIL_PUMP_JACK)
                        .shouldTick()
                        .rightClickToOpenGui()
                        .multiblockProperties(MultiblockTypeInit.OIL_PUMP_JACK).build()));
    }
}
