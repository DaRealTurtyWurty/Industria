package dev.turtywurty.industria.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public class RubberWoodBlock extends Block implements LatexBlock {
    public final boolean isStripped;

    public RubberWoodBlock(AbstractBlock.Settings settings, boolean isStripped) {
        super(isStripped ? settings.ticksRandomly() : settings);
        this.isStripped = isStripped;

        setDefaultState(this.stateManager.getDefaultState().with(LATEX_LEVEL, 0));
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return withRandomLatex(ctx, super.getPlacementState(ctx), this);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(LATEX_LEVEL);
    }

    @Override
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        super.randomTick(state, world, pos, random);
        tickLatex(state, world, pos, random);
    }

    @Override
    protected boolean hasRandomTicks(BlockState state) {
        return this.isStripped && state.get(LATEX_LEVEL) > 0;
    }
}
