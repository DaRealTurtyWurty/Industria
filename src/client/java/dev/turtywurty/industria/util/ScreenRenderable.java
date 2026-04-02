package dev.turtywurty.industria.util;

import net.minecraft.client.gui.GuiGraphicsExtractor;

@FunctionalInterface
public interface ScreenRenderable {
    void extractRenderState(GuiGraphicsExtractor context, double mouseX, double mouseY, float partialTicks);
}
