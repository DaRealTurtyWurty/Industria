package dev.turtywurty.industria.util;

import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.turtymultiloader.client.registration.ClientRegistrations;
import java.util.function.Function;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

/** Shared custom render types used by Industria renderers and models. */
public final class IndustriaRenderTypes {
    private static final RenderPipeline GLASS_PIPELINE = ClientRegistrations.registerRenderPipeline(
            RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET)
                    .withLocation(Industria.id("pipeline/machine_glass"))
                    .withShaderDefine("ALPHA_CUTOUT", 0.1F)
                    .withShaderDefine("PER_FACE_LIGHTING")
                    .withBindGroupLayout(BindGroupLayouts.SAMPLER1)
                    .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                    .withCull(false)
                    .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false))
                    .build());
    public static final Function<Identifier, RenderType> GLASS_RENDER_TYPE = Util.memoize(texture ->
            ClientRegistrations.createRenderType("industria_machine_glass",
                    RenderSetup.builder(GLASS_PIPELINE)
                            .withTexture("Sampler0", texture)
                            .useLightmap()
                            .useOverlay()
                            .affectsCrumbling()
                            .sortOnUpload()
                            .setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE)
                            .createRenderSetup()));

    private IndustriaRenderTypes() {
    }
}
