package dev.turtywurty.industria.molding;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.turtymultiloader.client.registration.ClientRegistrations;
import net.minecraft.client.renderer.RenderPipelines;

public final class MoldRenderPipelines {
    private MoldRenderPipelines() {
    }

    public static final RenderPipeline MOLD_LINE = ClientRegistrations.registerRenderPipeline(RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
            .withLocation(Industria.id("pipeline/mold_line"))
            .withVertexShader(Industria.id("core/mold_line"))
            .withFragmentShader(Industria.id("core/mold_line"))
            .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
            .withCull(false)
            .build());

    public static final RenderPipeline MOLD_CAST = ClientRegistrations.registerRenderPipeline(RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
            .withLocation(Industria.id("pipeline/mold_cast"))
            .withVertexShader(Industria.id("core/mold_cast"))
            .withFragmentShader(Industria.id("core/mold_cast"))
            .withSampler("Sampler0")
            .withSampler("Sampler1")
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS)
            .withCull(false)
            .build());
}
