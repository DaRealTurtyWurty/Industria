package dev.turtywurty.industria.init.worldgen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.worldgen.trunkplacer.RubberTreeTrunkPlacer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

public class TrunkPlacerTypeInit {
    public static final TrunkPlacerType<RubberTreeTrunkPlacer> RUBBER = register("rubber_tree_trunk_placer", new TrunkPlacerType<>(RubberTreeTrunkPlacer.CODEC));

    public static <T extends TrunkPlacer> TrunkPlacerType<T> register(String name, TrunkPlacerType<T> type) {
        return Registry.register(BuiltInRegistries.TRUNK_PLACER_TYPE, Industria.id(name), type);
    }

    public static void init() {}
}
