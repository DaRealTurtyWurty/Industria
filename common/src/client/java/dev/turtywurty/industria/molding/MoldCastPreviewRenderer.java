package dev.turtywurty.industria.molding;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Mth;

public class MoldCastPreviewRenderer extends PictureInPictureRenderer<MoldCastPreviewState> {
    private static final int GRID_SIZE = MoldCastSurface.RESOLUTION;
    private MoldCastSurface.Snapshot lastSurface;
    private TextureAtlasSprite lastSprite;

    public MoldCastPreviewRenderer() {
    }

    @Override
    public Class<MoldCastPreviewState> getRenderStateClass() {
        return MoldCastPreviewState.class;
    }

    @Override
    protected void renderToTexture(
        MoldCastPreviewState moldCastPreviewState,
        PoseStack poseStack,
        SubmitNodeCollector collector
    ) {
        TextureAtlasSprite sprite = moldCastPreviewState.getSprite();
        MoldCastSurface.Snapshot surface = moldCastPreviewState.surface();
        collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucentEmissive(sprite.atlasLocation()),
            (pose, buffer) -> {
                for (int row = 0; row < GRID_SIZE; row++) {
                    for (int column = 0; column < GRID_SIZE; column++) {
                        if (surface.removed(column, row))
                            continue;

                        triangle(buffer, pose, sprite, surface,
                            column, row, column, row + 1, column + 1, row + 1);
                        triangle(buffer, pose, sprite, surface,
                            column, row, column + 1, row + 1, column + 1, row);
                    }
                }
            });
        this.lastSurface = surface;
        this.lastSprite = sprite;
    }

    private static void triangle(
        VertexConsumer buffer,
        PoseStack.Pose pose,
        TextureAtlasSprite sprite,
        MoldCastSurface.Snapshot surface,
        int ax,
        int ay,
        int bx,
        int by,
        int cx,
        int cy
    ) {
        float abx = surface.positionX(bx, by) - surface.positionX(ax, ay);
        float aby = surface.positionY(bx, by) - surface.positionY(ax, ay);
        float abz = surface.depth(bx, by) - surface.depth(ax, ay);
        float acx = surface.positionX(cx, cy) - surface.positionX(ax, ay);
        float acy = surface.positionY(cx, cy) - surface.positionY(ax, ay);
        float acz = surface.depth(cx, cy) - surface.depth(ax, ay);

        float nx = aby * acz - abz * acy;
        float ny = abz * acx - abx * acz;
        float nz = abx * acy - aby * acx;
        float inverseLength = 1.0f / (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        nx *= inverseLength;
        ny *= inverseLength;

        float wallFacing = MoldCastLighting.wallFacing(nx, ny);
        vertex(buffer, pose, sprite, surface, ax, ay, wallFacing);
        vertex(buffer, pose, sprite, surface, bx, by, wallFacing);
        vertex(buffer, pose, sprite, surface, cx, cy, wallFacing);
        vertex(buffer, pose, sprite, surface, cx, cy, wallFacing);
    }

    private static void vertex(
        VertexConsumer buffer,
        PoseStack.Pose pose,
        TextureAtlasSprite sprite,
        MoldCastSurface.Snapshot surface,
        int column,
        int row,
        float wallFacing
    ) {
        float x = column / (float) GRID_SIZE;
        float y = row / (float) GRID_SIZE;
        float visibility = surface.illumination(column, row);
        int shade = Math.round(255 * MoldCastLighting.brightness(wallFacing, visibility));
        int overlay = OverlayTexture.pack(MoldCastLighting.highlight(wallFacing, visibility), false);

        buffer.addVertex(pose, surface.positionX(column, row) - 0.5f,
            surface.positionY(column, row) - 0.5f, surface.depth(column, row))
            .setColor(shade, shade, shade, 255)
            .setUv(Mth.lerp(x, sprite.getU0(), sprite.getU1()), Mth.lerp(y, sprite.getV0(), sprite.getV1()))
            .setOverlay(overlay)
            .setLight(0xF000F0)
            .setNormal(pose, 0, 0, -1);
    }

    @Override
    protected boolean textureIsReadyToBlit(MoldCastPreviewState state) {
        return state.surface() == this.lastSurface && state.getSprite() == this.lastSprite;
    }

    @Override
    protected float getTranslateY(int height, int guiScale) {
        return height / 2.0f;
    }

    @Override
    protected String getTextureLabel() {
        return "industria:mold_cast_preview";
    }
}
