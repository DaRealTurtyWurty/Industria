package dev.turtywurty.industria.screen.fakeworld;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;

public record RenderContext(
        GuiGraphics guiContext,
        PoseStack matrices,
        MultiBufferSource.BufferSource consumers,
        float tickDelta,
        int framebufferWidth,
        int framebufferHeight,
        ClientLevel world
) {
}
