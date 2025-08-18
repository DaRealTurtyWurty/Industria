package dev.turtywurty.industria.util;

import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.function.UnaryOperator;

public class InWorldFluidRenderingComponent {

    private boolean shouldDebugAmount = false;

    public void setShouldDebugAmount(boolean shouldDebugAmount) {
        this.shouldDebugAmount = shouldDebugAmount;
    }

    public void renderFluidTank(@Nullable SingleFluidStorage fluidTank, VertexConsumerProvider vertexConsumers, MatrixStack matrices, int light, int overlay, World world, BlockPos pos, float x1, float y1, float z1, float x2, float maxHeightPixels, float z2) {
        renderFluidTank(fluidTank, vertexConsumers, matrices, light, overlay, world, pos, x1, y1, z1, x2, maxHeightPixels, z2, IndeterminateBoolean.INDETERMINATE);
    }

    public void renderFluidTank(@Nullable SingleFluidStorage fluidTank, VertexConsumerProvider vertexConsumers, MatrixStack matrices, int light, int overlay, World world, BlockPos pos, float x1, float y1, float z1, float x2, float maxHeightPixels, float z2, IndeterminateBoolean drawTopFace) {
        renderFluidTank(fluidTank, vertexConsumers, matrices, light, overlay, world, pos, x1, y1, z1, x2, maxHeightPixels, z2, 0xFFFFFFFF, ColorMode.MULTIPLICATION, drawTopFace);
    }

    public void renderFluidTank(@Nullable SingleFluidStorage fluidTank, VertexConsumerProvider vertexConsumers, MatrixStack matrices, int light, int overlay, World world, BlockPos pos, float x1, float y1, float z1, float x2, float maxHeightPixels, float z2, int color, ColorMode colorMode) {
        renderFluidTank(fluidTank, vertexConsumers, matrices, light, overlay, world, pos, x1, y1, z1, x2, maxHeightPixels, z2, color, colorMode, IndeterminateBoolean.INDETERMINATE);
    }

    public void renderFluidTank(@Nullable SingleFluidStorage fluidTank, VertexConsumerProvider vertexConsumers, MatrixStack matrices, int light, int overlay, World world, BlockPos pos, float x1, float y1, float z1, float x2, float maxHeightPixels, float z2, int color, ColorMode colorMode, IndeterminateBoolean drawTopFace) {
        // if (fluidTank == null || fluidTank.isResourceBlank() || fluidTank.amount <= 0) return;

        FluidVariant fluidVariant = FluidVariant.of(Fluids.WATER); //fluidTank.getResource();
        long amount = fluidTank.amount;
        long capacity = fluidTank.getCapacity();
        float fillPercentage = (float) (Math.sin(world.getTime() / 20.0) * 0.5 + 0.5);
        ; //(float) amount / capacity;
        fillPercentage = MathHelper.clamp(fillPercentage, 0.0F, 1.0F);

        if (this.shouldDebugAmount) {
            fillPercentage = (float) (Math.sin(world.getTime() / 64f) * 0.5 + 0.5);
        }

        int fluidColor = FluidVariantRendering.getColor(fluidVariant, world, pos);
        fluidColor = ColorMode.modifyColor(fluidColor, color, colorMode);

        Sprite stillSprite = FluidVariantRendering.getSprite(fluidVariant);
        if (stillSprite == null)
            return;

        RenderLayer renderLayer = RenderLayer.getItemEntityTranslucentCull(stillSprite.getAtlasId());
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(renderLayer);

        float y2 = ((fillPercentage * maxHeightPixels) / 16f) + y1;

        matrices.push();

        if (FluidVariantAttributes.isLighterThanAir(fluidVariant)) {
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));
        }

        int blockLight = (light >> 4) & 0xF;
        int luminosity = Math.max(blockLight, FluidVariantAttributes.getLuminance(fluidVariant));
        light = (light & 0xF00000) | (luminosity << 4);

        renderDirectionalTiledQuad(Direction.NORTH, vertexConsumer, matrices,
                x1, x2, y1, y2, z1 + 0.01f,
                stillSprite, fluidColor, light, overlay);

        renderDirectionalTiledQuad(Direction.SOUTH, vertexConsumer, matrices,
                x1, x2, y1, y2, z2 - 0.01f,
                stillSprite, fluidColor, light, overlay);

        renderDirectionalTiledQuad(Direction.WEST, vertexConsumer, matrices,
                z1, z2, y1, y2, x1 + 0.01f,
                stillSprite, fluidColor, light, overlay);

        renderDirectionalTiledQuad(Direction.EAST, vertexConsumer, matrices,
                z1, z2, y1, y2, x2 - 0.01f,
                stillSprite, fluidColor, light, overlay);

        if (drawTopFace.evaluate(fillPercentage < 1.0F))
            renderDirectionalTiledQuad(Direction.UP, vertexConsumer, matrices,
                    x1, x2, z1, z2, y2, stillSprite, fluidColor, light, overlay);


        matrices.pop();
    }

    // Calls the full renderFace with default color and color mode
    public void renderFace(Direction direction, @Nullable FluidVariant fluidVariant, VertexConsumerProvider vertexConsumers, MatrixStack matrices,
                           int light, int overlay, World world, BlockPos pos,
                           float left, float right, float up, float down, float depth,
                           UnaryOperator<RenderLayer> wrapRenderLayer) {

        renderFace(direction, fluidVariant, vertexConsumers, matrices, light, overlay, world, pos,
                left, right, up, down, depth,
                0xFFFFFFFF, ColorMode.MULTIPLICATION, wrapRenderLayer);
    }

    // Calls the full renderFace with default color, color mode, and identity render layer
    public void renderFace(Direction direction, @Nullable FluidVariant fluidVariant, VertexConsumerProvider vertexConsumers, MatrixStack matrices,
                           int light, int overlay, World world, BlockPos pos,
                           float left, float right, float up, float down, float depth) {

        renderFace(direction, fluidVariant, vertexConsumers, matrices, light, overlay, world, pos,
                left, right, up, down, depth,
                0xFFFFFFFF, ColorMode.MULTIPLICATION);
    }


    public void renderFace(Direction direction, @Nullable FluidVariant fluidVariant, VertexConsumerProvider vertexConsumers, MatrixStack matrices,
                           int light, int overlay, World world, BlockPos pos,
                           float left, float right, float up, float down, float depth,
                           int color, ColorMode colorMode) {

        renderFace(direction, fluidVariant, vertexConsumers, matrices, light, overlay, world, pos,
                left, right, up, down, depth,
                color, colorMode, renderLayer -> renderLayer);
    }


    public void renderFace(Direction direction, @Nullable FluidVariant fluidVariant, VertexConsumerProvider vertexConsumers, MatrixStack matrices, int light, int overlay, World world, BlockPos pos, float left, float right, float up, float down, float depth, int color, ColorMode colorMode, UnaryOperator<RenderLayer> wrapRenderLayer) {
        if (fluidVariant == null) return;

        Sprite stillSprite = FluidVariantRendering.getSprite(fluidVariant);
        if (stillSprite == null) return;

        int fluidColor = FluidVariantRendering.getColor(fluidVariant, world, pos);
        fluidColor = ColorMode.modifyColor(fluidColor, color, colorMode);

        RenderLayer renderLayer = RenderLayer.getItemEntityTranslucentCull(stillSprite.getAtlasId());
        renderLayer = wrapRenderLayer.apply(renderLayer);
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(renderLayer);

        matrices.push();

        if (FluidVariantAttributes.isLighterThanAir(fluidVariant)) {
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));
        }

        int blockLight = (light >> 4) & 0xF;
        int luminosity = Math.max(blockLight, FluidVariantAttributes.getLuminance(fluidVariant));
        light = (light & 0xF00000) | (luminosity << 4);

        renderDirectionalTiledQuad(direction, vertexConsumer, matrices, left, depth, up, right, down, stillSprite, fluidColor, light, overlay);

        matrices.pop();
    }

    private static void renderDirectionalTiledQuad(Direction direction, VertexConsumer vertexConsumer, MatrixStack matrices,
                                                   float left, float right, float up, float down, float depth,
                                                   Sprite sprite, int color, int light, int overlay) {

        // Swap if left > right or up > down
        if (left > right) {
            float temp = left;
            left = right;
            right = temp;
        }
        if (up > down) {
            float temp = up;
            up = down;
            down = temp;
        }

        float tileSize = 1.0f;
        float uMin = sprite.getMinU(), uMax = sprite.getMaxU();
        float vMin = sprite.getMinV(), vMax = sprite.getMaxV();

        float x1, x2, y1, y2, z1, z2;
        float uStart = uMin, uEnd = uMax, vStart = vMin, vEnd = vMax;
        Vector3f normal;

        // Coordinate and UV setup
        switch (direction) {
            case UP:
                x1 = left;
                x2 = right;
                y1 = y2 = depth;
                z1 = up;
                z2 = down;
                normal = new Vector3f(0, 1, 0);
                break;
            case DOWN:
                x1 = left;
                x2 = right;
                y1 = y2 = depth;
                z1 = down;
                z2 = up;
                normal = new Vector3f(0, -1, 0);
                uStart = uMax;
                uEnd = uMin;
                break;
            case NORTH:
                x1 = right;
                x2 = left;
                y1 = down;
                y2 = up;
                z1 = z2 = depth;
                normal = new Vector3f(0, 0, -1);
                vStart = vMax;
                vEnd = vMin;
                break;
            case SOUTH:
                x1 = left;
                x2 = right;
                y1 = down;
                y2 = up;
                z1 = z2 = depth;
                normal = new Vector3f(0, 0, 1);
                uStart = uMax;
                uEnd = uMin;
                vStart = vMax;
                vEnd = vMin;
                break;
            case WEST:
                x1 = x2 = depth;
                y1 = down;
                y2 = up;
                z1 = left;
                z2 = right;
                normal = new Vector3f(-1, 0, 0);
                vStart = vMax;
                vEnd = vMin;
                break;
            case EAST:
                x1 = x2 = depth;
                y1 = down;
                y2 = up;
                z1 = right;
                z2 = left;
                normal = new Vector3f(1, 0, 0);
                uStart = uMax;
                uEnd = uMin;
                vStart = vMax;
                vEnd = vMin;
                break;
            default:
                throw new IllegalArgumentException("Invalid direction");
        }

        // Calculate tiling
        float tileCountX = Math.max(1, Math.round(Math.abs(x2 - x1) / tileSize));
        float tileCountY = Math.max(1, Math.round(Math.abs(y2 - y1) / tileSize));
        float tileCountZ = Math.max(1, Math.round(Math.abs(z2 - z1) / tileSize));
        float tileWidthX = (x2 - x1) / tileCountX;
        float tileWidthY = (y2 - y1) / tileCountY;
        float tileWidthZ = (z2 - z1) / tileCountZ;

        MatrixStack.Entry entry = matrices.peek();




        // Draw tiles
        for (int i = 0; i < tileCountX; i++) {
            for (int j = 0; j < (int) (direction.getAxis() == Direction.Axis.Y ? tileCountZ : tileCountY); j++) {

                float xStart = x1 + i * tileWidthX, xEnd = xStart + tileWidthX;
                float yStart = y1 + j * tileWidthY, yEnd = yStart + tileWidthY;
                float zStart = z1 + j * tileWidthZ, zEnd = zStart + tileWidthZ;


                float[][] vertices;
                if (direction.getAxis() == Direction.Axis.Y) {
                    vertices = new float[][]{{xStart, y1, zStart}, {xStart, y1, zEnd}, {xEnd, y1, zEnd}, {xEnd, y1, zStart}};
                } else if (direction.getAxis() == Direction.Axis.Z) {
                    vertices = new float[][]{{xStart, yStart, z1}, {xStart, yEnd, z1}, {xEnd, yEnd, z1}, {xEnd, yStart, z1}};
                } else {
                    vertices = new float[][]{{x1, yStart, zStart}, {x1, yEnd, zStart}, {x1, yEnd, zEnd}, {x1, yStart, zEnd}};
                }

                float[][] uvs = {{uStart, vStart}, {uStart, vEnd}, {uEnd, vEnd}, {uEnd, vStart}};

                for (int k = 0; k < 4; k++) {
                    vertexConsumer.vertex(entry, vertices[k][0], vertices[k][1], vertices[k][2])
                            .color(color).texture(uvs[k][0], uvs[k][1]).light(light).overlay(overlay).normal(entry, normal);
                }
            }
        }
    }


    // For front and back (XY plane)
//    public static void drawTiledXYQuad(VertexConsumer vertexConsumer,
//                                        MatrixStack.Entry entry,
//                                        float x1, float y1, float z1,
//                                        float x2, float y2, float z2,
//                                        Sprite sprite,
//                                        int color,
//                                        int light, int overlay,
//                                        float nx, float ny, float nz) {
//        float tileSize = 1.0f;
//        int fullTilesX = (int) ((x2 - x1) / tileSize);
//        int fullTilesY = (int) ((y2 - y1) / tileSize);
//        float leftoverX = (x2 - x1) - (fullTilesX * tileSize);
//        float leftoverY = (y2 - y1) - (fullTilesY * tileSize);
//
//        // Draw full tiles
//        for (int i = 0; i < fullTilesX; i++) {
//            for (int j = 0; j < fullTilesY; j++) {
//                float xStart = x1 + i * tileSize;
//                float xEnd = xStart + tileSize;
//                float yStart = y1 + j * tileSize;
//                float yEnd = yStart + tileSize;
//                float u0 = sprite.getMinU();
//                float v0 = sprite.getMinV();
//                float u1 = sprite.getMaxU();
//                float v1 = sprite.getMaxV();
//                drawQuad(vertexConsumer, entry, xStart, yStart, z1, xEnd, yEnd, z2, u0, v0, u1, v1, color, light, overlay, nx, ny, nz);
//            }
//        }
//
//        // Draw leftover tiles in x
//        if (leftoverX > 0) {
//            for (int j = 0; j < fullTilesY; j++) {
//                float xStart = x1 + fullTilesX * tileSize;
//                float xEnd = xStart + leftoverX;
//                float yStart = y1 + j * tileSize;
//                float yEnd = yStart + tileSize;
//                float u0 = sprite.getMinU();
//                float v0 = sprite.getMinV();
//                float u1 = sprite.getFrameU(leftoverX);
//                float v1 = sprite.getFrameV(tileSize);
//                drawQuad(vertexConsumer, entry, xStart, yStart, z1, xEnd, yEnd, z2, u0, v0, u1, v1, color, light, overlay, nx, ny, nz);
//            }
//        }
//
//        // Draw leftover tiles in y
//        if (leftoverY > 0) {
//            for (int i = 0; i < fullTilesX; i++) {
//                float xStart = x1 + i * tileSize;
//                float xEnd = xStart + tileSize;
//                float yStart = y1 + fullTilesY * tileSize;
//                float yEnd = yStart + leftoverY;
//                float u0 = sprite.getMinU();
//                float v0 = sprite.getMinV();
//                float u1 = sprite.getFrameU(tileSize);
//                float v1 = sprite.getFrameV(leftoverY);
//                drawQuad(vertexConsumer, entry, xStart, yStart, z1, xEnd, yEnd, z2, u0, v0, u1, v1, color, light, overlay, nx, ny, nz);
//            }
//
//            // Draw the corner leftover tile if both leftoverX and leftoverY > 0
//            if (leftoverX > 0) {
//                float xStart = x1 + fullTilesX * tileSize;
//                float xEnd = xStart + leftoverX;
//                float yStart = y1 + fullTilesY * tileSize;
//                float yEnd = yStart + leftoverY;
//                float u0 = sprite.getMinU();
//                float v0 = sprite.getMinV();
//                float u1 = sprite.getFrameU(leftoverX);
//                float v1 = sprite.getFrameV(leftoverY);
//                drawQuad(vertexConsumer, entry, xStart, yStart, z1, xEnd, yEnd, z2, u0, v0, u1, v1, color, light, overlay, nx, ny, nz);
//            }
//        }
//    }
}
