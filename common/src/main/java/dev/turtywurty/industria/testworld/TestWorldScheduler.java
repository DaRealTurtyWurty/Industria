package dev.turtywurty.industria.testworld;

import net.minecraft.server.level.ServerLevel;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.function.Consumer;

public final class TestWorldScheduler {
    private static final Map<ServerLevel, PriorityQueue<ScheduledAction>> ACTIONS = new IdentityHashMap<>();

    private static long nextSequence;

    public static void schedule(TestWorldContext context, long delay, Consumer<TestWorldContext> action) {
        long executionTick = context.overworld().getGameTime() + delay;
        ACTIONS.computeIfAbsent(context.overworld(), _ -> new PriorityQueue<>())
                .add(new ScheduledAction(executionTick, nextSequence++, context, action));
    }

    public static void tick(ServerLevel level) {
        PriorityQueue<ScheduledAction> queue = ACTIONS.get(level);
        if (queue == null)
            return;

        long currentTick = level.getGameTime();
        while (!queue.isEmpty() && queue.peek().executionTick() <= currentTick) {
            ScheduledAction action = queue.remove();
            action.action().accept(action.context());
        }

        if (queue.isEmpty()) {
            ACTIONS.remove(level);
        }
    }

    private record ScheduledAction(
            long executionTick,
            long sequence,
            TestWorldContext context,
            Consumer<TestWorldContext> action
    ) implements Comparable<ScheduledAction> {
        @Override
        public int compareTo(ScheduledAction other) {
            int tickComparison = Long.compare(executionTick, other.executionTick());
            if (tickComparison != 0)
                return tickComparison;

            return Long.compare(sequence, other.sequence());
        }
    }
}
