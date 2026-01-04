package dev.turtywurty.industria.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

public class RubberWoodBlock extends Block implements LatexBlock {
    public final boolean isStripped;

    public RubberWoodBlock(BlockBehaviour.Properties settings, boolean isStripped) {
        super(isStripped ? settings.randomTicks() : settings);
        this.isStripped = isStripped;

        registerDefaultState(this.stateDefinition.any().setValue(LATEX_LEVEL, 0));
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
