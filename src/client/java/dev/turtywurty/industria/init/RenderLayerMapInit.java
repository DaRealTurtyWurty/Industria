package dev.turtywurty.industria.init;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayer;

public class RenderLayerMapInit {
    public static void init() {
        BlockRenderLayerMap.INSTANCE.putFluids(RenderLayer.getTranslucent(),
                FluidInit.CRUDE_OIL.still(), FluidInit.CRUDE_OIL.flowing(),
                FluidInit.DIRTY_SODIUM_ALUMINATE.still(), FluidInit.DIRTY_SODIUM_ALUMINATE.flowing(),
                FluidInit.SODIUM_ALUMINATE.still(), FluidInit.SODIUM_ALUMINATE.flowing());
    }
}
