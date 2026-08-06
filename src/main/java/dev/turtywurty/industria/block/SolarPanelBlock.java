package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class SolarPanelBlock extends IndustriaBlock {
    private static final VoxelShape VOXEL_SHAPE = createShape();

    public static final BooleanProperty ON_STAIR = BooleanProperty.create("on_stair");

    public SolarPanelBlock(Properties settings) {
        super(settings, new BlockProperties()
                .hasHorizontalFacing()
                .addStateProperty(ON_STAIR, false)
                .useRotatedShapes(VOXEL_SHAPE)
                .canExistAt(SolarPanelBlock::hasSolidGround)
                .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(() -> BlockEntityTypeInit.SOLAR_PANEL)
                        .shouldTick()
                        .rightClickToOpenGui()));
    }

    private static boolean hasSolidGround(LevelReader level, BlockPos pos) {
        BlockPos groundPos = pos.below();
        BlockState groundState = level.getBlockState(groundPos);
        return StairBlock.isStairs(groundState)
                ? isValidStair(groundState)
                : groundState.isFaceSturdy(level, groundPos, Direction.UP);
    }

    private static boolean isValidStair(BlockState state) {
        return StairBlock.isStairs(state)
                && state.getValue(StairBlock.HALF) == Half.BOTTOM
                && state.getValue(StairBlock.SHAPE) == StairsShape.STRAIGHT;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null)
            return null;

        BlockState groundState = context.getLevel().getBlockState(context.getClickedPos().below());
        boolean onStair = isValidStair(groundState);
        state = state.setValue(ON_STAIR, onStair);
        return onStair
                ? state.setValue(BlockStateProperties.HORIZONTAL_FACING, groundState.getValue(StairBlock.FACING))
                : state;
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks,
                                     BlockPos pos, Direction direction, BlockPos neighborPos,
                                     BlockState neighborState, RandomSource random) {
        BlockState updatedState = super.updateShape(state, level, ticks, pos, direction, neighborPos, neighborState, random);
        if (updatedState.is(Blocks.AIR) || direction != Direction.DOWN)
            return updatedState;

        boolean onStair = isValidStair(neighborState);
        updatedState = updatedState.setValue(ON_STAIR, onStair);
        return onStair
                ? updatedState.setValue(BlockStateProperties.HORIZONTAL_FACING, neighborState.getValue(StairBlock.FACING))
                : updatedState;
    }

    private static VoxelShape createShape() {
        var shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0.125, 0, 0.1875, 0.875, 0.375, 0.8125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.25, 0.375, 0.3125, 0.75, 0.6875, 0.6875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.625, 0.0625, 0.9375, 0.8125, 0.25), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.6875, 0.25, 0.9375, 0.875, 0.4375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.75, 0.4375, 0.9375, 0.9375, 0.625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.8125, 0.625, 0.9375, 1, 0.8125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.25, 0.6875, 0.4375, 0.75, 0.75, 0.6875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.25, 0.75, 0.625, 0.75, 0.8125, 0.6875), BooleanOp.OR);

        return shape.optimize();
    }
}
