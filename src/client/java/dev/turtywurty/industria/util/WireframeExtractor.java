package dev.turtywurty.industria.util;

import net.minecraft.client.render.model.BakedGeometry;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import org.joml.Math;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public interface WireframeExtractor {
    static List<Line> fromBakedModel(BakedGeometry model) {
        Set<Line> lines = new HashSet<>(); // We are using a set to avoid duplicate lines (hashes are used to determine if a line is a duplicate)
        for (Direction direction : Direction.values()) {
            for (BakedQuad quad : model.getQuads(direction)) {
                extractVerticesFromBakedQuad(lines, quad);
            }
        }

        for (BakedQuad quad : model.getQuads(null)) {
            extractVerticesFromBakedQuad(lines, quad);
        }

        return new ArrayList<>(lines);
    }

    private static void extractVerticesFromBakedQuad(Set<Line> lines, BakedQuad quad) {
        int[] vertices = quad.vertexData();
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
