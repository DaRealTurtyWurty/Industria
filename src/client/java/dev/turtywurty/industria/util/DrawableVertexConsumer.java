package dev.turtywurty.industria.util;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;

public class DrawableVertexConsumer implements VertexConsumerProvider {
    private final Immediate source;

    public DrawableVertexConsumer(Immediate source) {
        this.source = source;
    }

    @Override
    public VertexConsumer getBuffer(RenderLayer layer) {
        return this.source.getBuffer(layer);
    }

    public void draw() {
        this.source.draw();
    }
}
