package dev.turtywurty.industria.init.worldgen;

import dev.turtywurty.industria.Industria;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.dimension.DimensionType;

import java.util.Optional;
import java.util.OptionalLong;

public class DimensionTypeInit {
    public static final RegistryKey<DimensionType> THE_LUMEN_DEPTHS = registerKey("the_lumen_depths");

    public static void bootstrap(Registerable<DimensionType> context) {
        context.register(THE_LUMEN_DEPTHS, new DimensionType(
                OptionalLong.empty(),
                true,
                false,
                false,
                true,
                1.0,
                true,
                false,
                0,
                256,
                256,
                BlockTags.INFINIBURN_OVERWORLD, // TODO: Replace with custom blocks tag
                Industria.id("the_lumen_depths"),
                0.5f,
                Optional.empty(),
                new DimensionType.MonsterSettings(
                        false,
                        false,
                        UniformIntProvider.create(0, 7),
                        0
                )
        ));
    }

    private static RegistryKey<DimensionType> registerKey(String name) {
        return RegistryKey.of(RegistryKeys.DIMENSION_TYPE, Industria.id(name));
    }
}
