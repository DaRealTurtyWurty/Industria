package dev.turtywurty.industria.init;

import dev.turtywurty.industria.util.WoodRegistrySet;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayer;

public class RenderLayerMapInit {
    public static void init() {
        BlockRenderLayerMap.INSTANCE.putFluids(RenderLayer.getTranslucent(),
                FluidInit.CRUDE_OIL.still(), FluidInit.CRUDE_OIL.flowing(),
                FluidInit.DIRTY_SODIUM_ALUMINATE.still(), FluidInit.DIRTY_SODIUM_ALUMINATE.flowing(),
                FluidInit.SODIUM_ALUMINATE.still(), FluidInit.SODIUM_ALUMINATE.flowing());

        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), BlockInit.FLUID_TANK);

        for (WoodRegistrySet woodSet : WoodRegistrySet.getWoodSets()) {
            BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(),
                    woodSet.door,
                    woodSet.trapdoor,
                    woodSet.leaves,
                    woodSet.sapling);
        }
    }
}