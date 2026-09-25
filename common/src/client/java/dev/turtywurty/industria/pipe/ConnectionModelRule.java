package dev.turtywurty.industria.pipe;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record ConnectionModelRule(
        Identifier id,
        Block pipeBlock,
        Block targetBlock,
        PipeConnectionModelSelector selector,
        List<ConnectionModelSet> models,
        int priority
) {
    public ConnectionModelRule {
        models = List.copyOf(models);
    }

    public ConnectionModelRule(Identifier id, Block pipeBlock, Block targetBlock,
                               PipeConnectionTargetPredicate targetMatcher, ConnectionModelSet model, int priority) {
        this(id, pipeBlock, targetBlock,
                (level, targetPos, targetState, targetFace) ->
                        targetMatcher.test(level, targetPos, targetState, targetFace) ? model : null,
                List.of(model), priority);
    }

    public @Nullable ConnectionModelSet resolve(BlockAndTintGetter level, BlockPos targetPos, BlockState pipeState,
                                                BlockState targetState, Direction targetFace) {
        if (!pipeState.is(pipeBlock) || !targetState.is(targetBlock))
            return null;

        return selector.select(level, targetPos, targetState, targetFace);
    }
}
