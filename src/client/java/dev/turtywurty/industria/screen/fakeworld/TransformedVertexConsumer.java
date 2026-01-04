package dev.turtywurty.industria.screen.fakeworld;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

final class TransformedVertexConsumer implements VertexConsumer {
    private final VertexConsumer delegate;
    private final Matrix4f modelViewMatrix;
    private final Matrix3f normalMatrix;
    private final boolean overrideLight;
    private final int forcedLight;

    TransformedVertexConsumer(VertexConsumer delegate, PoseStack.Pose matrices) {
        this.delegate = delegate;
        this.modelViewMatrix = matrices.pose();
        this.normalMatrix = matrices.normal();
        this.overrideLight = false;
        this.forcedLight = 0;
    }

    TransformedVertexConsumer(VertexConsumer delegate, PoseStack.Pose matrices, int forcedLight) {
        this.delegate = delegate;
        this.modelViewMatrix = matrices.pose();
        this.normalMatrix = matrices.normal();
        this.overrideLight = true;
        this.forcedLight = forcedLight;
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        Vector4f vector4f = this.modelViewMatrix.transform(new Vector4f(x, y, z, 1.0F));
        return this.delegate.addVertex(vector4f.x(), vector4f.y(), vector4f.z());
    }

    @Override
    public VertexConsumer setColor(int red, int green, int blue, int alpha) {
        return this.delegate.setColor(red, green, blue, alpha);
    }

    @Override
    public VertexConsumer setColor(int argb) {
        return this.delegate.setColor(argb);
    }

    @Override
    public VertexConsumer setLineWidth(float width) {
        return this.delegate.setLineWidth(width);
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        return this.delegate.setUv(u, v);
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        return this.delegate.setUv1(u, v);
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        if (this.overrideLight) {
            return this.delegate.setUv2(this.forcedLight & 0xFFFF, (this.forcedLight >> 16) & 0xFFFF);
        }

        return this.delegate.setUv2(u, v);
    }

    @Override
    public VertexConsumer setNormal(float x, float y, float z) {
        Vector3f vector3f = this.normalMatrix.transform(new Vector3f(x, y, z));
        return this.delegate.setNormal(vector3f.x(), vector3f.y(), vector3f.z());
    }

    @Override
    public void addVertex(float x, float y, float z, int color, float u, float v, int overlay, int light, float normalX, float normalY, float normalZ) {
        Vector4f vector4f = this.modelViewMatrix.transform(new Vector4f(x, y, z, 1.0F));
        Vector3f vector3f = this.normalMatrix.transform(new Vector3f(normalX, normalY, normalZ));
        if (this.overrideLight) {
            light = this.forcedLight;
        }
        this.delegate.addVertex(
                vector4f.x(),
                vector4f.y(),
                vector4f.z(),
                color,
                u,
                v,
                overlay,
                light,
                vector3f.x(),
                vector3f.y(),
                vector3f.z()
        );
    }
}
