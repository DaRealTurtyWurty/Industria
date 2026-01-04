package dev.turtywurty.industria.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.function.UnaryOperator;

public class InWorldFluidRenderingComponent {
    private boolean shouldDebugAmount = false;

    public void setShouldDebugAmount(boolean shouldDebugAmount) {
        this.shouldDebugAmount = shouldDebugAmount;
    }

    public void render(@Nullable SingleFluidStorage fluidTank, SubmitNodeCollector queue, PoseStack matrices, int light, int overlay, Level world, BlockPos pos, float x1, float y1, float z1, float x2, float maxHeightPixels, float z2) {
        render(fluidTank, queue, matrices, light, overlay, world, pos, x1, y1, z1, x2, maxHeightPixels, z2, IndeterminateBoolean.INDETERMINATE);
    }

    public void render(@Nullable SingleFluidStorage fluidTank, SubmitNodeCollector queue, PoseStack matrices, int light, int overlay, Level world, BlockPos pos, float x1, float y1, float z1, float x2, float maxHeightPixels, float z2, IndeterminateBoolean drawTopFace) {
        render(fluidTank, queue, matrices, light, overlay, world, pos, x1, y1, z1, x2, maxHeightPixels, z2, 0xFFFFFFFF, ColorMode.MULTIPLICATION, drawTopFace);
    }

    public void render(@Nullable SingleFluidStorage fluidTank, SubmitNodeCollector queue, PoseStack matrices, int light, int overlay, Level world, BlockPos pos, float x1, float y1, float z1, float x2, float maxHeightPixels, float z2, int color, ColorMode colorMode) {
        render(fluidTank, queue, matrices, light, overlay, world, pos, x1, y1, z1, x2, maxHeightPixels, z2, color, colorMode, IndeterminateBoolean.INDETERMINATE);
    }

    public void render(@Nullable SingleFluidStorage fluidTank, SubmitNodeCollector queue, PoseStack matrices, int light, int overlay, Level world, BlockPos pos, float x1, float y1, float z1, float x2, float maxHeightPixels, float z2, int color, ColorMode colorMode, IndeterminateBoolean drawTopFace) {
        if (fluidTank == null || fluidTank.isResourceBlank() || fluidTank.amount <= 0)
            return;

        FluidVariant fluidVariant = fluidTank.getResource();
        long amount = fluidTank.amount;
        long capacity = fluidTank.getCapacity();
        float fillPercentage = (float) amount / capacity;
        fillPercentage = Mth.clamp(fillPercentage, 0.0F, 1.0F);

        if (this.shouldDebugAmount) {
            fillPercentage = (float) (Math.sin(world.getGameTime() / 64.0) * 0.5 + 0.5);
        }

        int fluidColor = FluidVariantRendering.getColor(fluidVariant, world, pos);
        int newFluidColor = ColorMode.modifyColor(fluidColor, color, colorMode);

        TextureAtlasSprite stillSprite = FluidVariantRendering.getSprite(fluidVariant);
        if (stillSprite == null)
            return;

        RenderType renderLayer = RenderTypes.itemEntityTranslucentCull(stillSprite.atlasLocation());

        float y2 = ((fillPercentage * maxHeightPixels) / 16f) + y1;

        matrices.pushPose();
        matrices.mulPose(Axis.XP.rotationDegrees(180));

        if (FluidVariantAttributes.isLighterThanAir(fluidVariant)) {
            matrices.mulPose(Axis.XP.rotationDegrees(180));
        }

        int blockLight = (light >> 4) & 0xF;
        int luminosity = Math.max(blockLight, FluidVariantAttributes.getLuminance(fluidVariant));
        int newLight = (light & 0xF00000) | (luminosity << 4);

        float finalFillPercentage = fillPercentage;
        queue.submitCustomGeometry(matrices, renderLayer, (entry, vertexConsumer) -> {
            // Front (XY plane, z constant)
            drawTiledXYQuad(vertexConsumer, entry,
                    x1, y1, z1 + 0.001F,
                    x2, y2, z1 + 0.001F,
                    stillSprite, newFluidColor, newLight, overlay, 0.0F, 1.0F, -1.0F);

            // Back (XY plane, z constant)
            drawReversedTiledXYQuad(vertexConsumer, entry,
                    x1, y1, z2 - 0.001F,
                    x2, y2, z2 - 0.001F,
                    stillSprite, newFluidColor, newLight, overlay, 0.0F, 1.0F, 1.0F);

            // Left (YZ plane, x constant)
            drawReversedTiledYZQuad(vertexConsumer, entry,
                    x1 + 0.001F, y1, z1,
                    y2, z2,
                    stillSprite, newFluidColor, newLight, overlay, 1.0F, 1.0F, 0.0F);

            // Right (YZ plane, x constant)
            drawTiledYZQuad(vertexConsumer, entry,
                    x2 - 0.001F, y1, z1,
                    y2, z2,
                    stillSprite, newFluidColor, newLight, overlay, -1.0F, 1.0F, 0.0F);

            if (drawTopFace.evaluate(finalFillPercentage < 1.0F)) {
                drawTiledTopQuad(vertexConsumer, entry, x1, y2, z1 + 0.001F, x2, z2 - 0.001F, stillSprite, fluidColor, newLight, overlay);
            }
        });

        matrices.popPose();
    }

    public void renderTopFaceOnly(@Nullable FluidVariant fluidVariant, SubmitNodeCollector queue, PoseStack matrices, int light, int overlay, Level world, BlockPos pos, float x1, float y, float z1, float x2, float z2, UnaryOperator<RenderType> wrapRenderLayer) {
        renderTopFaceOnly(fluidVariant, queue, matrices, light, overlay, world, pos, x1, y, z1, x2, z2, 0xFFFFFFFF, ColorMode.MULTIPLICATION, wrapRenderLayer);
    }

    public void renderTopFaceOnly(@Nullable FluidVariant fluidVariant, SubmitNodeCollector queue, PoseStack matrices, int light, int overlay, Level world, BlockPos pos, float x1, float y, float z1, float x2, float z2) {
        renderTopFaceOnly(fluidVariant, queue, matrices, light, overlay, world, pos, x1, y, z1, x2, z2, 0xFFFFFFFF, ColorMode.MULTIPLICATION);
    }

    public void renderTopFaceOnly(@Nullable FluidVariant fluidVariant, SubmitNodeCollector queue, PoseStack matrices, int light, int overlay, Level world, BlockPos pos, float x1, float y, float z1, float x2, float z2, int color, ColorMode colorMode) {
        renderTopFaceOnly(fluidVariant, queue, matrices, light, overlay, world, pos, x1, y, z1, x2, z2, color, colorMode, renderLayer -> renderLayer);
    }

    public void renderTopFaceOnly(@Nullable FluidVariant fluidVariant, SubmitNodeCollector queue, PoseStack matrices, final int light, int overlay, Level world, BlockPos pos, float x1, float y, float z1, float x2, float z2, int color, ColorMode colorMode, UnaryOperator<RenderType> wrapRenderLayer) {
        if (fluidVariant == null)
            return;

        int fluidColor = FluidVariantRendering.getColor(fluidVariant, world, pos);
        int newFluidColor = ColorMode.modifyColor(fluidColor, color, colorMode);

        TextureAtlasSprite stillSprite = FluidVariantRendering.getSprite(fluidVariant);
        if (stillSprite == null)
            return;

        RenderType renderLayer = RenderTypes.itemEntityTranslucentCull(stillSprite.atlasLocation());
        renderLayer = wrapRenderLayer.apply(renderLayer);

        matrices.pushPose();
        matrices.mulPose(Axis.XP.rotationDegrees(180));

        if (FluidVariantAttributes.isLighterThanAir(fluidVariant)) {
            matrices.mulPose(Axis.XP.rotationDegrees(180));
        }

        queue.submitCustomGeometry(matrices, renderLayer, (entry, vertexConsumer) -> {
            int blockLight = (light >> 4) & 0xF;
            int luminosity = Math.max(blockLight, FluidVariantAttributes.getLuminance(fluidVariant));
            int newLight = (light & 0xF00000) | (luminosity << 4);

            drawTiledTopQuad(vertexConsumer, entry, x1, y, z1 + 0.001F, x2, z2 - 0.001F, stillSprite, newFluidColor, newLight, overlay);
        });

        matrices.popPose();
    }

    public static void drawTiledTopQuad(VertexConsumer vertexConsumer,
                                        PoseStack.Pose entry,
                                        float x1, float y, float z1,
                                        float x2, float z2,
                                        TextureAtlasSprite sprite,
                                        int color,
                                        int light, int overlay) {
        float tileSize = 1.0f; // Maximum tile size in world space
        int tileCountX = Math.max(1, Math.round((x2 - x1) / tileSize));
        int tileCountZ = Math.max(1, Math.round((z2 - z1) / tileSize));

        float tileWidth = (x2 - x1) / tileCountX;
        float tileDepth = (z2 - z1) / tileCountZ;

        float u0 = sprite.getU0();
        float v0 = sprite.getV0();
        float u1 = sprite.getU1();
        float v1 = sprite.getV1();

        float tileUSize = (u1 - u0);
        float tileVSize = (v1 - v0);

        for (int i = 0; i < tileCountX; i++) {
            for (int j = 0; j < tileCountZ; j++) {
                float xStart = x1 + i * tileWidth;
                float xEnd = xStart + tileWidth;
                float zStart = z1 + j * tileDepth;
                float zEnd = zStart + tileDepth;

                float uEnd = u0 + tileUSize;
                float vEnd = v0 + tileVSize;

                vertexConsumer.addVertex(entry, xStart, y, zStart)
                        .setColor(color)
                        .setUv(u0, v0)
                        .setLight(light)
                        .setOverlay(overlay)
                        .setNormal(0.0F, 1.0F, 0.0F);

                vertexConsumer.addVertex(entry, xStart, y, zEnd)
                        .setColor(color)
                        .setUv(u0, vEnd)
                        .setLight(light)
                        .setOverlay(overlay)
                        .setNormal(0.0F, 1.0F, 0.0F);

                vertexConsumer.addVertex(entry, xEnd, y, zEnd)
                        .setColor(color)
                        .setUv(uEnd, vEnd)
                        .setLight(light)
                        .setOverlay(overlay)
                        .setNormal(0.0F, 1.0F, 0.0F);

                vertexConsumer.addVertex(entry, xEnd, y, zStart)
                        .setColor(color)
                        .setUv(uEnd, v0)
                        .setLight(light)
                        .setOverlay(overlay)
                        .setNormal(entry, 0.0F, 1.0F, 0.0F);
            }
        }
    }

    public void drawTiledXYQuadOnly(@Nullable FluidVariant fluidVariant, SubmitNodeCollector queue, PoseStack matrices, int light, int overlay, Level world, BlockPos pos, float x1, float y1, float z1, float x2, float y2, float z2) {
        drawTiledXYQuadOnly(fluidVariant, queue, matrices, light, overlay, world, pos, x1, y1, z1, x2, y2, z2, 0xFFFFFFFF, ColorMode.MULTIPLICATION);
    }

    public void drawTiledXYQuadOnly(@Nullable FluidVariant fluidVariant, SubmitNodeCollector queue, PoseStack matrices, final int light, int overlay, Level world, BlockPos pos, float x1, float y1, float z1, float x2, float y2, float z2, int color, ColorMode colorMode) {
        if (fluidVariant == null)
            return;

        int fluidColor = FluidVariantRendering.getColor(fluidVariant, world, pos);
        int newFluidColor = ColorMode.modifyColor(fluidColor, color, colorMode);

        TextureAtlasSprite stillSprite = FluidVariantRendering.getSprite(fluidVariant);
        if (stillSprite == null)
            return;

        RenderType renderLayer = RenderTypes.itemEntityTranslucentCull(stillSprite.atlasLocation());

        matrices.pushPose();
        matrices.mulPose(Axis.XP.rotationDegrees(180));

        if (FluidVariantAttributes.isLighterThanAir(fluidVariant)) {
            matrices.mulPose(Axis.XP.rotationDegrees(180));
        }

        int blockLight = (light >> 4) & 0xF;
        int luminosity = Math.max(blockLight, FluidVariantAttributes.getLuminance(fluidVariant));
        int newLight = (light & 0xF00000) | (luminosity << 4);

        queue.submitCustomGeometry(matrices, renderLayer, (entry, vertexConsumer) ->
                drawTiledXYQuad(vertexConsumer, entry, x1, y1, z1, x2, y2, z2, stillSprite, newFluidColor, newLight, overlay, 0.0F, 1.0F, -1.0F));

        matrices.popPose();
    }

    // For front and back (XY plane)
    public static void drawTiledXYQuad(VertexConsumer vertexConsumer,
                                       PoseStack.Pose entry,
                                       float x1, float y1, float z1,
                                       float x2, float y2, float z2,
                                       TextureAtlasSprite sprite,
                                       int color,
                                       int light, int overlay,
                                       float nx, float ny, float nz) {
        float tileSize = 1.0f;
        int fullTilesX = (int) ((x2 - x1) / tileSize);
        int fullTilesY = (int) ((y2 - y1) / tileSize);
        float leftoverX = (x2 - x1) - (fullTilesX * tileSize);
        float leftoverY = (y2 - y1) - (fullTilesY * tileSize);

        // Draw full tiles
        for (int i = 0; i < fullTilesX; i++) {
            for (int j = 0; j < fullTilesY; j++) {
                float xStart = x1 + i * tileSize;
                float xEnd = xStart + tileSize;
                float yStart = y1 + j * tileSize;
                float yEnd = yStart + tileSize;
                float u0 = sprite.getU0();
                float v0 = sprite.getV0();
                float u1 = sprite.getU1();
                float v1 = sprite.getV1();
                drawQuad(vertexConsumer, entry, xStart, yStart, z1, xEnd, yEnd, z2, u0, v0, u1, v1, color, light, overlay, nx, ny, nz);
            }
        }

        // Draw leftover tiles in x
        if (leftoverX > 0) {
            for (int j = 0; j < fullTilesY; j++) {
                float xStart = x1 + fullTilesX * tileSize;
                float xEnd = xStart + leftoverX;
                float yStart = y1 + j * tileSize;
                float yEnd = yStart + tileSize;
                float u0 = sprite.getU0();
                float v0 = sprite.getV0();
                float u1 = sprite.getU(leftoverX);
                float v1 = sprite.getV(tileSize);
                drawQuad(vertexConsumer, entry, xStart, yStart, z1, xEnd, yEnd, z2, u0, v0, u1, v1, color, light, overlay, nx, ny, nz);
            }
        }

        // Draw leftover tiles in y
        if (leftoverY > 0) {
            for (int i = 0; i < fullTilesX; i++) {
                float xStart = x1 + i * tileSize;
                float xEnd = xStart + tileSize;
                float yStart = y1 + fullTilesY * tileSize;
                float yEnd = yStart + leftoverY;
                float u0 = sprite.getU0();
                float v0 = sprite.getV0();
                float u1 = sprite.getU(tileSize);
                float v1 = sprite.getV(leftoverY);
                drawQuad(vertexConsumer, entry, xStart, yStart, z1, xEnd, yEnd, z2, u0, v0, u1, v1, color, light, overlay, nx, ny, nz);
            }

            // Draw the corner leftover tile if both leftoverX and leftoverY > 0
            if (leftoverX > 0) {
                float xStart = x1 + fullTilesX * tileSize;
                float xEnd = xStart + leftoverX;
                float yStart = y1 + fullTilesY * tileSize;
                float yEnd = yStart + leftoverY;
                float u0 = sprite.getU0();
                float v0 = sprite.getV0();
                float u1 = sprite.getU(leftoverX);
                float v1 = sprite.getV(leftoverY);
                drawQuad(vertexConsumer, entry, xStart, yStart, z1, xEnd, yEnd, z2, u0, v0, u1, v1, color, light, overlay, nx, ny, nz);
            }
        }
    }

    // For left and right (YZ plane)
    public static void drawTiledYZQuad(VertexConsumer vertexConsumer,
                                       PoseStack.Pose entry,
                                       float x, float y1, float z1,
                                       float y2, float z2,
                                       TextureAtlasSprite sprite,
                                       int color,
                                       int light, int overlay,
                                       float nx, float ny, float nz) {
        float tileSize = 1.0f;
        int fullTilesZ = (int) ((z2 - z1) / tileSize);
        int fullTilesY = (int) ((y2 - y1) / tileSize);
        float leftoverZ = (z2 - z1) - (fullTilesZ * tileSize);
        float leftoverY = (y2 - y1) - (fullTilesY * tileSize);

        // Draw full tiles
        for (int i = 0; i < fullTilesZ; i++) {
            for (int j = 0; j < fullTilesY; j++) {
                float zStart = z1 + i * tileSize;
                float zEnd = zStart + tileSize;
                float yStart = y1 + j * tileSize;
                float yEnd = yStart + tileSize;
                float u0 = sprite.getU0();
                float v0 = sprite.getV0();
                float u1 = sprite.getU1();
                float v1 = sprite.getV1();
                drawQuad(vertexConsumer, entry, x, yStart, zStart, x, yEnd, zEnd, u0, v0, u1, v1, color, light, overlay, nx, ny, nz);
            }
        }

        // Draw leftover tiles in z
        if (leftoverZ > 0) {
            for (int j = 0; j < fullTilesY; j++) {
                float zStart = z1 + fullTilesZ * tileSize;
                float zEnd = zStart + leftoverZ;
                float yStart = y1 + j * tileSize;
                float yEnd = yStart + tileSize;
                float u0 = sprite.getU0();
                float v0 = sprite.getV0();
                float u1 = sprite.getU(leftoverZ);
                float v1 = sprite.getV(tileSize);
                drawQuad(vertexConsumer, entry, x, yStart, zStart, x, yEnd, zEnd, u0, v0, u1, v1, color, light, overlay, nx, ny, nz);
            }
        }

        // Draw leftover tiles in y
        if (leftoverY > 0) {
            for (int i = 0; i < fullTilesZ; i++) {
                float zStart = z1 + i * tileSize;
                float zEnd = zStart + tileSize;
                float yStart = y1 + fullTilesY * tileSize;
                float yEnd = yStart + leftoverY;
                float u0 = sprite.getU0();
                float v0 = sprite.getV0();
                float u1 = sprite.getU(tileSize);
                float v1 = sprite.getV(leftoverY);
                drawQuad(vertexConsumer, entry, x, yStart, zStart, x, yEnd, zEnd, u0, v0, u1, v1, color, light, overlay, nx, ny, nz);
            }

            // Draw the corner leftover tile if both leftoverZ and leftoverY > 0
            if (leftoverZ > 0) {
                float zStart = z1 + fullTilesZ * tileSize;
                float zEnd = zStart + leftoverZ;
                float yStart = y1 + fullTilesY * tileSize;
                float yEnd = yStart + leftoverY;
                float u0 = sprite.getU0();
                float v0 = sprite.getV0();
                float u1 = sprite.getU(leftoverZ);
                float v1 = sprite.getV(leftoverY);
                drawQuad(vertexConsumer, entry, x, yStart, zStart, x, yEnd, zEnd, u0, v0, u1, v1, color, light, overlay, nx, ny, nz);
            }
        }
    }

    private static void drawQuad(VertexConsumer vertexConsumer,
                                 PoseStack.Pose entry,
                                 float x1, float y1, float z1,
                                 float x2, float y2, float z2,
                                 float minU, float minV,
                                 float maxU, float maxV,
                                 int color,
                                 int light, int overlay,
                                 float normalX, float normalY, float normalZ) {
        vertexConsumer.addVertex(entry, x1, y1, z1)
                .setColor(color)
                .setUv(minU, minV)
                .setLight(light)
                .setOverlay(overlay)
                .setNormal(normalX, normalY, normalZ);

        vertexConsumer.addVertex(entry, x1, y2, z1)
                .setColor(color)
                .setUv(minU, maxV)
                .setLight(light)
                .setOverlay(overlay)
                .setNormal(normalX, normalY, normalZ);

        vertexConsumer.addVertex(entry, x2, y2, z2)
                .setColor(color)
                .setUv(maxU, maxV)
                .setLight(light)
                .setOverlay(overlay)
                .setNormal(normalX, normalY, normalZ);

        vertexConsumer.addVertex(entry, x2, y1, z2)
                .setColor(color)
                .setUv(maxU, minV)
                .setLight(light)
                .setOverlay(overlay)
                .setNormal(normalX, normalY, normalZ);
    }

    private static void drawReversedQuad(VertexConsumer vertexConsumer,
                                         PoseStack.Pose entry,
                                         float x1, float y1, float z1,
                                         float x2, float y2, float z2,
                                         float minU, float minV,
                                         float maxU, float maxV,
                                         int color,
                                         int light, int overlay,
                                         float normalX, float normalY, float normalZ) {
        // Vertex 4: (x2, y1, z2) with (maxU, minV)
        vertexConsumer.addVertex(entry, x2, y1, z2).setColor(color).setUv(maxU, minV).setLight(light).setOverlay(overlay).setNormal(normalX, normalY, normalZ);
        // Vertex 3: (x2, y2, z2) with (maxU, maxV)
        vertexConsumer.addVertex(entry, x2, y2, z2).setColor(color).setUv(maxU, maxV).setLight(light).setOverlay(overlay).setNormal(normalX, normalY, normalZ);
        // Vertex 2: (x1, y2, z1) with (minU, maxV)
        vertexConsumer.addVertex(entry, x1, y2, z1).setColor(color).setUv(minU, maxV).setLight(light).setOverlay(overlay).setNormal(normalX, normalY, normalZ);
        // Vertex 1: (x1, y1, z1) with (minU, minV)
        vertexConsumer.addVertex(entry, x1, y1, z1).setColor(color).setUv(minU, minV).setLight(light).setOverlay(overlay).setNormal(normalX, normalY, normalZ);
    }

    private static void drawReversedTiledXYQuad(VertexConsumer vertexConsumer,
                                                PoseStack.Pose entry,
                                                float x1, float y1, float z1,
                                                float x2, float y2, float z2,
                                                TextureAtlasSprite sprite,
                                                int color,
                                                int light, int overlay,
                                                float nx, float ny, float nz) {
        float tileSize = 1.0f;
        int fullTilesX = (int) ((x2 - x1) / tileSize);
        int fullTilesY = (int) ((y2 - y1) / tileSize);
        float leftoverX = (x2 - x1) - (fullTilesX * tileSize);
        float leftoverY = (y2 - y1) - (fullTilesY * tileSize);

        // Draw full tiles
        for (int i = 0; i < fullTilesX; i++) {
            for (int j = 0; j < fullTilesY; j++) {
                float xStart = x1 + i * tileSize;
                float xEnd = xStart + tileSize;
                float yStart = y1 + j * tileSize;
                float yEnd = yStart + tileSize;
                float u0 = sprite.getU0();
                float v0 = sprite.getV0();
                float u1 = sprite.getU1();
                float v1 = sprite.getV1();
                drawReversedQuad(vertexConsumer, entry, xStart, yStart, z1, xEnd, yEnd, z2, u0, v0, u1, v1, color, light, overlay, nx, ny, nz);
            }
        }

        // Draw leftover tiles in x
        if (leftoverX > 0) {
            for (int j = 0; j < fullTilesY; j++) {
                float xStart = x1 + fullTilesX * tileSize;
                float xEnd = xStart + leftoverX;
                float yStart = y1 + j * tileSize;
                float yEnd = yStart + tileSize;
                float u0 = sprite.getU0();
                float v0 = sprite.getV0();
                float u1 = sprite.getU(leftoverX);
                float v1 = sprite.getV(tileSize);
                drawReversedQuad(vertexConsumer, entry, xStart, yStart, z1, xEnd, yEnd, z2, u0, v0, u1, v1, color, light, overlay, nx, ny, nz);
            }
        }

        // Draw leftover tiles in y
        if (leftoverY > 0) {
            for (int i = 0; i < fullTilesX; i++) {
                float xStart = x1 + i * tileSize;
                float xEnd = xStart + tileSize;
                float yStart = y1 + fullTilesY * tileSize;
                float yEnd = yStart + leftoverY;
                float u0 = sprite.getU0();
                float v0 = sprite.getV0();
                float u1 = sprite.getU(tileSize);
                float v1 = sprite.getV(leftoverY);
                drawReversedQuad(vertexConsumer, entry, xStart, yStart, z1, xEnd, yEnd, z2, u0, v0, u1, v1, color, light, overlay, nx, ny, nz);
            }

            // Draw the corner leftover tile if both leftoverX and leftoverY > 0
            if (leftoverX > 0) {
                float xStart = x1 + fullTilesX * tileSize;
                float xEnd = xStart + leftoverX;
                float yStart = y1 + fullTilesY * tileSize;
                float yEnd = yStart + leftoverY;
                float u0 = sprite.getU0();
                float v0 = sprite.getV0();
                float u1 = sprite.getU(leftoverX);
                float v1 = sprite.getV(leftoverY);
                drawReversedQuad(vertexConsumer, entry, xStart, yStart, z1, xEnd, yEnd, z2, u0, v0, u1, v1, color, light, overlay, nx, ny, nz);
            }
        }
    }

    private static void drawReversedTiledYZQuad(VertexConsumer vertexConsumer,
                                                PoseStack.Pose entry,
                                                float x, float y1, float z1,
                                                float y2, float z2,
                                                TextureAtlasSprite sprite,
                                                int color,
                                                int light, int overlay,
                                                float nx, float ny, float nz) {
        float tileSize = 1.0f;
        int fullTilesZ = (int) ((z2 - z1) / tileSize);
        int fullTilesY = (int) ((y2 - y1) / tileSize);
        float leftoverZ = (z2 - z1) - (fullTilesZ * tileSize);
        float leftoverY = (y2 - y1) - (fullTilesY * tileSize);

        // Draw full tiles
        for (int i = 0; i < fullTilesZ; i++) {
            for (int j = 0; j < fullTilesY; j++) {
                float zStart = z1 + i * tileSize;
                float zEnd = zStart + tileSize;
                float yStart = y1 + j * tileSize;
                float yEnd = yStart + tileSize;
                float u0 = sprite.getU0();
                float v0 = sprite.getV0();
                float u1 = sprite.getU1();
                float v1 = sprite.getV1();
                drawReversedQuad(vertexConsumer, entry, x, yStart, zStart, x, yEnd, zEnd, u0, v0, u1, v1, color, light, overlay, nx, ny, nz);
            }
        }

        // Draw leftover tiles in z
        if (leftoverZ > 0) {
            for (int j = 0; j < fullTilesY; j++) {
                float zStart = z1 + fullTilesZ * tileSize;
                float zEnd = zStart + leftoverZ;
                float yStart = y1 + j * tileSize;
                float yEnd = yStart + tileSize;
                float u0 = sprite.getU0();
                float v0 = sprite.getV0();
                float u1 = sprite.getU(leftoverZ);
                float v1 = sprite.getV(tileSize);
                drawReversedQuad(vertexConsumer, entry, x, yStart, zStart, x, yEnd, zEnd, u0, v0, u1, v1, color, light, overlay, nx, ny, nz);
            }
        }

        // Draw leftover tiles in y
        if (leftoverY > 0) {
            for (int i = 0; i < fullTilesZ; i++) {
                float zStart = z1 + i * tileSize;
                float zEnd = zStart + tileSize;
                float yStart = y1 + fullTilesY * tileSize;
                float yEnd = yStart + leftoverY;
                float u0 = sprite.getU0();
                float v0 = sprite.getV0();
                float u1 = sprite.getU(tileSize);
                float v1 = sprite.getV(leftoverY);
                drawReversedQuad(vertexConsumer, entry, x, yStart, zStart, x, yEnd, zEnd, u0, v0, u1, v1, color, light, overlay, nx, ny, nz);
            }

            // Draw the corner leftover tile if both leftoverZ and leftoverY > 0
            if (leftoverZ > 0) {
                float zStart = z1 + fullTilesZ * tileSize;
                float zEnd = zStart + leftoverZ;
                float yStart = y1 + fullTilesY * tileSize;
                float yEnd = yStart + leftoverY;
                float u0 = sprite.getU0();
                float v0 = sprite.getV0();
                float u1 = sprite.getU(leftoverZ);
                float v1 = sprite.getV(leftoverY);
                drawReversedQuad(vertexConsumer, entry, x, yStart, zStart, x, yEnd, zEnd, u0, v0, u1, v1, color, light, overlay, nx, ny, nz);
            }
        }
    }
}
