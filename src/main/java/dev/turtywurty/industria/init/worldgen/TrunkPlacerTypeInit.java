package dev.turtywurty.industria.init.worldgen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.worldgen.trunkplacer.RubberTreeTrunkPlacer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.world.gen.trunk.TrunkPlacer;
import net.minecraft.world.gen.trunk.TrunkPlacerType;

public class TrunkPlacerTypeInit {
    public static final TrunkPlacerType<RubberTreeTrunkPlacer> RUBBER = register("rubber_tree_trunk_placer", new TrunkPlacerType<>(RubberTreeTrunkPlacer.CODEC));

    public static <T extends TrunkPlacer> TrunkPlacerType<T> register(String name, TrunkPlacerType<T> type) {
        return Registry.register(Registries.TRUNK_PLACER_TYPE, Industria.id(name), type);
    }

    public static void init() {
    }
}
