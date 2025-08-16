package dev.turtywurty.industria.init;

import dev.turtywurty.industria.util.WoodRegistrySet;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.minecraft.client.render.BlockRenderLayer;

public class RenderLayerMapInit {
    public static void init() {
        BlockRenderLayerMap.putFluids(BlockRenderLayer.TRANSLUCENT,
                FluidInit.CRUDE_OIL.still(), FluidInit.CRUDE_OIL.flowing(),
                FluidInit.DIRTY_SODIUM_ALUMINATE.still(), FluidInit.DIRTY_SODIUM_ALUMINATE.flowing(),
                FluidInit.SODIUM_ALUMINATE.still(), FluidInit.SODIUM_ALUMINATE.flowing());

        BlockRenderLayerMap.putBlocks(BlockRenderLayer.CUTOUT, BlockInit.FLUID_TANK);

        for (WoodRegistrySet woodSet : WoodRegistrySet.getWoodSets()) {
            BlockRenderLayerMap.putBlocks(BlockRenderLayer.CUTOUT,
                    woodSet.door,
                    woodSet.trapdoor,
                    woodSet.leaves,
                    woodSet.sapling);
        }
    }
}