package dev.turtywurty.industria.util.math;

import net.minecraft.util.Mth;
import org.jbox2d.common.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class BoundingBox {
    private Vec3 center;
    private Vec3 halfSize;

    private Vec3 up;
    private Vec3 right;
    private Vec3 forward;

    public BoundingBox(@NotNull Vec3 center, @NotNull Vec3 halfSize, @NotNull Vec3 up, @NotNull Vec3 right, @NotNull Vec3 forward) {
        Objects.requireNonNull(center, "Center cannot be null");
        Objects.requireNonNull(halfSize, "HalfSize cannot be null");
        Objects.requireNonNull(up, "Up vector cannot be null");
        Objects.requireNonNull(right, "Right vector cannot be null");
        Objects.requireNonNull(forward, "Forward vector cannot be null");
        if (halfSize.x <= 0 || halfSize.y <= 0 || halfSize.z <= 0)
            throw new IllegalArgumentException("HalfSize must have positive dimensions");

        this.center = copy(center);
        this.halfSize = copy(halfSize);

        this.up = copy(up);
        this.right = copy(right);
        this.forward = copy(forward);
        orthonormalizeBasis();
    }

    public Vec3[] getAxes() {
        return new Vec3[]{copy(up), copy(right), copy(forward)};
    }

    public Vec3 getCenter() {
        return copy(center);
    }

    public Vec3 getHalfSize() {
        return copy(halfSize);
    }

    public Vec3 getRight() {
        return copy(right);
    }

    public Vec3 getUp() {
        return copy(up);
    }

    public Vec3 getForward() {
        return copy(forward);
    }

    public void setCenter(@NotNull Vec3 center) {
        Objects.requireNonNull(center, "Center cannot be null");
        this.center = copy(center);
    }

    public void setHalfSize(@NotNull Vec3 halfSize) {
        Objects.requireNonNull(halfSize, "HalfSize cannot be null");
        if (halfSize.x <= 0 || halfSize.y <= 0 || halfSize.z <= 0)
            throw new IllegalArgumentException("HalfSize must have positive dimensions");

        this.halfSize = copy(halfSize);
    }

    public void setOrientation(@NotNull Vec3 up, @NotNull Vec3 right, @NotNull Vec3 forward) {
        Objects.requireNonNull(up, "Up vector cannot be null");
        Objects.requireNonNull(right, "Right vector cannot be null");
        Objects.requireNonNull(forward, "Forward vector cannot be null");

        this.up = copy(up);
        this.right = copy(right);
        this.forward = copy(forward);
        orthonormalizeBasis();
    }

    public Vec3[] getCorners() {
        Vec3 localX = mul(right, halfSize.x);
        Vec3 localY = mul(up, halfSize.y);
        Vec3 localZ = mul(forward, halfSize.z);

        return new Vec3[]{
                add(center, add(add(localX, localY), localZ)),
                add(center, add(add(localX, localY), neg(localZ))),
                add(center, add(add(localX, neg(localY)), localZ)),
                add(center, add(add(localX, neg(localY)), neg(localZ))),
                add(center, add(add(neg(localX), localY), localZ)),
                add(center, add(add(neg(localX), localY), neg(localZ))),
                add(center, add(add(neg(localX), neg(localY)), localZ)),
                add(center, add(add(neg(localX), neg(localY)), neg(localZ)))
        };
    }

    public boolean containsPoint(@NotNull Vec3 point) {
        Objects.requireNonNull(point, "Point cannot be null");

        Vec3 localPoint = sub(point, center);
        float x = dot(localPoint, right);
        float y = dot(localPoint, up);
        float z = dot(localPoint, forward);

        final float epsilon = 1e-6f;

        return Math.abs(x) <= halfSize.x + epsilon &&
                Math.abs(y) <= halfSize.y + epsilon &&
                Math.abs(z) <= halfSize.z + epsilon;
    }

    public Vec3 closestPointOnBox(@NotNull Vec3 point) {
        Objects.requireNonNull(point, "Point cannot be null");

        Vec3 localPoint = worldToLocal(point);
        var closest = new Vec3(
                Math.clamp(localPoint.x, -halfSize.x, halfSize.x),
                Math.clamp(localPoint.y, -halfSize.y, halfSize.y),
                Math.clamp(localPoint.z, -halfSize.z, halfSize.z)
        );

        return localToWorld(closest);
    }

    public float distanceToBox(@NotNull Vec3 point) {
        Objects.requireNonNull(point, "Point cannot be null");

        Vec3 localPoint = worldToLocal(point);
        float dx = excess(localPoint.x, halfSize.x);
        float dy = excess(localPoint.y, halfSize.y);
        float dz = excess(localPoint.z, halfSize.z);

        return Mth.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * Ray/OBB intersection test using slab method in box local space.
     *
     * @param rayOrigin world-space origin
     * @param rayDir    world-space direction (need not be normalized)
     * @return true if the ray hits the box
     */
    public boolean intersectsRay(@NotNull Vec3 rayOrigin, @NotNull Vec3 rayDir) {
        // Transform into local space where box is axis-aligned
        Vec3 o = worldToLocal(rayOrigin);
        // Project direction into local space via dot with each axis
        float dx = dot(rayDir, right);
        float dy = dot(rayDir, up);
        float dz = dot(rayDir, forward);

        float tmin = -Float.MAX_VALUE;
        float tmax = Float.MAX_VALUE;
        final float epsilon = 1e-8f;

        // X slab
        if (Math.abs(dx) < epsilon) {
            if (o.x < -halfSize.x || o.x > halfSize.x) return false;
        } else {
            float inv = 1.0f / dx;
            float t1 = (-halfSize.x - o.x) * inv;
            float t2 = (halfSize.x - o.x) * inv;
            if (t1 > t2) {
                float tmp = t1;
                t1 = t2;
                t2 = tmp;
            }
            tmin = Math.max(tmin, t1);
            tmax = Math.min(tmax, t2);
            if (tmin > tmax) return false;
        }

        // Y slab
        if (Math.abs(dy) < epsilon) {
            if (o.y < -halfSize.y || o.y > halfSize.y) return false;
        } else {
            float inv = 1.0f / dy;
            float t1 = (-halfSize.y - o.y) * inv;
            float t2 = (halfSize.y - o.y) * inv;
            if (t1 > t2) {
                float tmp = t1;
                t1 = t2;
                t2 = tmp;
            }
            tmin = Math.max(tmin, t1);
            tmax = Math.min(tmax, t2);
            if (tmin > tmax) return false;
        }

        // Z slab
        if (Math.abs(dz) < epsilon) {
            if (o.z < -halfSize.z || o.z > halfSize.z) return false;
        } else {
            float inv = 1.0f / dz;
            float t1 = (-halfSize.z - o.z) * inv;
            float t2 = (halfSize.z - o.z) * inv;
            if (t1 > t2) {
                float tmp = t1;
                t1 = t2;
                t2 = tmp;
            }
            tmin = Math.max(tmin, t1);
            tmax = Math.min(tmax, t2);
            if (tmin > tmax) return false;
        }

        return !(tmax < 0f); // box is behind the ray
    }

    public boolean intersects(@NotNull BoundingBox other) {
        // Axes
        Vec3[] A = {right, up, forward};
        Vec3[] B = {other.right, other.up, other.forward};
        float[] Ea = {halfSize.x, halfSize.y, halfSize.z};
        float[] Eb = {other.halfSize.x, other.halfSize.y, other.halfSize.z};

        // Rotation matrix R and |R| with epsilon
        float[][] R = new float[3][3];
        float[][] AbsR = new float[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                R[i][j] = dot(A[i], B[j]);
                AbsR[i][j] = Math.abs(R[i][j]) + 1e-6f; // add epsilon to handle near-parallel axes
            }
        }

        // Translation vector t in A's local frame
        Vec3 tVec = sub(other.center, this.center);
        float[] t = new float[]{dot(tVec, A[0]), dot(tVec, A[1]), dot(tVec, A[2])};

        float ra, rb;

        // Test A's axes
        for (int i = 0; i < 3; i++) {
            ra = Ea[i];
            rb = Eb[0] * AbsR[i][0] + Eb[1] * AbsR[i][1] + Eb[2] * AbsR[i][2];
            if (Math.abs(t[i]) > ra + rb) return false;
        }

        // Test B's axes
        for (int j = 0; j < 3; j++) {
            ra = Ea[0] * AbsR[0][j] + Ea[1] * AbsR[1][j] + Ea[2] * AbsR[2][j];
            rb = Eb[j];
            float tj = Math.abs(t[0] * R[0][j] + t[1] * R[1][j] + t[2] * R[2][j]);
            if (tj > ra + rb) return false;
        }

        // Test cross products A_i x B_j
        // i = 0..2, j = 0..2
        // Each axis formula derived from Gottschalk et al. (OBBTree)
        // L = A0 x B0
        ra = Ea[1] * AbsR[2][0] + Ea[2] * AbsR[1][0];
        rb = Eb[1] * AbsR[0][2] + Eb[2] * AbsR[0][1];
        if (Math.abs(t[2] * R[1][0] - t[1] * R[2][0]) > ra + rb) return false;

        // L = A0 x B1
        ra = Ea[1] * AbsR[2][1] + Ea[2] * AbsR[1][1];
        rb = Eb[0] * AbsR[0][2] + Eb[2] * AbsR[0][0];
        if (Math.abs(t[2] * R[1][1] - t[1] * R[2][1]) > ra + rb) return false;

        // L = A0 x B2
        ra = Ea[1] * AbsR[2][2] + Ea[2] * AbsR[1][2];
        rb = Eb[0] * AbsR[0][1] + Eb[1] * AbsR[0][0];
        if (Math.abs(t[2] * R[1][2] - t[1] * R[2][2]) > ra + rb) return false;

        // L = A1 x B0
        ra = Ea[0] * AbsR[2][0] + Ea[2] * AbsR[0][0];
        rb = Eb[1] * AbsR[1][2] + Eb[2] * AbsR[1][1];
        if (Math.abs(t[0] * R[2][0] - t[2] * R[0][0]) > ra + rb) return false;

        // L = A1 x B1
        ra = Ea[0] * AbsR[2][1] + Ea[2] * AbsR[0][1];
        rb = Eb[0] * AbsR[1][2] + Eb[2] * AbsR[1][0];
        if (Math.abs(t[0] * R[2][1] - t[2] * R[0][1]) > ra + rb) return false;

        // L = A1 x B2
        ra = Ea[0] * AbsR[2][2] + Ea[2] * AbsR[0][2];
        rb = Eb[0] * AbsR[1][1] + Eb[1] * AbsR[1][0];
        if (Math.abs(t[0] * R[2][2] - t[2] * R[0][2]) > ra + rb) return false;

        // L = A2 x B0
        ra = Ea[0] * AbsR[1][0] + Ea[1] * AbsR[0][0];
        rb = Eb[1] * AbsR[2][2] + Eb[2] * AbsR[2][1];
        if (Math.abs(t[1] * R[0][0] - t[0] * R[1][0]) > ra + rb) return false;

        // L = A2 x B1
        ra = Ea[0] * AbsR[1][1] + Ea[1] * AbsR[0][1];
        rb = Eb[0] * AbsR[2][2] + Eb[2] * AbsR[2][0];
        if (Math.abs(t[1] * R[0][1] - t[0] * R[1][1]) > ra + rb) return false;

        // L = A2 x B2
        ra = Ea[0] * AbsR[1][2] + Ea[1] * AbsR[0][2];
        rb = Eb[0] * AbsR[2][1] + Eb[1] * AbsR[2][0];
        if (Math.abs(t[1] * R[0][2] - t[0] * R[1][2]) > ra + rb) return false;

        return true; // no separating axis found
    }

    public BoundingBox translated(@NotNull Vec3 translation) {
        Objects.requireNonNull(translation, "Translation cannot be null");
        return new BoundingBox(add(center, translation), halfSize, up, right, forward);
    }

    public BoundingBox expand(float dx, float dy, float dz) {
        final float epsilon = 1e-6f;
        var halfSize = new Vec3(
                Math.max(epsilon, this.halfSize.x + dx),
                Math.max(epsilon, this.halfSize.y + dy),
                Math.max(epsilon, this.halfSize.z + dz)
        );

        return new BoundingBox(this.center, halfSize, this.up, this.right, this.forward);
    }

    public BoundingBox rotateAroundCenter(@NotNull Vec3 axis, float angleRad) {
        Vec3 normalized = normalize(axis);
        if (lengthSq(normalized) < 1e-12f || Math.abs(angleRad) < 1e-12f) return this; // no-op

        Vec3 r2 = rodrigues(this.right, normalized, angleRad);
        Vec3 u2 = rodrigues(this.up, normalized, angleRad);
        Vec3 f2 = rodrigues(this.forward, normalized, angleRad);
        return new BoundingBox(this.center, this.halfSize, u2, r2, f2);
    }

    public float volume() {
        return 8f * halfSize.x * halfSize.y * halfSize.z;
    }

    public float surfaceArea() {
        float a = halfSize.x * 2f, b = halfSize.y * 2f, c = halfSize.z * 2f;
        return 2f * (a * b + a * c + b * c);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof BoundingBox bb)) return false;
        return approxEqVec(center, bb.center) &&
                approxEqVec(halfSize, bb.halfSize) &&
                approxEqVec(up, bb.up) &&
                approxEqVec(right, bb.right) &&
                approxEqVec(forward, bb.forward);
    }

    @Override
    public int hashCode() {
        int h = 17;
        h = 31 * h + hashVec(center);
        h = 31 * h + hashVec(halfSize);
        h = 31 * h + hashVec(up);
        h = 31 * h + hashVec(right);
        h = 31 * h + hashVec(forward);
        return h;
    }

    @Override
    public String toString() {
        return "BoundingBox{" +
                "center=" + vecStr(center) +
                ", halfSize=" + vecStr(halfSize) +
                ", right=" + vecStr(right) +
                ", up=" + vecStr(up) +
                ", forward=" + vecStr(forward) +
                '}';
    }

    private void orthonormalizeBasis() {
        final float epsilon = 1e-12f;
        // Start with 'right' as primary axis
        Vec3 normRight = normalize(this.right);
        if (lengthSq(normRight) < epsilon) normRight = new Vec3(1, 0, 0);

        // Make 'up' orthogonal to normRight
        Vec3 normUp = sub(this.up, mul(normRight, dot(this.up, normRight)));
        normUp = normalize(normUp);
        if (lengthSq(normUp) < epsilon) {
            // pick any vector not parallel to normRight
            var tmp = Math.abs(normRight.x) < 0.9f ?
                    new Vec3(1, 0, 0) :
                    new Vec3(0, 1, 0);
            normUp = normalize(sub(tmp, mul(normRight, dot(tmp, normRight))));
        }

        // Compute forward to ensure right-handedness
        Vec3 normForward = cross(normRight, normUp);
        normForward = normalize(normForward);

        // Rebuild normUp to be perfectly orthogonal (optional but nice)
        normUp = cross(normForward, normRight);

        this.right = normRight;
        this.up = normUp;
        this.forward = normForward;
    }

    private Vec3 worldToLocal(Vec3 point) {
        Vec3 v = sub(point, center);
        return new Vec3(dot(v, right), dot(v, up), dot(v, forward));
    }

    private Vec3 localToWorld(Vec3 point) {
        // center + q.x*right + q.y*up + q.z*forward
        return add(center, add(add(mul(right, point.x), mul(up, point.y)), mul(forward, point.z)));
    }

    private static Vec3 copy(Vec3 vec) {
        return new Vec3(vec.x, vec.y, vec.z);
    }

    private static Vec3 add(Vec3 vec1, Vec3 vec2) {
        return new Vec3(vec1.x + vec2.x, vec1.y + vec2.y, vec1.z + vec2.z);
    }

    private static Vec3 sub(Vec3 vec1, Vec3 vec2) {
        return new Vec3(vec1.x - vec2.x, vec1.y - vec2.y, vec1.z - vec2.z);
    }

    private static Vec3 neg(Vec3 vec) {
        return new Vec3(-vec.x, -vec.y, -vec.z);
    }

    private static Vec3 mul(Vec3 vec, float scale) {
        return new Vec3(vec.x * scale, vec.y * scale, vec.z * scale);
    }

    private static float dot(Vec3 vec1, Vec3 vec2) {
        return vec1.x * vec2.x + vec1.y * vec2.y + vec1.z * vec2.z;
    }

    private static Vec3 cross(Vec3 vec1, Vec3 vec2) {
        return new Vec3(
                vec1.y * vec2.z - vec1.z * vec2.y,
                vec1.z * vec2.x - vec1.x * vec2.z,
                vec1.x * vec2.y - vec1.y * vec2.x
        );
    }

    private static float lengthSq(Vec3 vec) {
        return dot(vec, vec);
    }

    private static float length(Vec3 vec) {
        return (float) Math.sqrt(lengthSq(vec));
    }

    private static Vec3 normalize(Vec3 vec) {
        float length = length(vec);
        if (length < 1e-12f) return new Vec3(0, 0, 0);
        float inverse = 1.0f / length;
        return new Vec3(vec.x * inverse, vec.y * inverse, vec.z * inverse);
    }

    private static float excess(float v, float h) {
        float a = Math.abs(v) - h;
        return a > 0 ? a : 0f;
    }

    private static Vec3 rodrigues(Vec3 vec, Vec3 axisUnit, float angle) {
        Objects.requireNonNull(vec, "Vector v cannot be null");
        Objects.requireNonNull(axisUnit, "Axis vector cannot be null");

        // v_rot = v*c + (axis × v)*s + axis*(axis·v)*(1-c)
        float c = (float) Math.cos(angle);
        float s = (float) Math.sin(angle);
        Vec3 term1 = mul(vec, c);
        Vec3 term2 = mul(cross(axisUnit, vec), s);
        Vec3 term3 = mul(axisUnit, (1 - c) * dot(axisUnit, vec));
        return add(add(term1, term2), term3);
    }

    private static boolean approxEqVec(Vec3 a, Vec3 b) {
        Objects.requireNonNull(a, "Vector a cannot be null");
        Objects.requireNonNull(b, "Vector b cannot be null");

        final float epsilon = 1e-5f;
        return Math.abs(a.x - b.x) < epsilon &&
                Math.abs(a.y - b.y) < epsilon &&
                Math.abs(a.z - b.z) < epsilon;
    }

    private static int hashVec(Vec3 v) {
        Objects.requireNonNull(v, "Vector cannot be null");

        int h = Float.hashCode(v.x);
        h = 31 * h + Float.hashCode(v.y);
        h = 31 * h + Float.hashCode(v.z);
        return h;
    }

    private static String vecStr(Vec3 v) {
        Objects.requireNonNull(v, "Vector cannot be null");

        return "(" + v.x + ", " + v.y + ", " + v.z + ')';
    }
}
