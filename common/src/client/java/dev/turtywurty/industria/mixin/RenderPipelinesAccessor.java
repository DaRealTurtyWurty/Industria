package dev.turtywurty.industria.mixin;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.renderer.RenderPipelines;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderPipelines.class)
public class RenderPipelinesAccessor {
    @Accessor("ENTITY_SNIPPET")
    public static RenderPipeline.Snippet industria$getEntitySnippet() {
        throw new AssertionError();
    }
}
