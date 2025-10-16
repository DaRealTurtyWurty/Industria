package dev.turtywurty.industria.init.worldgen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.init.list.TagList;
import dev.turtywurty.industria.worldgen.structure.FloatingOrbStructure;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.math.floatprovider.ConstantFloatProvider;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.YOffset;
import net.minecraft.world.gen.heightprovider.UniformHeightProvider;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.structure.Structure;

public class StructureInit {
    public static final RegistryKey<Structure> FLOATING_ORB = registerKey("floating_orb");

    private static RegistryKey<Structure> registerKey(String name) {
        return RegistryKey.of(RegistryKeys.STRUCTURE, Industria.id(name));
    }

    public static void bootstrap(Registerable<Structure> context) {
        RegistryEntryLookup<Biome> biomeLookup = context.getRegistryLookup(RegistryKeys.BIOME);
        context.register(FLOATING_ORB, new FloatingOrbStructure(
                new Structure.Config(
                        biomeLookup.getOrThrow(TagList.Biomes.FLOATING_ORB_BIOMES)
                ),
                BlockStateProvider.of(Blocks.MOSSY_COBBLESTONE),
                BlockStateProvider.of(Blocks.COBBLESTONE),
                UniformIntProvider.create(5, 20),
                UniformHeightProvider.create(YOffset.fixed(5), YOffset.fixed(20)),
                ConstantFloatProvider.create(0.5f),
                ConstantFloatProvider.create(0.75f),
                ConstantFloatProvider.create(0.6f),
                UniformIntProvider.create(5, 50),
                BlockStateProvider.of(Blocks.VINE)
        ));
    }
}
