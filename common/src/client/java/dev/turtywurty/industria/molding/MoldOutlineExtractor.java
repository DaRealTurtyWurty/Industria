package dev.turtywurty.industria.molding;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.resources.Identifier;

import java.util.*;

@Deprecated(since = "Originally this was going to use the texture for the mold, but this may be re-used in the future for something else")
public class MoldOutlineExtractor {
    public static boolean isSolid(NativeImage image, int x, int y) {
        if (x < 0 || x >= image.getWidth() || y < 0 || y >= image.getHeight())
            return false;

        int pixel = image.getPixel(x, y);
        int alpha = (pixel >> 24) & 0xFF;
        return alpha > 0;
    }

    public static MoldPattern extract(Identifier id, NativeImage image) {
        List<List<GridPoint>> loops = findLoops(image);

        List<List<MoldPoint>> moldLoops = new ArrayList<>();
        for (List<GridPoint> loop : loops) {
            List<MoldPoint> points = new ArrayList<>();
            for (GridPoint point : loop) {
                float x = (float) point.x() / image.getWidth();
                float y = (float) point.y() / image.getHeight();
                points.add(new MoldPoint(x, y));
            }

            moldLoops.add(List.copyOf(points));
        }

        return new MoldPattern(id, List.copyOf(moldLoops));
    }

    private static List<List<GridPoint>> findLoops(NativeImage image) {
        List<List<GridPoint>> loops = new ArrayList<>();

        List<BoundaryEdge> edges = findBoundaryEdges(image);
        Map<GridPoint, List<BoundaryEdge>> index = indexEdges(edges);

        Set<BoundaryEdge> remaining = new HashSet<>(edges);
        while (!remaining.isEmpty()) {
            BoundaryEdge first = remaining.iterator().next();
            List<GridPoint> loop = traceLoop(first, index);
            loops.add(loop);
            for (int i = 0; i < loop.size(); i++) {
                GridPoint start = loop.get(i);
                GridPoint end = loop.get((i + 1) % loop.size());
                remaining.remove(new BoundaryEdge(start, end));
            }
        }

        return loops;
    }


    private static List<BoundaryEdge> findBoundaryEdges(NativeImage image) {
        List<BoundaryEdge> edges = new ArrayList<>();
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                if (!isSolid(image, x, y))
                    continue;

                var current = new GridPoint(x, y);
                var above = new GridPoint(x, y - 1);
                var right = new GridPoint(x + 1, y);
                var below = new GridPoint(x, y + 1);
                var left = new GridPoint(x - 1, y);

                if (!isSolid(image, above.x(), above.y())) {
                    edges.add(new BoundaryEdge(current, new GridPoint(x + 1, y)));
                }

                if (!isSolid(image, right.x(), right.y())) {
                    edges.add(new BoundaryEdge(new GridPoint(x + 1, y), new GridPoint(x + 1, y + 1)));
                }

                if (!isSolid(image, below.x(), below.y())) {
                    edges.add(new BoundaryEdge(new GridPoint(x + 1, y + 1), new GridPoint(x, y + 1)));
                }

                if (!isSolid(image, left.x(), left.y())) {
                    edges.add(new BoundaryEdge(new GridPoint(x, y + 1), current));
                }
            }
        }

        return edges;
    }

    private static Map<GridPoint, List<BoundaryEdge>> indexEdges(List<BoundaryEdge> edges) {
        Map<GridPoint, List<BoundaryEdge>> edgeMap = new HashMap<>();
        for (BoundaryEdge edge : edges) {
            edgeMap.computeIfAbsent(edge.start(), _ -> new ArrayList<>()).add(edge);
        }

        return edgeMap;
    }

    private static int turnPriority(BoundaryEdge current, BoundaryEdge candidate) {
        int turn = (candidate.direction() - current.direction() + 4) % 4;
        return switch (turn) {
            case 0 -> 1; // Straight
            case 1 -> 0; // Right
            case 2 -> 3; // Backwards
            case 3 -> 2; // Left
            default -> throw new IllegalStateException("Unexpected value: " + turn);
        };
    }

    private static BoundaryEdge findNextEdge(BoundaryEdge current, List<BoundaryEdge> candidates) {
        BoundaryEdge best = null;
        int bestPriority = Integer.MAX_VALUE;
        for (BoundaryEdge candidate : candidates) {
            int priority = turnPriority(current, candidate);
            if (priority < bestPriority) {
                bestPriority = priority;
                best = candidate;
            }
        }

        return best;
    }

    private static List<GridPoint> traceLoop(BoundaryEdge first, Map<GridPoint, List<BoundaryEdge>> index) {
        List<GridPoint> points = new ArrayList<>();

        BoundaryEdge current = first;
        do {
            points.add(current.start());
            List<BoundaryEdge> candidates = index.getOrDefault(current.end(), List.of());
            current = findNextEdge(current, candidates);
            if (current == null)
                throw new IllegalStateException("Found an open outline, this should not happen!");
        } while (!current.equals(first));

        return points;
    }

    public record GridPoint(int x, int y) {
    }

    public record BoundaryEdge(GridPoint start, GridPoint end) {
        public int direction() {
            if (end.x() > start.x())
                return 0;

            if (end.y() > start.y())
                return 1;

            if (end.x() < start.x())
                return 2;

            return 3;
        }
    }
}
