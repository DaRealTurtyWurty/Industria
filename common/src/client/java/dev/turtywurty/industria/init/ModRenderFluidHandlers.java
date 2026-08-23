package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.turtymultiloader.client.registration.ClientRegistrations;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.world.level.material.Fluid;

import java.util.function.Supplier;

public class ModRenderFluidHandlers {
    public static void init() {
        registerFluidModel("crude_oil", ModFluids.CRUDE_OIL.still(), ModFluids.CRUDE_OIL.flowing());
        registerFluidModel("dirty_sodium_aluminate", ModFluids.DIRTY_SODIUM_ALUMINATE.still(), ModFluids.DIRTY_SODIUM_ALUMINATE.flowing());
        registerFluidModel("sodium_aluminate", ModFluids.SODIUM_ALUMINATE.still(), ModFluids.SODIUM_ALUMINATE.flowing());
        registerFluidModel("molten_aluminium", ModFluids.MOLTEN_ALUMINIUM.still(), ModFluids.MOLTEN_ALUMINIUM.flowing());
        registerFluidModel("molten_cryolite", ModFluids.MOLTEN_CRYOLITE.still(), ModFluids.MOLTEN_CRYOLITE.flowing());
        registerFluidModel("fluid_latex", ModFluids.LATEX.still(), ModFluids.LATEX.flowing());
        registerFluidModel("methanol", ModFluids.METHANOL.still(), ModFluids.METHANOL.flowing());
        registerFluidModel("formic_acid", ModFluids.FORMIC_ACID.still(), ModFluids.FORMIC_ACID.flowing());
        registerFluidModel("diluted_formic_acid", ModFluids.DILUTED_FORMIC_ACID.still(), ModFluids.DILUTED_FORMIC_ACID.flowing());
    }

    private static void registerFluidModel(String name, Supplier<? extends Fluid> still, Supplier<? extends Fluid> flowing) {
        FluidModel.Unbaked model = new FluidModel.Unbaked(
                new Material(Industria.id("block/" + name + "_still")),
                new Material(Industria.id("block/" + name + "_flow")),
                null,
                null
        );
        ClientRegistrations.registerFluidModel(model, still, flowing);
    }
}
