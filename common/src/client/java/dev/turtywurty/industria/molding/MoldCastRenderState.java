package dev.turtywurty.industria.molding;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;
import org.jspecify.annotations.Nullable;

public class MoldCastRenderState implements GuiElementRenderState {
    private final TextureAtlasSprite sprite;
    private final DynamicTexture maskTexture;
    private final int x, y, size;
    private final Matrix3x2f pose;
    private final @Nullable ScreenRectangle scissorArea;

    public MoldCastRenderState(
        TextureAtlasSprite sprite,
        DynamicTexture maskTexture,
        int x,
        int y,
        int size,
        Matrix3x2fc pose,
        @Nullable ScreenRectangle scissorArea
    ) {
        this.sprite = sprite;
        this.maskTexture = maskTexture;
        this.x = x;
        this.y = y;
        this.size = size;
        this.pose = new Matrix3x2f(pose);
        this.scissorArea = scissorArea;
    }

    @Override
    public void buildVertices(VertexConsumer vertexConsumer) {
        vertexConsumer.addVertexWith2DPose(this.pose, x, y)
            .setColor(0xFFFFFFFF)
            .setUv(sprite.getU0(), sprite.getV0())
            .setUv2(0, 0);
        vertexConsumer.addVertexWith2DPose(this.pose, x, y + size)
            .setColor(0xFFFFFFFF)
            .setUv(sprite.getU0(), sprite.getV1())
            .setUv2(0, 1);
        vertexConsumer.addVertexWith2DPose(this.pose, x + size, y + size)
            .setColor(0xFFFFFFFF)
            .setUv(sprite.getU1(), sprite.getV1())
            .setUv2(1, 1);
        vertexConsumer.addVertexWith2DPose(this.pose, x + size, y)
            .setColor(0xFFFFFFFF)
            .setUv(sprite.getU1(), sprite.getV0())
            .setUv2(1, 0);
    }

    @Override
    public RenderPipeline pipeline() {
        return MoldRenderPipelines.MOLD_CAST;
    }

    @Override
    public TextureSetup textureSetup() {
        AbstractTexture atlas = Minecraft.getInstance().getTextureManager().getTexture(this.sprite.atlasLocation());
        return TextureSetup.doubleTexture(
            atlas.getTextureView(), atlas.getSampler(),
            this.maskTexture.getTextureView(), this.maskTexture.getSampler());
    }

    @Override
    public @Nullable ScreenRectangle scissorArea() {
        return this.scissorArea;
    }

    @Override
    public @Nullable ScreenRectangle bounds() {
        var rectangle = new ScreenRectangle(this.x, this.y, this.size, this.size);
        rectangle = rectangle.transformMaxBounds(this.pose);
        return scissorArea == null ? rectangle : rectangle.intersection(scissorArea);
    }
}
