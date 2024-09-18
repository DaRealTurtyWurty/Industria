package dev.turtywurty.industria.util;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public abstract class DefaultScreenElement extends ScreenElement {
    private static final Vec3d CENTER = new Vec3d(0.5, 0.5, 0.5);

    protected int localX, localY, localZ;
    protected double rotationX, rotationY, rotationZ;
    protected double scale = 1.0;
    protected int color = 0xFFFFFF;
    protected Vec3d rotationPoint = Vec3d.ZERO;

    public DefaultScreenElement() {
        super(0, 0);
    }

    public <T extends DefaultScreenElement> T localPos(int localX, int localY, int localZ) {
        this.localX = localX;
        this.localY = localY;
        this.localZ = localZ;
        return (T) this;
    }

    public <T extends DefaultScreenElement> T rotation(double rotationX, double rotationY, double rotationZ) {
        this.rotationX = rotationX;
        this.rotationY = rotationY;
        this.rotationZ = rotationZ;
        return (T) this;
    }

    public <T extends DefaultScreenElement> T scale(double scale) {
        this.scale = scale;
        return (T) this;
    }

    public <T extends DefaultScreenElement> T color(int color) {
        this.color = color;
        return (T) this;
    }

    public <T extends DefaultScreenElement> T rotationPoint(Vec3d rotationPoint) {
        this.rotationPoint = rotationPoint;
        return (T) this;
    }

    public <T extends DefaultScreenElement> T rotationPoint(double x, double y, double z) {
        return rotationPoint(new Vec3d(x, y, z));
    }

    public <T extends DefaultScreenElement> T rotateAroundCenter(double rotationX, double rotationY, double rotationZ) {
        return rotation(rotationX, rotationY, rotationZ)
                .rotationPoint(CENTER);
    }

    protected void prepareMatrix(MatrixStack stack) {
        stack.push();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        DiffuseLighting.enableGuiDepthLighting();
    }

    protected void transformMatrix(MatrixStack stack) {
        stack.translate(x, y, z);
        stack.scale((float) scale, (float) scale, (float) scale);
        stack.translate(localX, localY, localZ);
        stack.multiplyPositionMatrix(new Matrix4f().scaling(-1f));
        stack.translate(rotationPoint.x, rotationPoint.y, rotationPoint.z);
        stack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) rotationZ));
        stack.multiply(RotationAxis.POSITIVE_X.rotationDegrees((float) rotationX));
        stack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) rotationY));
        stack.translate(-rotationPoint.x, -rotationPoint.y, -rotationPoint.z);
    }

    protected void cleanupMatrix(MatrixStack matrixStack) {
        matrixStack.pop();
        DiffuseLighting.enableGuiDepthLighting();
    }
}
