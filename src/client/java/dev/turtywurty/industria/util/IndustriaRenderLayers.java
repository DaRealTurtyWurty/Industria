package dev.turtywurty.industria.util;

import dev.turtywurty.industria.Industria;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.TriState;
import net.minecraft.util.Util;

import java.util.function.BiFunction;

import static net.minecraft.client.render.RenderPhase.*;

public class IndustriaRenderLayers {
    private static final BiFunction<Identifier, Boolean, RenderLayer> FLUID_FACTORY = Util.memoize((texture, mipmap) -> createFluid("fluid", texture, mipmap));

    private static RenderLayer.MultiPhase createFluid(String name, Identifier texture, boolean mipmap) {
        RenderLayer.MultiPhaseParameters parameters = RenderLayer.MultiPhaseParameters.builder()
                .program(ENTITY_TRANSLUCENT_PROGRAM)
                .texture(new Texture(texture, TriState.FALSE, mipmap))
                .transparency(TRANSLUCENT_TRANSPARENCY)
                .cull(DISABLE_CULLING)
                .lightmap(ENABLE_LIGHTMAP)
                .overlay(ENABLE_OVERLAY_COLOR)
                .writeMaskState(ALL_MASK)
                .build(false);

        return RenderLayer.of(createLayerName(name), VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS, 256, false, true, parameters);
    }

    private static final RenderLayer SEISMIC_SCANNER_HOLOGRAM = RenderLayer.of(createLayerName("seismic_scanner_hologram"),
            VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.LINES, 1536, false, false,
            RenderLayer.MultiPhaseParameters.builder()
                    .program(LIGHTNING_PROGRAM)
                    .writeMaskState(COLOR_MASK)
                    .transparency(LIGHTNING_TRANSPARENCY)
                    .build(false));

    public static RenderLayer getFluid() {
        return FLUID_FACTORY.apply(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, true);
    }

    public static RenderLayer getFluid(Identifier texture, boolean mipmap) {
        return FLUID_FACTORY.apply(texture, mipmap);
    }

    public static RenderLayer getSeismicScannerHologram() {
        return SEISMIC_SCANNER_HOLOGRAM;
    }

    private static String createLayerName(String name) {
        return Industria.MOD_ID + ":" + name;
    }
}
