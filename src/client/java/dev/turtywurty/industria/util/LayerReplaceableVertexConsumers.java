package dev.turtywurty.industria.util;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;

public class LayerReplaceableVertexConsumers implements VertexConsumerProvider {
    private final VertexConsumerProvider source;
    private final RenderLayer renderLayer;

    public LayerReplaceableVertexConsumers(VertexConsumerProvider source, RenderLayer renderLayer) {
        this.source = source;
        this.renderLayer = renderLayer;
    }

    @Override
    public VertexConsumer getBuffer(RenderLayer layer) {
        return this.source.getBuffer(this.renderLayer);
    }
}
