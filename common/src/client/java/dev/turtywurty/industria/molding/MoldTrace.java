package dev.turtywurty.industria.molding;

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

public final class MoldTrace {
    private final List<MoldPoint> polygon;
    private final float tolerance;
    private MoldPoint start;
    private MoldPoint end;
    private int edge;
    private int cornersPassed;
    private boolean failed;
    private boolean complete;
    private final List<List<ProgressRange>> progress = new ArrayList<>();
    private final boolean[] visitedCorners;

    public MoldTrace(List<MoldPoint> polygon, float tolerance) {
        this.polygon = List.copyOf(polygon);
        this.tolerance = tolerance;
        this.visitedCorners = new boolean[polygon.size()];
        for (int i = 0; i < polygon.size(); i++) {
            progress.add(new ArrayList<>());
        }
    }

    public void begin(MoldPoint point) {
        if (start == null) {
            start = point;
            edge = findStartingEdge(point);
        } else if (distance(end, point) > tolerance) {
            failed = true;
        }

        if (complete || distanceToEdge(point, edge) > tolerance) {
            failed = true;
        }
        end = point;
        if (!failed && distance(point, polygon.get(edge)) <= tolerance) {
            visitedCorners[edge] = true;
        }
    }

    public boolean move(MoldPoint point) {
        if (start == null) {
            begin(point);
            return false;
        }

        if (complete || distanceToEdge(point, edge) > tolerance) {
            failed = true;
        }

        MoldPoint previous = end;
        end = point;
        if (failed)
            return false;

        boolean reachedCorner = distance(point, polygon.get((edge + 1) % polygon.size())) <= tolerance;
        addProgress(edge, projection(previous, edge), reachedCorner ? 1 : projection(point, edge));
        if (reachedCorner) {
            visitedCorners[(edge + 1) % polygon.size()] = true;
            edge = (edge + 1) % polygon.size();
            cornersPassed++;
        }

        if (cornersPassed >= polygon.size() && distance(point, start) <= tolerance) {
            complete = true;
            return true;
        }
        return false;
    }

    public int edge() {
        return edge;
    }

    public boolean failed() {
        return failed;
    }

    public List<ProgressRange> progress(int edge) {
        return List.copyOf(progress.get(edge));
    }

    public boolean visitedCorner(int corner) {
        return visitedCorners[corner];
    }

    public record ProgressRange(float start, float end) {
    }

    private void addProgress(int edge, float from, float to) {
        float start = Math.min(from, to);
        float end = Math.max(from, to);
        if (start == end)
            return;
        List<ProgressRange> ranges = progress.get(edge);
        for (int i = ranges.size() - 1; i >= 0; i--) {
            ProgressRange range = ranges.get(i);
            if (start <= range.end() && end >= range.start()) {
                start = Math.min(start, range.start());
                end = Math.max(end, range.end());
                ranges.remove(i);
            }
        }
        ranges.add(new ProgressRange(start, end));
        ranges.sort(Comparator.comparingDouble(ProgressRange::start));
    }

    private float projection(MoldPoint point, int edge) {
        MoldPoint a = polygon.get(edge);
        MoldPoint b = polygon.get((edge + 1) % polygon.size());
        float dx = b.x() - a.x();
        float dy = b.y() - a.y();
        float lengthSquared = dx * dx + dy * dy;
        return lengthSquared == 0
            ? 0
            : Math.clamp(
                ((point.x() - a.x()) * dx + (point.y() - a.y()) * dy) / lengthSquared, 0, 1);
    }

    private int findStartingEdge(MoldPoint point) {
        for (int i = 0; i < polygon.size(); i++) {
            if (distance(point, polygon.get(i)) <= tolerance)
                return i;
        }

        int nearest = 0;
        float nearestDistance = Float.POSITIVE_INFINITY;
        for (int i = 0; i < polygon.size(); i++) {
            float distance = distanceToEdge(point, i);
            if (distance < nearestDistance) {
                nearest = i;
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    private float distanceToEdge(MoldPoint point, int edge) {
        MoldPoint a = polygon.get(edge);
        MoldPoint b = polygon.get((edge + 1) % polygon.size());
        float dx = b.x() - a.x();
        float dy = b.y() - a.y();
        float lengthSquared = dx * dx + dy * dy;
        float t = lengthSquared == 0
            ? 0
            : Math.clamp(
                ((point.x() - a.x()) * dx + (point.y() - a.y()) * dy) / lengthSquared, 0, 1);
        return (float) Math.hypot(point.x() - a.x() - t * dx, point.y() - a.y() - t * dy);
    }

    private static float distance(MoldPoint a, MoldPoint b) {
        return (float) Math.hypot(a.x() - b.x(), a.y() - b.y());
    }
}
