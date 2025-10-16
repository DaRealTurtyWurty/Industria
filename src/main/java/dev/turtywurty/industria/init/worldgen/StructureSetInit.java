package dev.turtywurty.industria.init.worldgen;

import dev.turtywurty.industria.Industria;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.structure.StructureSet;
import net.minecraft.world.gen.chunk.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.gen.chunk.placement.SpreadType;
import net.minecraft.world.gen.structure.Structure;

import java.util.List;

public class StructureSetInit {
    public static final RegistryKey<StructureSet> FLOATING_ORB_SET = registerKey("floating_orb");

    private static RegistryKey<StructureSet> registerKey(String name) {
        return RegistryKey.of(RegistryKeys.STRUCTURE_SET, Industria.id(name));
    }

    public static void bootstrap(Registerable<StructureSet> context) {
        RegistryEntryLookup<Structure> structureLookup = context.getRegistryLookup(RegistryKeys.STRUCTURE);
        context.register(FLOATING_ORB_SET, new StructureSet(
                List.of(
                        StructureSet.createEntry(
                                structureLookup.getOrThrow(StructureInit.FLOATING_ORB),
                                1
                        )
                ),
                new RandomSpreadStructurePlacement(
                        3,
                        2,
                        SpreadType.LINEAR,
                        16549321
                )
        ));
    }
}
