package dev.turtywurty.industria.init;

import dev.turtywurty.industria.block.RubberLeavesBlock;
import dev.turtywurty.industria.block.RubberLogBlock;
import dev.turtywurty.industria.init.list.SaplingGeneratorList;
import dev.turtywurty.industria.util.WoodRegistrySet;

public class WoodSetInit {
    public static final WoodRegistrySet RUBBER = new WoodRegistrySet.Builder("rubber", SaplingGeneratorList.RUBBER)
            .leaves(RubberLeavesBlock::new)
            .log(settings -> new RubberLogBlock(settings, false), settings -> new RubberLogBlock(settings, true))
            .wood(settings -> new RubberLogBlock(settings, false), settings -> new RubberLogBlock(settings, true))
            .build();

    public static void init() {
    }
}
