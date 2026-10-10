package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.turtymultiloader.client.registration.ClientRegistrations;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.world.level.material.Fluid;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.function.Supplier;

public class ModRenderFluidHandlers {
    private static final Map<String, Integer> MOLTEN_COLORS = Map.ofEntries(
            Map.entry("molten_aluminium", 0xFFE5EAF1),
            Map.entry("molten_silver", 0xFFF4F0D7),
            Map.entry("molten_lead", 0xFFA799BC),
            Map.entry("molten_titanium", 0xFFB9C3D6),
            Map.entry("molten_zinc", 0xFFABD4D8),
            Map.entry("molten_cobalt", 0xFF728FE0),
            Map.entry("molten_lithium", 0xFFEBA4B9),
            Map.entry("molten_nickel", 0xFFD0D393),
            Map.entry("molten_iridium", 0xFFBCD9EF),
            Map.entry("molten_silicon", 0xFF9FB4D1),
            Map.entry("molten_tin", 0xFFDCE4E5),
            Map.entry("molten_steel", 0xFFA2B4C6),
            Map.entry("molten_iron", 0xFFD8B09B),
            Map.entry("molten_copper", 0xFFE9A069),
            Map.entry("molten_gold", 0xFFF1C548),
            Map.entry("molten_netherite", 0xFFBA8876),
            Map.entry("molten_cryolite", 0xFFA2E4CF)
    );

    public static void init() {
        registerFluidModel("crude_oil", ModFluids.CRUDE_OIL.still(), ModFluids.CRUDE_OIL.flowing());
        registerFluidModel("dirty_sodium_aluminate", ModFluids.DIRTY_SODIUM_ALUMINATE.still(), ModFluids.DIRTY_SODIUM_ALUMINATE.flowing());
        registerFluidModel("sodium_aluminate", ModFluids.SODIUM_ALUMINATE.still(), ModFluids.SODIUM_ALUMINATE.flowing());
        ModFluids.getMoltenFluids().forEach((name, fluid) -> {
            int color = MOLTEN_COLORS.getOrDefault(name, 0xFFFFFFFF);
            registerFluidModel("molten_metal", fluid.still(), fluid.flowing(), state -> color);
        });
        registerFluidModel("fluid_latex", ModFluids.LATEX.still(), ModFluids.LATEX.flowing());
        registerFluidModel("methanol", ModFluids.METHANOL.still(), ModFluids.METHANOL.flowing());
        registerFluidModel("formic_acid", ModFluids.FORMIC_ACID.still(), ModFluids.FORMIC_ACID.flowing());
        registerFluidModel("diluted_formic_acid", ModFluids.DILUTED_FORMIC_ACID.still(), ModFluids.DILUTED_FORMIC_ACID.flowing());
    }

    private static void registerFluidModel(String name, Supplier<? extends Fluid> still, Supplier<? extends Fluid> flowing) {
        registerFluidModel(name, still, flowing, null);
    }

    private static void registerFluidModel(String name, Supplier<? extends Fluid> still, Supplier<? extends Fluid> flowing,
                                          @Nullable BlockTintSource tint) {
        FluidModel.Unbaked model = new FluidModel.Unbaked(
                new Material(Industria.id("block/" + name + "_still")),
                new Material(Industria.id("block/" + name + "_flow")),
                null,
                tint
        );
        ClientRegistrations.registerFluidModel(model, still, flowing);
    }
}
