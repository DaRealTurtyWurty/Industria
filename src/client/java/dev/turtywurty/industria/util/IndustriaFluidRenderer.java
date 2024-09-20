package dev.turtywurty.industria.util;

import dev.turtywurty.industria.blockentity.util.fluid.FluidStack;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.*;

public class IndustriaFluidRenderer {
    public static VertexConsumer getFluidBuilder(VertexConsumerProvider provider) {
        return provider.getBuffer(IndustriaRenderLayers.getFluid());
    }

    public static void renderFluidBox(FluidState fluidState, float xMin, float yMin, float zMin, float xMax, float yMax, float zMax,
                                      VertexConsumerProvider provider, MatrixStack matrices, int light, boolean renderBottom, float red, float green, float blue, float alpha, ColorMode colorMode) {
        renderFluidBox(fluidState.getFluid(), xMin, yMin, zMin, xMax, yMax, zMax, provider, matrices, light, renderBottom, red, green, blue, alpha, colorMode);
    }

    public static void renderFluidBox(FluidState fluidState, float xMin, float yMin, float zMin, float xMax, float yMax, float zMax,
                                      VertexConsumerProvider provider, MatrixStack matrices, int light, boolean renderBottom) {
        renderFluidBox(fluidState.getFluid(), xMin, yMin, zMin, xMax, yMax, zMax, provider, matrices, light, renderBottom);
    }

    public static void renderFluidBox(Fluid fluid, float xMin, float yMin, float zMin, float xMax, float yMax, float zMax,
                                      VertexConsumerProvider provider, MatrixStack matrices, int light, boolean renderBottom) {
        renderFluidBox(FluidVariant.of(fluid), xMin, yMin, zMin, xMax, yMax, zMax, getFluidBuilder(provider), matrices, light, renderBottom, 1, 1, 1, 1, ColorMode.MULTIPLICATION);
    }

    public static void renderFluidBox(Fluid fluid, float xMin, float yMin, float zMin, float xMax, float yMax, float zMax,
                                      VertexConsumerProvider provider, MatrixStack matrices, int light, boolean renderBottom, float red, float green, float blue, float alpha, ColorMode colorMode) {
        renderFluidBox(FluidVariant.of(fluid), xMin, yMin, zMin, xMax, yMax, zMax, getFluidBuilder(provider), matrices, light, renderBottom, red, green, blue, alpha, colorMode);
    }

    public static void renderFluidBox(FluidStack fluidStack, float xMin, float yMin, float zMin, float xMax, float yMax, float zMax,
                                      VertexConsumerProvider provider, MatrixStack matrices, int light, boolean renderBottom) {
        renderFluidBox(fluidStack, xMin, yMin, zMin, xMax, yMax, zMax, provider, matrices, light, renderBottom, 1, 1, 1, 1, ColorMode.MULTIPLICATION);
    }

    public static void renderFluidBox(FluidStack fluidStack, float xMin, float yMin, float zMin, float xMax, float yMax, float zMax,
                                      VertexConsumerProvider provider, MatrixStack matrices, int light, boolean renderBottom, float red, float green, float blue, float alpha, ColorMode colorMode) {
        renderFluidBox(fluidStack.fluid(), xMin, yMin, zMin, xMax, yMax, zMax, getFluidBuilder(provider), matrices, light, renderBottom, red, green, blue, alpha, colorMode);
    }

    public static void renderFluidBox(FluidVariant fluidVariant, float xMin, float yMin, float zMin, float xMax, float yMax, float zMax,
                                      VertexConsumer builder, MatrixStack matrices, int light, boolean renderBottom, float red, float green, float blue, float alpha, ColorMode colorMode) {
        Sprite[] sprites = FluidVariantRendering.getSprites(fluidVariant);
        Sprite fluidTexture = sprites != null ? sprites[0] : null;
        if (fluidTexture == null)
            return;

        int color = FluidVariantRendering.getColor(fluidVariant);
        float r = (color >> 16 & 0xFF) / 255.0F;
        float g = (color >> 8 & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;
        float a = (color >> 24 & 0xFF) / 255.0F;

        switch (colorMode) {
            case ADDITION:
                red += r;
                green += g;
                blue += b;
                alpha += a;
                break;
            case MULTIPLICATION:
                red *= r;
                green *= g;
                blue *= b;
                alpha *= a;
                break;
            case SUBTRACTION:
                red -= r;
                green -= g;
                blue -= b;
                alpha -= a;
                break;
            case DIVISION:
                red /= r;
                green /= g;
                blue /= b;
                alpha /= a;
                break;
        }

        int blockLight = (light >> 4) & 0xF;
        int luminosity = Math.max(blockLight, FluidVariantAttributes.getLuminance(fluidVariant));
        light = (light & 0xF00000) | (luminosity << 4);

        Vec3d center = new Vec3d((xMin + xMax) / 2, (yMin + yMax) / 2, (zMin + zMax) / 2); // TODO: Possibly wrong
        matrices.push();

        if (FluidVariantAttributes.isLighterThanAir(fluidVariant)) {
            matrices.translate(center.x, center.y, center.z);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));
            matrices.translate(-center.x, -center.y, -center.z);
        }

        for (Direction side : Direction.values()) {
            if (side == Direction.DOWN && !renderBottom)
                continue;

            boolean positive = side.getDirection() == Direction.AxisDirection.POSITIVE;
            if (side.getAxis().isHorizontal()) {
                if (side.getAxis() == Direction.Axis.X) {
                    renderStillTiledFace(side, zMin, yMin, zMax, yMax, positive ? xMax : xMin, builder, matrices, light, red, green, blue, alpha, fluidTexture);
                } else {
                    renderStillTiledFace(side, xMin, yMin, xMax, yMax, positive ? zMax : zMin, builder, matrices, light, red, green, blue, alpha, fluidTexture);
                }
            } else {
                renderStillTiledFace(side, xMin, zMin, xMax, zMax, positive ? yMax : yMin, builder, matrices, light, red, green, blue, alpha, fluidTexture);
            }
        }

        matrices.pop();
    }

    public static void renderStillTiledFace(Direction dir, float left, float down, float right, float up, float depth,
                                            VertexConsumer builder, MatrixStack matrices, int light, float red, float green, float blue, float alpha, Sprite texture) {
        renderTiledFace(dir, left, down, right, up, depth, builder, matrices, light, red, green, blue, alpha, texture, 1);
    }

    public static void renderTiledFace(Direction dir, float left, float down, float right, float up, float depth,
                                       VertexConsumer builder, MatrixStack matrices, int light, float red, float green, float blue, float alpha, Sprite texture, float textureScale) {
        boolean positive = dir.getDirection() == Direction.AxisDirection.POSITIVE;
        boolean horizontal = dir.getAxis().isHorizontal();
        boolean x = dir.getAxis() == Direction.Axis.X;

        float shrink = texture.getAnimationFrameDelta() * 0.5f * textureScale;
        float centerU = texture.getFrameU(0.5f * textureScale);
        float centerV = texture.getFrameV(0.5f * textureScale);

        float f;
        float x2;
        float y2;
        float u1, u2;
        float v1, v2;
        for (float x1 = left; x1 < right; x1 = x2) {
            f = MathHelper.floor(x1);
            x2 = Math.min(f + 1, right);
            if (dir == Direction.NORTH || dir == Direction.EAST) {
                f = MathHelper.ceil(x2);
                u1 = texture.getFrameU((f - x2) * textureScale);
                u2 = texture.getFrameU((f - x1) * textureScale);
            } else {
                u1 = texture.getFrameU((x1 - f) * textureScale);
                u2 = texture.getFrameU((x2 - f) * textureScale);
            }

            u1 = MathHelper.lerp(shrink, u1, centerU);
            u2 = MathHelper.lerp(shrink, u2, centerU);
            for (float y1 = down; y1 < up; y1 = y2) {
                f = MathHelper.floor(y1);
                y2 = Math.min(f + 1, up);
                if (dir == Direction.UP) {
                    v1 = texture.getFrameV((y1 - f) * textureScale);
                    v2 = texture.getFrameV((y2 - f) * textureScale);
                } else {
                    f = MathHelper.ceil(y2);
                    v1 = texture.getFrameV((f - y2) * textureScale);
                    v2 = texture.getFrameV((f - y1) * textureScale);
                }
                v1 = MathHelper.lerp(shrink, v1, centerV);
                v2 = MathHelper.lerp(shrink, v2, centerV);

                if (horizontal) {
                    if (x) {
                        addVertex(builder, matrices, depth, y2, positive ? x2 : x1, red, green, blue, alpha, u1, v1, dir, light);
                        addVertex(builder, matrices, depth, y1, positive ? x2 : x1, red, green, blue, alpha, u1, v2, dir, light);
                        addVertex(builder, matrices, depth, y1, positive ? x1 : x2, red, green, blue, alpha, u2, v2, dir, light);
                        addVertex(builder, matrices, depth, y2, positive ? x1 : x2, red, green, blue, alpha, u2, v1, dir, light);
                    } else {
                        addVertex(builder, matrices, positive ? x1 : x2, y2, depth, red, green, blue, alpha, u1, v1, dir, light);
                        addVertex(builder, matrices, positive ? x1 : x2, y1, depth, red, green, blue, alpha, u1, v2, dir, light);
                        addVertex(builder, matrices, positive ? x2 : x1, y1, depth, red, green, blue, alpha, u2, v2, dir, light);
                        addVertex(builder, matrices, positive ? x2 : x1, y2, depth, red, green, blue, alpha, u2, v1, dir, light);
                    }
                } else {
                    addVertex(builder, matrices, x1, depth, positive ? y1 : y2, red, green, blue, alpha, u1, v1, dir, light);
                    addVertex(builder, matrices, x1, depth, positive ? y2 : y1, red, green, blue, alpha, u1, v2, dir, light);
                    addVertex(builder, matrices, x2, depth, positive ? y2 : y1, red, green, blue, alpha, u2, v2, dir, light);
                    addVertex(builder, matrices, x2, depth, positive ? y1 : y2, red, green, blue, alpha, u2, v1, dir, light);
                }
            }
        }
    }

    private static void addVertex(VertexConsumer builder, MatrixStack matrices, float x, float y, float z, float red, float green, float blue, float alpha,
                                  float u, float v, Direction face, int light) {
        Vec3i normal = face.getVector();
        MatrixStack.Entry peek = matrices.peek();

        builder.vertex(peek.getPositionMatrix(), x, y, z)
                .color(red, green, blue, alpha)
                .texture(u, v)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(light)
                .normal(peek, normal.getX(), normal.getY(), normal.getZ());
    }

    public enum ColorMode {
        ADDITION,
        MULTIPLICATION,
        SUBTRACTION,
        DIVISION
    }
}