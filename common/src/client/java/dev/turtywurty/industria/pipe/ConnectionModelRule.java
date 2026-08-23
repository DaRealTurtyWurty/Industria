package dev.turtywurty.industria.pipe;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public record ConnectionModelRule(
        Identifier id,
        Block pipeBlock,
        Block targetBlock,
        PipeConnectionTargetPredicate targetMatcher,
        ConnectionModelSet models,
        int priority
) {
    public boolean matches(BlockAndTintGetter level, BlockPos targetPos, BlockState pipeState,
                           BlockState targetState, Direction targetFace) {
        return pipeState.is(pipeBlock)
                && targetState.is(targetBlock)
                && targetMatcher.test(level, targetPos, targetState, targetFace);
    }
}
