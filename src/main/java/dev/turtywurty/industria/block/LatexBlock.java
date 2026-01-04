package dev.turtywurty.industria.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public interface LatexBlock {
    IntegerProperty LATEX_LEVEL = IntegerProperty.create("latex_level", 0, 5);

    default BlockState withRandomLatex(BlockPlaceContext ctx, BlockState superState, Block block) {
        BlockState state = superState == null ? block.defaultBlockState() : superState;
        return state.setValue(LATEX_LEVEL, ctx.getLevel().getRandom().nextIntBetweenInclusive(1, 5));
    }

    default void tickLatex(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        if(random.nextInt(10) == 0){
            world.setBlockAndUpdate(pos, state.setValue(LATEX_LEVEL, state.getValue(LATEX_LEVEL) - 1));
        }
    }
}