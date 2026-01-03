package dev.turtywurty.industria.screen.fakeworld;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

final class TransformedVertexConsumer implements VertexConsumer {
    private final VertexConsumer delegate;
    private final Matrix4f modelViewMatrix;
    private final Matrix3f normalMatrix;

    TransformedVertexConsumer(VertexConsumer delegate, MatrixStack.Entry matrices) {
        this.delegate = delegate;
        this.modelViewMatrix = matrices.getPositionMatrix();
        this.normalMatrix = matrices.getNormalMatrix();
    }

    @Override
    public VertexConsumer vertex(float x, float y, float z) {
        Vector4f vector4f = this.modelViewMatrix.transform(new Vector4f(x, y, z, 1.0F));
        return this.delegate.vertex(vector4f.x(), vector4f.y(), vector4f.z());
    }

    @Override
    public VertexConsumer color(int red, int green, int blue, int alpha) {
        return this.delegate.color(red, green, blue, alpha);
    }

    @Override
    public VertexConsumer color(int argb) {
        return this.delegate.color(argb);
    }

    @Override
    public VertexConsumer lineWidth(float width) {
        return this.delegate.lineWidth(width);
    }

    @Override
    public VertexConsumer texture(float u, float v) {
        return this.delegate.texture(u, v);
    }

    @Override
    public VertexConsumer overlay(int u, int v) {
        return this.delegate.overlay(u, v);
    }

    @Override
    public VertexConsumer light(int u, int v) {
        return this.delegate.light(u, v);
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        Vector3f vector3f = this.normalMatrix.transform(new Vector3f(x, y, z));
        return this.delegate.normal(vector3f.x(), vector3f.y(), vector3f.z());
    }

    @Override
    public void vertex(float x, float y, float z, int color, float u, float v, int overlay, int light, float normalX, float normalY, float normalZ) {
        Vector4f vector4f = this.modelViewMatrix.transform(new Vector4f(x, y, z, 1.0F));
        Vector3f vector3f = this.normalMatrix.transform(new Vector3f(normalX, normalY, normalZ));
        this.delegate.vertex(
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
