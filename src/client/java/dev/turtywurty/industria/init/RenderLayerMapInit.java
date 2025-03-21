package dev.turtywurty.industria.init;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayer;

public class RenderLayerMapInit {
    public static void init() {
        BlockRenderLayerMap.INSTANCE.putFluids(RenderLayer.getTranslucent(), FluidInit.CRUDE_OIL, FluidInit.CRUDE_OIL_FLOWING);
    }
}
