package dev.turtywurty.industria.pipe;

import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.BiPredicate;

public final class PipeConnectionModelApi {
    private PipeConnectionModelApi() {
    }

    public static void register(
            Identifier ruleId,
            Block pipeBlock,
            Block targetBlock,
            Direction targetFace,
            ConnectionModelSet models
    ) {
        register(
                ruleId,
                pipeBlock,
                targetBlock,
                (_, actualFace) -> actualFace == targetFace,
                models,
                0
        );
    }

    public static void register(
            Identifier ruleId,
            Block pipeBlock,
            Block targetBlock,
            BiPredicate<BlockState, Direction> targetMatcher,
            ConnectionModelSet models,
            int priority
    ) {
        register(
                ruleId,
                pipeBlock,
                targetBlock,
                (_, _, state, targetFace) -> targetMatcher.test(state, targetFace),
                models,
                priority
        );
    }

    public static void register(
            Identifier ruleId,
            Block pipeBlock,
            Block targetBlock,
            PipeConnectionTargetPredicate targetMatcher,
            ConnectionModelSet models,
            int priority
    ) {
        PipeConnectionModelRegistry.register(new ConnectionModelRule(
                        ruleId,
                        pipeBlock,
                        targetBlock,
                        targetMatcher,
                        models,
                        priority
                )
        );
    }
}
