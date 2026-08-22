package dev.turtywurty.industria.init.worldgen;

import com.mojang.serialization.MapCodec;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.worldgen.trunkplacer.RubberTreeTrunkPlacer;
import dev.turtywurty.turtymultiloader.registration.RegistrationHandle;
import dev.turtywurty.turtymultiloader.registration.RegistryService;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

public class ModTrunkPlacerTypes {
    private static final RegistryService REGISTRIES = RegistryService.get();

    public static final RegistrationHandle<TrunkPlacerType<?>, TrunkPlacerType<RubberTreeTrunkPlacer>> RUBBER =
            register("rubber_tree_trunk_placer", RubberTreeTrunkPlacer.CODEC);

    public static <T extends TrunkPlacer> RegistrationHandle<TrunkPlacerType<?>, TrunkPlacerType<T>> register(String name, MapCodec<T> codec) {
        return REGISTRIES.registerTrunkPlacerType(Industria.id(name), codec);
    }

    public static void init() {
    }
}
