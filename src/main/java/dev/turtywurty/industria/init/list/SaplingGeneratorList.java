package dev.turtywurty.industria.init.list;

import dev.turtywurty.industria.init.worldgen.ConfiguredFeatureInit;
import net.minecraft.world.level.block.grower.TreeGrower;

import java.util.Optional;

public class SaplingGeneratorList {
    public static final TreeGrower RUBBER = new TreeGrower(
            "rubber",
            Optional.empty(),
            Optional.of(ConfiguredFeatureInit.RUBBER_TREE),
            Optional.empty());
}
