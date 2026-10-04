package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.metal.MetalDefinition;
import dev.turtywurty.industria.metal.MetalForm;
import dev.turtywurty.industria.metal.MetalRegistry;

import java.util.Map;

public final class ModMetals {
    private ModMetals() {
    }

    public static final MetalDefinition ALUMINIUM = MetalRegistry.register(new MetalDefinition(
            Industria.id("aluminium"),
            100f,
            ModBlocks.ALUMINIUM_BLOCK,
            ModFluids.MOLTEN_ALUMINIUM,
            Map.of(
                    MetalForm.INGOT, ModItems.ALUMINIUM_INGOT,
                    MetalForm.NUGGET, ModItems.ALUMINIUM_NUGGET,
                    MetalForm.PLATE, ModItems.ALUMINIUM_PLATE,
                    MetalForm.POWDER, ModItems.ALUMINIUM_POWDER
            )
    ));

    public static void init() {
    }
}
