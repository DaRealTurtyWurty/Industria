package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.RubberLeavesBlock;
import dev.turtywurty.industria.block.RubberLogBlock;
import dev.turtywurty.industria.init.list.SaplingGeneratorList;
import dev.turtywurty.turtymultiloader.registration.RegistryService;
import dev.turtywurty.turtymultiloader.registration.WoodSet;

public class ModWoodSets {
    private static final RegistryService REGISTRIES = RegistryService.get();

    public static final WoodSet RUBBER = REGISTRIES.registerWoodSet(
            Industria.id("rubber"),
            SaplingGeneratorList.RUBBER,
            builder -> builder
                    .leaves((context, properties) -> new RubberLeavesBlock(properties))
                    .log((context, properties) -> new RubberLogBlock(properties, false))
                    .strippedLog((context, properties) -> new RubberLogBlock(properties, true))
                    .wood((context, properties) -> new RubberLogBlock(properties, false))
                    .strippedWood((context, properties) -> new RubberLogBlock(properties, true))
    );

    public static void init() {
    }
}
