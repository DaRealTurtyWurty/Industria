package dev.turtywurty.industria.util;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.Sprite;
import net.minecraft.text.Text;
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

    public static void drawNineSlicedTexture(DrawContext context, Identifier texture,
                                             int startX, int startY,
                                             int width, int height,
                                             int u0, int v0, int sliceSize,
                                             boolean drawMiddle) {
        drawNineSlicedTexture(context, texture, startX, startY, width, height, u0, v0, sliceSize, 256, 256, drawMiddle);
    }

    public static void drawNineSlicedTexture(DrawContext context, Identifier texture,
                                             int startX, int startY,
                                             int width, int height,
                                             int u0, int v0, int sliceSize) {
        drawNineSlicedTexture(context, texture, startX, startY, width, height, u0, v0, sliceSize, true);
    }

    public static void drawNineSlicedTexture(DrawContext context, Identifier texture,
                                             int startX, int startY,
                                             int width, int height,
                                             int u0, int v0, int sliceSize,
                                             int texWidth, int texHeight) {
        drawNineSlicedTexture(context, texture, startX, startY, width, height, u0, v0, sliceSize, texWidth, texHeight, true);
    }

    public static void drawNineSlicedTexture(DrawContext context, Identifier texture,
                                             int startX, int startY,
                                             int width, int height,
                                             int u0, int v0, int sliceSize,
                                             int texWidth, int texHeight,
                                             boolean drawMiddle) {
        int rightStart = startX + width - sliceSize;
        int bottomStart = startY + height - sliceSize;

        int u1 = u0 + sliceSize;
        int u2 = u1 + sliceSize;
        int v1 = v0 + sliceSize;
        int v2 = v1 + sliceSize;

        // Top row
        ScreenUtils.drawTexture(context, texture, startX, startY, u0, v0, sliceSize, sliceSize, texWidth, texHeight);
        for (int x = sliceSize; x < rightStart - startX; x += sliceSize) {
            int drawWidth = Math.min(sliceSize, rightStart - startX - x);
            ScreenUtils.drawTexture(context, texture, startX + x, startY, u1, v0, drawWidth, sliceSize, texWidth, texHeight);
        }

        ScreenUtils.drawTexture(context, texture, rightStart, startY, u2, v0, sliceSize, sliceSize, texWidth, texHeight);

        // Middle rows
        for (int y = sliceSize; y < bottomStart - startY; y += sliceSize) {
            int drawHeight = Math.min(sliceSize, bottomStart - startY - y);
            ScreenUtils.drawTexture(context, texture, startX, startY + y, u0, v1, sliceSize, drawHeight, texWidth, texHeight);
            if (drawMiddle) {
                for (int x = sliceSize; x < rightStart - startX; x += sliceSize) {
                    int drawWidth = Math.min(sliceSize, rightStart - startX - x);
                    ScreenUtils.drawTexture(context, texture, startX + x, startY + y, u1, v1, drawWidth, drawHeight, texWidth, texHeight);
                }
            }
            ScreenUtils.drawTexture(context, texture, rightStart, startY + y, u2, v1, sliceSize, drawHeight, texWidth, texHeight);
        }

        // Bottom row
        ScreenUtils.drawTexture(context, texture, startX, bottomStart, u0, v2, sliceSize, sliceSize, texWidth, texHeight);
        for (int x = sliceSize; x < rightStart - startX; x += sliceSize) {
            int drawWidth = Math.min(sliceSize, rightStart - startX - x);
            ScreenUtils.drawTexture(context, texture, startX + x, bottomStart, u1, v2, drawWidth, sliceSize, texWidth, texHeight);
        }
        ScreenUtils.drawTexture(context, texture, rightStart, bottomStart, u2, v2, sliceSize, sliceSize, texWidth, texHeight);
    }

    public static void drawTextTruncated(DrawContext context, String text, int x, int y, int maxWidth, int color, boolean shadow) {
        MinecraftClient client = MinecraftClient.getInstance();
        int textWidth = client.textRenderer.getWidth(text);
        if (textWidth <= maxWidth) {
            context.drawText(client.textRenderer, text, x, y, color, shadow);
        } else {
            String truncatedText = client.textRenderer.trimToWidth(text, maxWidth - client.textRenderer.getWidth("...")) + "...";
            context.drawText(client.textRenderer, truncatedText, x, y, color, shadow);
        }
    }

    public static void drawTextTruncated(DrawContext context, Text text, int x, int y, int maxWidth, int color, boolean shadow) {
        drawTextTruncated(context, text.getString(), x, y, maxWidth, color, shadow);
    }
}
