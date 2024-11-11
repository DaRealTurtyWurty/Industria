package dev.turtywurty.industria.util;

import net.minecraft.block.BlockState;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;
import org.joml.Math;
import org.joml.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public interface WireframeExtractor {
    static List<Line> fromBakedModel(BakedModel model, @Nullable BlockState state, Random random) {
        Set<Line> lines = new HashSet<>(); // We are using a set to avoid duplicate lines (hashes are used to determine if a line is a duplicate)
        for (Direction direction : Direction.values()) {
            for (BakedQuad quad : model.getQuads(state, direction, random)) {
                extractVerticesFromBakedQuad(lines, quad);
            }
        }

        for (BakedQuad quad : model.getQuads(state, null, random)) {
            extractVerticesFromBakedQuad(lines, quad);
        }

        return new ArrayList<>(lines);
    }

    static void renderFromModelParts(List<ModelPart> modelParts, MatrixStack matrices, VertexConsumer vertexConsumer) {
        Vector3f v0 = new Vector3f();
        Vector3f v1 = new Vector3f();
        Vector3f v2 = new Vector3f();
        Vector3f v3 = new Vector3f();
        Vector4f pos = new Vector4f();
        Vector3f normal = new Vector3f();
        for (ModelPart modelPart : modelParts) {
            visitPart(modelPart, matrices, vertexConsumer, v0, v1, v2, v3, pos, normal);
        }
    }

    private static void visitPart(ModelPart modelPart, MatrixStack matrices, VertexConsumer vertexConsumer, Vector3f v0, Vector3f v1, Vector3f v2, Vector3f v3, Vector4f pos, Vector3f normal) {
        if(!modelPart.visible || (modelPart.isEmpty() && modelPart.children.isEmpty()))
            return;

        matrices.push();
        modelPart.rotate(matrices);

        {
            MatrixStack.Entry entry = matrices.peek();
            Matrix4f pose = entry.getPositionMatrix();
            Matrix3f poseNormal = entry.getNormalMatrix();
            Set<Line> lines = new HashSet<>();
            for (ModelPart.Cuboid cuboid : modelPart.cuboids) {
                for (ModelPart.Quad quad : cuboid.sides) {
                    quad.vertices()[0].pos().div(16, v0);
                    quad.vertices()[1].pos().div(16, v1);
                    quad.vertices()[2].pos().div(16, v2);
                    quad.vertices()[3].pos().div(16, v3);
                    lines.add(Line.from(v0, v1));
                    lines.add(Line.from(v1, v2));
                    lines.add(Line.from(v2, v3));
                    lines.add(Line.from(v3, v0));
                }
            }

            for (WireframeExtractor.Line line : lines) {
                poseNormal.transform(line.normalX(), line.normalY(), line.normalZ(), normal);

                pose.transform(line.x1(), line.y1(), line.z1(), 1F, pos);
                vertexConsumer.vertex(pos.x(), pos.y(), pos.z())
                        .color(0, 0, 0, 102)
                        .normal(normal.x, normal.y, normal.z);

                pose.transform(line.x2(), line.y2(), line.z2(), 1F, pos);
                vertexConsumer.vertex(pos.x(), pos.y(), pos.z())
                        .color(0, 0, 0, 102)
                        .normal(normal.x, normal.y, normal.z);
            }
        }

        for (ModelPart part : modelPart.children.values()) {
            visitPart(part, matrices, vertexConsumer, v0, v1, v2, v3, pos, normal);
        }

        matrices.pop();
    }

    private static void extractVerticesFromBakedQuad(Set<Line> lines, BakedQuad quad) {
        int[] vertices = quad.getVertexData();
        for (int vIndex = 0; vIndex < 4; vIndex++) {
            int offset = calculateOffset(vIndex);
            float x1 = Float.intBitsToFloat(vertices[offset]);
            float y1 = Float.intBitsToFloat(vertices[offset + 1]);
            float z1 = Float.intBitsToFloat(vertices[offset + 2]);

            offset = calculateOffset((vIndex + 1) % 4);
            float x2 = Float.intBitsToFloat(vertices[offset]);
            float y2 = Float.intBitsToFloat(vertices[offset + 1]);
            float z2 = Float.intBitsToFloat(vertices[offset + 2]);

            lines.add(Line.from(x1, y1, z1, x2, y2, z2));
        }
    }

    private static int calculateOffset(int index) {
        return index * VertexOffsets.STRIDE + VertexOffsets.POSITION;
    }

    record Line(float x1, float y1, float z1,
                       float x2, float y2, float z2,
                       float normalX, float normalY, float normalZ,
                       int hash) {
        /**
         * Creates a line from two points.
         * The normal of the line is calculated by normalizing the vector from the first point to the second point.
         *
         * @param x1 The x position of the first point.
         * @param y1 The y position of the first point.
         * @param z1 The z position of the first point.
         * @param x2 The x position of the second point.
         * @param y2 The y position of the second point.
         * @param z2 The z position of the second point.
         * @return A line from the two points.
         */
        public static Line from(float x1, float y1, float z1, float x2, float y2, float z2) {
            float nX = x2 - x1;
            float nY = y2 - y1;
            float nZ = z2 - z1;
            float length = Math.fma(nX, nX, Math.fma(nY, nY, nZ * nZ)); // nX^2 + nY^2 + nZ^2
            float normalizationFactor = Math.invsqrt(length); // 1 / sqrt(length)
            nX *= normalizationFactor;
            nY *= normalizationFactor;
            nZ *= normalizationFactor;
            return new Line(x1, y1, z1, x2, y2, z2, nX, nY, nZ, hash(x1, y1, z1, x2, y2, z2));
        }

        public static Line from(Vector3f start, Vector3f end) {
            return from(start.x, start.y, start.z, end.x, end.y, end.z);
        }

        private static int hash(float x1, float y1, float z1, float x2, float y2, float z2) {
            int result = Float.floatToIntBits(x1);
            result = 31 * result + Float.floatToIntBits(y1);
            result = 31 * result + Float.floatToIntBits(z1);
            result = 31 * result + Float.floatToIntBits(x2);
            result = 31 * result + Float.floatToIntBits(y2);
            result = 31 * result + Float.floatToIntBits(z2);
            return result;
        }

        @Override
        public int hashCode() {
            return this.hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null || getClass() != obj.getClass())
                return false;

            Line line = (Line) obj;
            return MathHelper.approximatelyEquals(line.x1, x1) &&
                    MathHelper.approximatelyEquals(line.y1, y1) &&
                    MathHelper.approximatelyEquals(line.z1, z1) &&
                    MathHelper.approximatelyEquals(line.x2, x2) &&
                    MathHelper.approximatelyEquals(line.y2, y2) &&
                    MathHelper.approximatelyEquals(line.z2, z2);
        }
    }
}
