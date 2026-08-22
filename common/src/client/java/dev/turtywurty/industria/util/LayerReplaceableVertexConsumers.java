package dev.turtywurty.industria.util;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;

public class LayerReplaceableVertexConsumers implements MultiBufferSource {
    private final MultiBufferSource source;
    private final RenderType renderLayer;

    public LayerReplaceableVertexConsumers(MultiBufferSource source, RenderType renderLayer) {
        this.source = source;
        this.renderLayer = renderLayer;
    }

    @Override
    public VertexConsumer getBuffer(RenderType layer) {
        return this.source.getBuffer(this.renderLayer);
    }
}
