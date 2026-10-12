package dev.turtywurty.industria.molding;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.turtymultiloader.client.registration.ClientRegistrations;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;

public final class MoldRenderPipelines {
    private MoldRenderPipelines() {
    }

    public static final RenderPipeline MOLD_LINE = ClientRegistrations
        .registerRenderPipeline(RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
            .withLocation(Industria.id("pipeline/mold_line"))
            .withVertexShader(Industria.id("core/mold_line"))
            .withFragmentShader(Industria.id("core/mold_line"))
            .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR).withPrimitiveTopology(PrimitiveTopology.QUADS)
            .withCull(false)
            .build());

    public static final RenderPipeline MOLD_CAST = ClientRegistrations
        .registerRenderPipeline(RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
            .withLocation(Industria.id("pipeline/mold_cast"))
            .withVertexShader(Industria.id("core/mold_cast"))
            .withFragmentShader(Industria.id("core/mold_cast"))
            .withBindGroupLayout(BindGroupLayouts.SAMPLER0)
            .withBindGroupLayout(BindGroupLayouts.SAMPLER1)
            .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP)
            .withPrimitiveTopology(PrimitiveTopology.QUADS)
            .withCull(false)
            .build());
}
