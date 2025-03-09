package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.init.BlockEntityTypeInit;

public class ThermalGeneratorBlock extends IndustriaBlock {
    public ThermalGeneratorBlock(Settings settings) {
        super(settings, new BlockProperties()
                .hasHorizontalFacing()
                .hasLitProperty()
                .hasComparatorOutput()
                .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(() -> BlockEntityTypeInit.THERMAL_GENERATOR)
                        .shouldTick()
                        .rightClickToOpenGui()
                        .dropContentsOnBreak()));
    }
}
