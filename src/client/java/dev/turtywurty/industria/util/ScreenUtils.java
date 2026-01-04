package dev.turtywurty.industria.util;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class ScreenUtils {
    public static void drawTexture(GuiGraphics context, Identifier texture, int x, int y, float u, float v, int width, int height) {
        drawTexture(context, texture, x, y, u, v, width, height, 256, 256);
    }

    public static void drawTexture(GuiGraphics context, Identifier texture, int x, int y, float u, float v, int width, int height, int color) {
        drawTexture(context, texture, x, y, u, v, width, height, 256, 256, color);
    }

    public static void drawTexture(GuiGraphics context, Identifier texture, int x, int y, float u, float v, int width, int height, int texWidth, int texHeight) {
        drawTexture(context, texture, x, y, u, v, width, height, texWidth, texHeight, -1);
    }

    public static void drawTexture(GuiGraphics context, Identifier texture, int x, int y, float u, float v, int width, int height, int texWidth, int texHeight, int color) {
        context.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, width, height, texWidth, texHeight, color);
    }

    public static void drawGuiTexture(GuiGraphics context, Identifier texture, int x, int y, int width, int height) {
        drawGuiTexture(context, texture, x, y, width, height, -1);
    }

    public static void drawGuiTexture(GuiGraphics context, Identifier texture, int x, int y, int width, int height, int color) {
        context.blitSprite(RenderPipelines.GUI_TEXTURED, texture, x, y, width, height, color);
    }

    public static void renderTiledSprite(GuiGraphics context, RenderPipeline pipeline, TextureAtlasSprite sprite, int x, int y, int width, int height, int color) {
        int spriteWidth = 16;
        int spriteHeight = 16;

        int xCount = Mth.floor((float) width / spriteWidth);
        int yCount = Mth.floor((float) height / spriteHeight);
        int xRemainder = width % spriteWidth;
        int yRemainder = height % spriteHeight;

        Identifier atlasId = sprite.atlasLocation();
        float minU = sprite.getU0();
        float minV = sprite.getV0();

        for (int i = 0; i < xCount; i++) {
            for (int j = 0; j < yCount; j++) {
                int x1 = x + i * spriteWidth;
                int y1 = y + j * spriteHeight;
                int x2 = x1 + spriteWidth;
                int y2 = y1 + spriteHeight;
                float maxU = sprite.getU1();
                float maxV = sprite.getV1();
                context.innerBlit(pipeline, atlasId, x1, x2, y1, y2, minU, maxU, minV, maxV, color);
            }

            if (yRemainder > 0) {
                int x1 = x + i * spriteWidth;
                int y1 = y + yCount * spriteHeight;
                int x2 = x1 + spriteWidth;
                int y2 = y1 + yRemainder;
                float maxU = sprite.getU1();
                float maxV = minV + (sprite.getV1() - sprite.getV0()) * ((float) yRemainder / spriteHeight);
                context.innerBlit(pipeline, atlasId, x1, x2, y1, y2, minU, maxU, minV, maxV, color);
            }
        }

        if (xRemainder > 0) {
            for (int j = 0; j < yCount; j++) {
                int x1 = x + xCount * spriteWidth;
                int y1 = y + j * spriteHeight;
                int x2 = x1 + xRemainder;
                int y2 = y1 + spriteHeight;
                float maxU = minU + (sprite.getU1() - sprite.getU0()) * ((float) xRemainder / spriteWidth);
                float maxV = sprite.getV1();
                context.innerBlit(pipeline, atlasId, x1, x2, y1, y2, minU, maxU, minV, maxV, color);
            }

            if (yRemainder > 0) {
                int x1 = x + xCount * spriteWidth;
                int y1 = y + yCount * spriteHeight;
                int x2 = x1 + xRemainder;
                int y2 = y1 + yRemainder;
                float maxU = minU + (sprite.getU1() - sprite.getU0()) * ((float) xRemainder / spriteWidth);
                float maxV = minV + (sprite.getV1() - sprite.getV0()) * ((float) yRemainder / spriteHeight);
                context.innerBlit(pipeline, atlasId, x1, x2, y1, y2, minU, maxU, minV, maxV, color);
            }
        }
    }

    public static void drawNineSlicedTexture(GuiGraphics context, Identifier texture,
                                             int startX, int startY,
                                             int width, int height,
                                             int u0, int v0, int sliceSize,
                                             boolean drawMiddle) {
        drawNineSlicedTexture(context, texture, startX, startY, width, height, u0, v0, sliceSize, 256, 256, drawMiddle);
    }

    public static void drawNineSlicedTexture(GuiGraphics context, Identifier texture,
                                             int startX, int startY,
                                             int width, int height,
                                             int u0, int v0, int sliceSize) {
        drawNineSlicedTexture(context, texture, startX, startY, width, height, u0, v0, sliceSize, true);
    }

    public static void drawNineSlicedTexture(GuiGraphics context, Identifier texture,
                                             int startX, int startY,
                                             int width, int height,
                                             int u0, int v0, int sliceSize,
                                             int texWidth, int texHeight) {
        drawNineSlicedTexture(context, texture, startX, startY, width, height, u0, v0, sliceSize, texWidth, texHeight, true);
    }

    public static void drawNineSlicedTexture(GuiGraphics context, Identifier texture,
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

    public static void drawTextTruncated(GuiGraphics context, String text, int x, int y, int maxWidth, int color, boolean shadow) {
        Minecraft client = Minecraft.getInstance();
        int textWidth = client.font.width(text);
        if (textWidth <= maxWidth) {
            context.drawString(client.font, text, x, y, color, shadow);
        } else {
            String truncatedText = client.font.plainSubstrByWidth(text, maxWidth - client.font.width("...")) + "...";
            context.drawString(client.font, truncatedText, x, y, color, shadow);
        }
    }

    public static void drawTextTruncated(GuiGraphics context, Component text, int x, int y, int maxWidth, int color, boolean shadow) {
        drawTextTruncated(context, text.getString(), x, y, maxWidth, color, shadow);
    }
}
