package dev.turtywurty.industria.molding;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;
import org.jspecify.annotations.Nullable;

public class MoldLineRenderState implements GuiElementRenderState {
    private final float x1, y1, x2, y2;
    private final float halfWidth;
    private final int color;
    private final Matrix3x2f pose;
    private final @Nullable ScreenRectangle scissorArea;
    private final RenderPipeline renderPipeline;

    public MoldLineRenderState(float x1, float y1, float x2, float y2, float halfWidth, int color, Matrix3x2fc pose, @Nullable ScreenRectangle scissorArea, RenderPipeline renderPipeline) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.halfWidth = halfWidth;
        this.color = color;
        this.pose = new Matrix3x2f(pose);
        this.scissorArea = scissorArea;
        this.renderPipeline = renderPipeline;
    }

    @Override
    public void buildVertices(VertexConsumer vertexConsumer) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float length = (float) Math.hypot(dx, dy);
        if (length == 0 || halfWidth <= 0)
            return;

        float offsetX = -dy / length * halfWidth;
        float offsetY = dx / length * halfWidth;

        float extensionX = dx / length * halfWidth;
        float extensionY = dy / length * halfWidth;
        float extent = length / (2.0f * halfWidth) + 1.0f;

        vertexConsumer.addVertexWith2DPose(this.pose, x1 - offsetX - extensionX, y1 - offsetY - extensionY)
                .setColor(this.color)
                .setUv(-extent, -1);
        vertexConsumer.addVertexWith2DPose(this.pose, x1 + offsetX - extensionX, y1 + offsetY - extensionY)
                .setColor(this.color)
                .setUv(-extent, 1);
        vertexConsumer.addVertexWith2DPose(this.pose, x2 + offsetX + extensionX, y2 + offsetY + extensionY)
                .setColor(this.color)
                .setUv(extent, 1);
        vertexConsumer.addVertexWith2DPose(this.pose, x2 - offsetX + extensionX, y2 - offsetY + extensionY)
                .setColor(this.color)
                .setUv(extent, -1);
    }

    @Override
    public RenderPipeline pipeline() {
        return this.renderPipeline;
    }

    @Override
    public TextureSetup textureSetup() {
        return TextureSetup.noTexture();
    }

    @Override
    public @Nullable ScreenRectangle scissorArea() {
        return this.scissorArea;
    }

    @Override
    public @Nullable ScreenRectangle bounds() {
        double padding = Math.sqrt(2.0) * halfWidth;
        int minX = (int) Math.floor(Math.min(x1, x2) - padding);
        int minY = (int) Math.floor(Math.min(y1, y2) - padding);
        int maxX = (int) Math.ceil(Math.max(x1, x2) + padding);
        int maxY = (int) Math.ceil(Math.max(y1, y2) + padding);
        var rectangle = new ScreenRectangle(minX, minY, maxX - minX, maxY - minY);
        rectangle = rectangle.transformMaxBounds(this.pose);
        return this.scissorArea == null
                ? rectangle
                : rectangle.intersection(this.scissorArea);
    }
}
