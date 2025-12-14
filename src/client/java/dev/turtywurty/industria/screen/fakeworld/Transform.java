package dev.turtywurty.industria.screen.fakeworld;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public record Transform(Vec3d translation, Quaternionf rotation, Vec3d scale, Vec3d pivot) {
    private static final Vec3d IDENTITY_SCALE = new Vec3d(1.0, 1.0, 1.0);
    private static final Vec3d DEFAULT_PIVOT = new Vec3d(0.5, 0.5, 0.5);
    public static final Transform IDENTITY = new Transform(Vec3d.ZERO, new Quaternionf(), IDENTITY_SCALE, DEFAULT_PIVOT);

    public Transform {
        translation = translation == null ? Vec3d.ZERO : translation;
        rotation = rotation == null ? new Quaternionf() : new Quaternionf(rotation);
        scale = scale == null ? IDENTITY_SCALE : scale;
        pivot = pivot == null ? DEFAULT_PIVOT : pivot;
    }

    public static Transform translate(double x, double y, double z) {
        return new Transform(new Vec3d(x, y, z), new Quaternionf(), IDENTITY_SCALE, DEFAULT_PIVOT);
    }

    public static Transform rotateDegrees(float degrees, Vec3d axis) {
        return rotateRadians((float) Math.toRadians(degrees), axis);
    }

    public static Transform rotateRadians(float radians, Vec3d axis) {
        if (axis == null) {
            return IDENTITY;
        }

        Vector3f normal = new Vector3f((float) axis.x, (float) axis.y, (float) axis.z);
        if (normal.lengthSquared() == 0) {
            return IDENTITY;
        }

        normal.normalize();
        Quaternionf quaternion = new Quaternionf().rotateAxis(radians, normal.x, normal.y, normal.z);
        return new Transform(Vec3d.ZERO, quaternion, IDENTITY_SCALE, DEFAULT_PIVOT);
    }

    public static Transform scale(double x, double y, double z) {
        return new Transform(Vec3d.ZERO, new Quaternionf(), new Vec3d(x, y, z), DEFAULT_PIVOT);
    }

    public Transform withPivot(Vec3d pivot) {
        return new Transform(this.translation, this.rotation, this.scale, pivot);
    }

    public boolean isIdentity() {
        return this == IDENTITY
                || (this.translation.equals(Vec3d.ZERO)
                && this.scale.equals(IDENTITY_SCALE)
                && this.rotation.x == 0.0F && this.rotation.y == 0.0F && this.rotation.z == 0.0F && this.rotation.w == 1.0F);
    }

    public void apply(MatrixStack matrices) {
        if (isIdentity())
            return;

        matrices.translate(this.pivot.x, this.pivot.y, this.pivot.z);
        matrices.multiply(this.rotation);
        matrices.scale((float) this.scale.x, (float) this.scale.y, (float) this.scale.z);
        matrices.translate((float) -this.pivot.x, (float) -this.pivot.y, (float) -this.pivot.z);
        matrices.translate(this.translation.x, this.translation.y, this.translation.z);
    }
}
