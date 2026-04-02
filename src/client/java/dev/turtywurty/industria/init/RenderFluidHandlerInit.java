package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderingRegistry;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.world.level.material.Fluid;

public class RenderFluidHandlerInit {
    public static void init() {
        registerFluidModel("crude_oil", FluidInit.CRUDE_OIL.still(), FluidInit.CRUDE_OIL.flowing());
        registerFluidModel("dirty_sodium_aluminate", FluidInit.DIRTY_SODIUM_ALUMINATE.still(), FluidInit.DIRTY_SODIUM_ALUMINATE.flowing());
        registerFluidModel("sodium_aluminate", FluidInit.SODIUM_ALUMINATE.still(), FluidInit.SODIUM_ALUMINATE.flowing());
        registerFluidModel("molten_aluminium", FluidInit.MOLTEN_ALUMINIUM.still(), FluidInit.MOLTEN_ALUMINIUM.flowing());
        registerFluidModel("molten_cryolite", FluidInit.MOLTEN_CRYOLITE.still(), FluidInit.MOLTEN_CRYOLITE.flowing());
    }

    private static void registerFluidModel(String name, Fluid still, Fluid flowing) {
        FluidRenderingRegistry.register(still, flowing, new FluidModel.Unbaked(
                new Material(Industria.id("block/" + name + "_still")),
                new Material(Industria.id("block/" + name + "_flow")),
                null,
                null));
    }
}
