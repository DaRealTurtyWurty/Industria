package dev.turtywurty.industria.init.list;

import dev.turtywurty.industria.init.worldgen.ModConfiguredFeatures;
import net.minecraft.world.level.block.grower.TreeGrower;

import java.util.Optional;

public class SaplingGeneratorList {
    public static final TreeGrower RUBBER = new TreeGrower(
            "rubber",
            Optional.empty(),
            Optional.of(ModConfiguredFeatures.RUBBER_TREE),
            Optional.empty());
}
