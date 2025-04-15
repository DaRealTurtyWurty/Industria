package dev.turtywurty.industria.init.list;

import dev.turtywurty.industria.init.worldgen.ConfiguredFeatureInit;
import net.minecraft.block.SaplingGenerator;

import java.util.Optional;

public class SaplingGeneratorList {
    public static final SaplingGenerator RUBBER = new SaplingGenerator(
            "rubber",
            Optional.empty(),
            Optional.of(ConfiguredFeatureInit.RUBBER_TREE),
            Optional.empty());
}
