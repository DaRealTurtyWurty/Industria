package dev.turtywurty.industria.init.list;

import dev.turtywurty.industria.init.worldgen.ModConfiguredFeatures;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.block.grower.TreeGrower;

public class SaplingGeneratorList {
    public static final TreeGrower RUBBER = new TreeGrower(
        "rubber",
        WeightedList.of(ModConfiguredFeatures.RUBBER_TREE),
        WeightedList.of(),
        WeightedList.of(),
        ModConfiguredFeatures.RUBBER_TREE);
}
