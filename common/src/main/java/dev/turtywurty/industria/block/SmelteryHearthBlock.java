package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;

public class SmelteryHearthBlock extends IndustriaBlock {
    public SmelteryHearthBlock(Properties properties) {
        super(properties, new BlockProperties().hasLitProperty());
    }
}
