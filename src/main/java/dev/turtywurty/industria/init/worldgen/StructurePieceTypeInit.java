package dev.turtywurty.industria.init.worldgen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.worldgen.feature.FloatingOrbStructure;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.structure.StructurePieceType;

public final class StructurePieceTypeInit {
    public static final StructurePieceType FLOATING_ORB = register("floating_orb", FloatingOrbStructure.Piece::new);

    private StructurePieceTypeInit() {}

    private static StructurePieceType register(String id, StructurePieceType.Simple factory) {
        return Registry.register(Registries.STRUCTURE_PIECE, Industria.id(id), factory);
    }

    public static void init() {}
}
