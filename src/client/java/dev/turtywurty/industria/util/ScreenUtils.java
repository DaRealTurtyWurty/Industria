package dev.turtywurty.industria.util;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;

public class ScreenUtils {
    public static void drawTexture(DrawContext context, Identifier texture, int x, int y, float u, float v, int width, int height) {
        drawTexture(context, texture, x, y, u, v, width, height, 256, 256);
    }

    public static void drawTexture(DrawContext context, Identifier texture, int x, int y, float u, float v, int width, int height, int color) {
        drawTexture(context, texture, x, y, u, v, width, height, 256, 256, color);
    }

    public static void drawTexture(DrawContext context, Identifier texture, int x, int y, float u, float v, int width, int height, int texWidth, int texHeight) {
        drawTexture(context, texture, x, y, u, v, width, height, texWidth, texHeight, -1);
    }

    public static void drawTexture(DrawContext context, Identifier texture, int x, int y, float u, float v, int width, int height, int texWidth, int texHeight, int color) {
        context.drawTexture(RenderLayer::getGuiTextured, texture, x, y, u, v, width, height, texWidth, texHeight, color);
    }

    public static void drawGuiTexture(DrawContext context, Identifier texture, int x, int y, int width, int height) {
        drawGuiTexture(context, texture, x, y, width, height, -1);
    }

    public static void drawGuiTexture(DrawContext context, Identifier texture, int x, int y, int width, int height, int color) {
        context.drawGuiTexture(RenderLayer::getGuiTextured, texture, x, y, width, height, color);
    }
}
