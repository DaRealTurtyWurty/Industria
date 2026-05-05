package dev.turtywurty.industria.block;

import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.Nullable;

public interface LatexBlock {
    IntegerProperty LATEX_LEVEL = IntegerProperty.create("latex_level", 0, 9);

    default BlockState withRandomLatex(BlockPlaceContext ctx, BlockState superState, Block block) {
        BlockState state = superState == null ? block.defaultBlockState() : superState;
        return state.setValue(LATEX_LEVEL, ctx.getLevel().getRandom().nextIntBetweenInclusive(6, 9));
    }

    default boolean hasLatex(BlockState state) {
        return state.getValue(LATEX_LEVEL) > 0;
    }

    default int getLatexLevel(BlockState state) {
        return state.getValue(LATEX_LEVEL);
    }

    default @Nullable BlockState extractLatex(BlockState state) {
        int level = state.getValue(LATEX_LEVEL);
        if (level > 0)
            return state.setValue(LATEX_LEVEL, level - 1);

        return null;
    }
}