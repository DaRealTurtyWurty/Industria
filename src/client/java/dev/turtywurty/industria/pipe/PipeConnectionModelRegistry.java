package dev.turtywurty.industria.pipe;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.*;

public final class PipeConnectionModelRegistry {
    private static final Map<Block, ConnectionModelSet> DEFAULTS = new IdentityHashMap<>();
    private static final Map<Block, List<ConnectionModelRule>> RULES_BY_TARGET = new IdentityHashMap<>();

    private PipeConnectionModelRegistry() {
    }

    public static void registerDefault(Block pipeBlock, ConnectionModelSet models) {
        if (DEFAULTS.putIfAbsent(pipeBlock, models) != null)
            throw new IllegalArgumentException("A default connection model is already registered for " + pipeBlock);
    }

    public static void register(ConnectionModelRule rule) {
        List<ConnectionModelRule> rules = RULES_BY_TARGET.computeIfAbsent(rule.targetBlock(), _ -> new ArrayList<>());
        rules.add(rule);
        rules.sort(
                Comparator.comparingInt(ConnectionModelRule::priority)
                        .reversed()
                        .thenComparing(ruleEntry -> ruleEntry.id().toString())
        );
    }

    @Nullable
    public static ConnectionModelSet findReplacement(BlockState pipeState, BlockState targetState, Direction targetFace) {
        List<ConnectionModelRule> rules = RULES_BY_TARGET.get(targetState.getBlock());
        if (rules == null)
            return null;

        for (ConnectionModelRule rule : rules) {
            if (rule.matches(pipeState, targetState, targetFace))
                return rule.models();
        }

        return null;
    }

    @Nullable
    public static ConnectionModelSet findModel(BlockState pipeState, BlockState targetState, Direction targetFace) {
        ConnectionModelSet replacement = findReplacement(pipeState, targetState, targetFace);

        if (replacement != null)
            return replacement;

        return DEFAULTS.get(pipeState.getBlock());
    }

    public static Collection<ConnectionModelSet> allModelSets() {
        Set<ConnectionModelSet> models = Collections.newSetFromMap(new IdentityHashMap<>());

        models.addAll(DEFAULTS.values());

        for (List<ConnectionModelRule> rules : RULES_BY_TARGET.values()) {
            for (ConnectionModelRule rule : rules) {
                models.add(rule.models());
            }
        }

        return models;
    }
}
