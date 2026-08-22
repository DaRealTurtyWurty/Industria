package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.init.ModBlockEntityTypes;

public class OilPumpJackBlock extends IndustriaBlock {
    public OilPumpJackBlock(Properties settings) {
        super(settings, new BlockProperties()
                .hasHorizontalFacing()
                .hasBlockEntityRenderer()
                .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(ModBlockEntityTypes.OIL_PUMP_JACK)
                        .shouldTick()
                        .rightClickToOpenGui()
                ));
    }
}
