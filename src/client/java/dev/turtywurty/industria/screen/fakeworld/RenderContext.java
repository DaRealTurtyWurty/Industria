package dev.turtywurty.industria.screen.fakeworld;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;

public record RenderContext(
        DrawContext guiContext,
        MatrixStack matrices,
        VertexConsumerProvider.Immediate consumers,
        float tickDelta,
        int framebufferWidth,
        int framebufferHeight,
        ClientWorld world
) {
}
