package dev.turtywurty.industria.util;

import dev.turtywurty.industria.Industria;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;

import static net.minecraft.client.render.RenderPhase.*;

public class IndustriaRenderLayers {
    private static final RenderLayer FLUID = RenderLayer.of(createLayerName("fluid"),
            VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS, 256, false, true,
            RenderLayer.MultiPhaseParameters.builder()
                    .program(ENTITY_TRANSLUCENT_CULL_PROGRAM)
                    .texture(MIPMAP_BLOCK_ATLAS_TEXTURE)
                    .transparency(TRANSLUCENT_TRANSPARENCY)
                    .lightmap(ENABLE_LIGHTMAP)
                    .overlay(ENABLE_OVERLAY_COLOR)
                    .build(true));

    public static RenderLayer getFluid() {
        return FLUID;
    }

    private static String createLayerName(String name) {
        return Industria.MOD_ID + ":" + name;
    }
}
