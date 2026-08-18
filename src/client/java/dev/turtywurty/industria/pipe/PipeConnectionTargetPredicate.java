package dev.turtywurty.industria.pipe;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

@FunctionalInterface
public interface PipeConnectionTargetPredicate {
    boolean test(BlockAndTintGetter level, BlockPos targetPos, BlockState targetState, Direction targetFace);
}
