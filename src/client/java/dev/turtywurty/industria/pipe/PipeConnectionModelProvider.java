package dev.turtywurty.industria.pipe;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public interface PipeConnectionModelProvider {
    @Nullable ConnectionModelSet resolve(BlockState pipeState, BlockState targetState, Direction pipeToTarget);
}
