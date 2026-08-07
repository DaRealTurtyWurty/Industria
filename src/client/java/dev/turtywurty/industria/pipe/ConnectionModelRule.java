package dev.turtywurty.industria.pipe;

import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.BiPredicate;

public record ConnectionModelRule(
        Identifier id,
        Block pipeBlock,
        Block targetBlock,
        BiPredicate<BlockState, Direction> targetMatcher,
        ConnectionModelSet models,
        int priority
) {
    public boolean matches(BlockState pipeState, BlockState targetState, Direction targetFace) {
        return pipeState.is(pipeBlock)
                && targetState.is(targetBlock)
                && targetMatcher.test(targetState, targetFace);
    }
}
