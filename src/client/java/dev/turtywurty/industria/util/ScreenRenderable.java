package dev.turtywurty.industria.util;

import net.minecraft.client.gui.GuiGraphics;

@FunctionalInterface
public interface ScreenRenderable {
    void render(GuiGraphics context, double mouseX, double mouseY, float partialTicks);
}
