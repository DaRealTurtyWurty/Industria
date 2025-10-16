package dev.turtywurty.industria.init.worldgen;

import com.mojang.serialization.MapCodec;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.worldgen.structure.FloatingOrbStructure;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;

public class StructureTypeInit {
    public static final StructureType<FloatingOrbStructure> FLOATING_ORB = register("floating_orb", FloatingOrbStructure.CODEC);

    private static <S extends Structure> StructureType<S> register(String id, MapCodec<S> codec) {
        return Registry.register(Registries.STRUCTURE_TYPE, Industria.id(id), () -> codec);
    }

    public static void init() {}
}
