package dev.turtywurty.industria.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public interface LatexBlock {
    IntProperty LATEX_LEVEL = IntProperty.of("latex_level", 0, 5);

    default BlockState withRandomLatex(ItemPlacementContext ctx, BlockState superState, Block block) {
        BlockState state = superState == null ? block.getDefaultState() : superState;
        return state.with(LATEX_LEVEL, ctx.getWorld().random.nextBetween(1, 5));
    }

    default void tickLatex(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if(random.nextInt(10) == 0){
            world.setBlockState(pos, state.with(LATEX_LEVEL, state.get(LATEX_LEVEL) - 1));
        }
    }
}