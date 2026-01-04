package dev.turtywurty.industria.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

public class RubberLogBlock extends RotatedPillarBlock implements LatexBlock {
    public final boolean isStripped;

    public RubberLogBlock(Properties settings, boolean isStripped) {
        super(isStripped ? settings.randomTicks() : settings);
        this.isStripped = isStripped;

        registerDefaultState(this.stateDefinition.any().setValue(AXIS, Direction.Axis.Y).setValue(LATEX_LEVEL, 0));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return withRandomLatex(ctx, super.getStateForPlacement(ctx), this);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LATEX_LEVEL);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        super.randomTick(state, world, pos, random);
        tickLatex(state, world, pos, random);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return this.isStripped && state.getValue(LATEX_LEVEL) > 0;
    }
}
