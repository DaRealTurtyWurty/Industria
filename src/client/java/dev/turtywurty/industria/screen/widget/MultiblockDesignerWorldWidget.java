package dev.turtywurty.industria.screen.widget;

import dev.turtywurty.industria.screen.MultiblockDesignerScreen;
import net.minecraft.client.gui.Click;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;

public class MultiblockDesignerWorldWidget extends FakeWorldWidget {
    private static final double MAX_CENTER_DISTANCE_SQ = 576.0;
    private final MultiblockDesignerScreen multiblockDesignerScreen;

    public MultiblockDesignerWorldWidget(MultiblockDesignerScreen multiblockDesignerScreen, int x, int y) {
        super(multiblockDesignerScreen.getScene(), x, y, 162, 162, true, 0.35F);
        this.multiblockDesignerScreen = multiblockDesignerScreen;
    }

    public boolean handleClick(Click click, boolean doubled) {
        if (isInsideWidget(click.x(), click.y())) {
            BlockPos closest = findClosestPiece(click.x(), click.y());
            if (closest != null) {
                multiblockDesignerScreen.selectPiece(closest);
                return true;
            }
        }

        return false;
    }

    private boolean isInsideWidget(double mouseX, double mouseY) {
        return mouseX >= this.x && mouseX < this.x + this.width
                && mouseY >= this.y && mouseY < this.y + this.height;
    }

    private BlockPos findClosestPiece(double mouseX, double mouseY) {
        BlockPos closestInside = null;
        double closestInsideDepthSq = Double.MAX_VALUE;
        BlockPos closestNear = null;
        double closestNearDistanceSq = Double.MAX_VALUE;
        double closestNearDepthSq = Double.MAX_VALUE;
        for (BlockPos pos : multiblockDesignerScreen.getCachedVariedBlockLists().keySet()) {
            Vec3d center = Vec3d.ofCenter(pos);
            Optional<Vec2f> projectedCenter = this.scene.projectToWidget(center);
            if (projectedCenter.isEmpty())
                continue;

            Vec2f screenPos = projectedCenter.get();
            double dx = screenPos.x - mouseX;
            double dy = screenPos.y - mouseY;
            double distanceSq = dx * dx + dy * dy;
            double depthSq = this.scene.getCameraPosition().squaredDistanceTo(center);

            Optional<ProjectedBounds> bounds = projectBlockBounds(pos);
            if (bounds.isPresent() && bounds.get().contains(mouseX, mouseY)) {
                if (depthSq < closestInsideDepthSq) {
                    closestInside = pos;
                    closestInsideDepthSq = depthSq;
                }
                continue;
            }

            if (distanceSq >= MAX_CENTER_DISTANCE_SQ)
                continue;

            boolean betterDistance = distanceSq < closestNearDistanceSq;
            boolean sameDistanceCloser = distanceSq == closestNearDistanceSq && depthSq < closestNearDepthSq;
            if (betterDistance || sameDistanceCloser) {
                closestNear = pos;
                closestNearDistanceSq = distanceSq;
                closestNearDepthSq = depthSq;
            }
        }

        return closestInside != null ? closestInside : closestNear;
    }

    private Optional<ProjectedBounds> projectBlockBounds(BlockPos pos) {
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        boolean projected = false;

        double baseX = pos.getX();
        double baseY = pos.getY();
        double baseZ = pos.getZ();
        for (int dx = 0; dx <= 1; dx++) {
            for (int dy = 0; dy <= 1; dy++) {
                for (int dz = 0; dz <= 1; dz++) {
                    Optional<Vec2f> projectedCorner = this.scene.projectToWidgetUnclamped(new Vec3d(baseX + dx, baseY + dy, baseZ + dz));
                    if (projectedCorner.isEmpty())
                        continue;

                    projected = true;
                    Vec2f screenCorner = projectedCorner.get();
                    minX = Math.min(minX, screenCorner.x);
                    minY = Math.min(minY, screenCorner.y);
                    maxX = Math.max(maxX, screenCorner.x);
                    maxY = Math.max(maxY, screenCorner.y);
                }
            }
        }

        if (!projected)
            return Optional.empty();

        return Optional.of(new ProjectedBounds(minX, minY, maxX, maxY));
    }

    private record ProjectedBounds(double minX, double minY, double maxX, double maxY) {
        boolean contains(double x, double y) {
            return x >= this.minX && x <= this.maxX && y >= this.minY && y <= this.maxY;
        }
    }
}
