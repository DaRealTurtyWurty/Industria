package dev.turtywurty.industria.util;

import java.util.ArrayList;
import java.util.List;

public class MouseTracker {
    private final Track track = new Track();
    private final int updatesPerSecond;

    private long lastUpdate = 0;

    public MouseTracker(int updatesPerSecond) {
        this.updatesPerSecond = updatesPerSecond;
    }

    public void update(float mouseX, float mouseY) {
        update((int) mouseX, (int) mouseY);
    }

    public void update(int mouseX, int mouseY) {
        if (System.currentTimeMillis() - this.lastUpdate >= 1000 / this.updatesPerSecond) {
            this.track.addPosition(mouseX, mouseY);
            this.lastUpdate = System.currentTimeMillis();
        }
    }

    public Track getTrack() {
        return this.track;
    }

    public void reset() {
        this.track.reset();
    }

    public static class Track {
        private final List<Position> positions = new ArrayList<>();

        public void addPosition(int x, int y) {
            Position last = this.positions.isEmpty() ? null : this.positions.getLast();
            if (last != null && last.getX() == x && last.getY() == y) {
                last.incrementTimes();
            } else {
                this.positions.add(new Position(x, y));
            }
        }

        public List<Position> getPositions() {
            return this.positions;
        }

        public void reset() {
            this.positions.clear();
        }

        public boolean isEmpty() {
            return this.positions.isEmpty();
        }

        public int length() {
            return this.positions.stream().mapToInt(Position::getTimes).sum();
        }
    }

    public static class Position {
        private final int x;
        private final int y;
        private int times = 1;

        public Position(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return this.x;
        }

        public int getY() {
            return this.y;
        }

        public int getTimes() {
            return this.times;
        }

        public void incrementTimes() {
            this.times++;
        }
    }
}
