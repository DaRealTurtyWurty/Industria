package dev.turtywurty.industria.util;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

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
        context.drawTexture(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, width, height, texWidth, texHeight, color);
    }

    public static void drawGuiTexture(DrawContext context, Identifier texture, int x, int y, int width, int height) {
        drawGuiTexture(context, texture, x, y, width, height, -1);
    }

    public static void drawGuiTexture(DrawContext context, Identifier texture, int x, int y, int width, int height, int color) {
        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, texture, x, y, width, height, color);
    }

    public static void renderTiledSprite(DrawContext context, RenderPipeline pipeline, Sprite sprite, int x, int y, int width, int height, int color) {
        int spriteWidth = 16;
        int spriteHeight = 16;

        int xCount = MathHelper.floor((float) width / spriteWidth);
        int yCount = MathHelper.floor((float) height / spriteHeight);
        int xRemainder = width % spriteWidth;
        int yRemainder = height % spriteHeight;

        Identifier atlasId = sprite.getAtlasId();
        float minU = sprite.getMinU();
        float minV = sprite.getMinV();

        for (int i = 0; i < xCount; i++) {
            for (int j = 0; j < yCount; j++) {
                int x1 = x + i * spriteWidth;
                int y1 = y + j * spriteHeight;
                int x2 = x1 + spriteWidth;
                int y2 = y1 + spriteHeight;
                float maxU = sprite.getMaxU();
                float maxV = sprite.getMaxV();
                context.drawTexturedQuad(pipeline, atlasId, x1, x2, y1, y2, minU, maxU, minV, maxV, color);
            }

            if (yRemainder > 0) {
                int x1 = x + i * spriteWidth;
                int y1 = y + yCount * spriteHeight;
                int x2 = x1 + spriteWidth;
                int y2 = y1 + yRemainder;
                float maxU = sprite.getMaxU();
                float maxV = minV + (sprite.getMaxV() - sprite.getMinV()) * ((float) yRemainder / spriteHeight);
                context.drawTexturedQuad(pipeline, atlasId, x1, x2, y1, y2, minU, maxU, minV, maxV, color);
            }
        }

        if (xRemainder > 0) {
            for (int j = 0; j < yCount; j++) {
                int x1 = x + xCount * spriteWidth;
                int y1 = y + j * spriteHeight;
                int x2 = x1 + xRemainder;
                int y2 = y1 + spriteHeight;
                float maxU = minU + (sprite.getMaxU() - sprite.getMinU()) * ((float) xRemainder / spriteWidth);
                float maxV = sprite.getMaxV();
                context.drawTexturedQuad(pipeline, atlasId, x1, x2, y1, y2, minU, maxU, minV, maxV, color);
            }

            if (yRemainder > 0) {
                int x1 = x + xCount * spriteWidth;
                int y1 = y + yCount * spriteHeight;
                int x2 = x1 + xRemainder;
                int y2 = y1 + yRemainder;
                float maxU = minU + (sprite.getMaxU() - sprite.getMinU()) * ((float) xRemainder / spriteWidth);
                float maxV = minV + (sprite.getMaxV() - sprite.getMinV()) * ((float) yRemainder / spriteHeight);
                context.drawTexturedQuad(pipeline, atlasId, x1, x2, y1, y2, minU, maxU, minV, maxV, color);
            }
        }
    }
}
