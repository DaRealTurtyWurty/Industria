package dev.turtywurty.industria.util;

import net.minecraft.client.gui.DrawContext;

@FunctionalInterface
public interface ScreenRenderable {
    void render(DrawContext context, double mouseX, double mouseY, float partialTicks);
}
