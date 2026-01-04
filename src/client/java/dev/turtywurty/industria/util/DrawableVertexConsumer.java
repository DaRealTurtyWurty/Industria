package dev.turtywurty.industria.util;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;

public class DrawableVertexConsumer implements MultiBufferSource {
    private final BufferSource source;

    public DrawableVertexConsumer(BufferSource source) {
        this.source = source;
    }

    @Override
    public VertexConsumer getBuffer(RenderType layer) {
        return this.source.getBuffer(layer);
    }

    public void draw() {
        this.source.endBatch();
    }
}
