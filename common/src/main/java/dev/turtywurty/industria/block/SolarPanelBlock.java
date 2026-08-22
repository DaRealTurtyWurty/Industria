package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.client.ClientScreenHooks;
import dev.turtywurty.industria.init.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

public class SolarPanelBlock extends IndustriaBlock {
    private static final Map<Direction, VoxelShape> SOLAR_PANEL_SHAPES =
            createRotatedShapes(createSolarPanelShape());
    private static final Map<Direction, VoxelShape> SOLAR_PANEL_STAIR_SHAPES =
            createRotatedShapes(createSolarPanelStairShape());
    private static final Map<Direction, VoxelShape> ADVANCED_SOLAR_PANEL_SHAPES =
            createRotatedShapes(createAdvancedSolarPanelShape());
    private static final Map<Direction, VoxelShape> ADVANCED_SOLAR_PANEL_STAIR_SHAPES =
            createRotatedShapes(createAdvancedSolarPanelStairShape());

    public static final BooleanProperty ON_STAIR = BooleanProperty.create("on_stair");
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    private final boolean isAdvanced;

    public SolarPanelBlock(Properties settings, boolean isAdvanced) {
        super(settings, createBlockProperties(isAdvanced));
        this.isAdvanced = isAdvanced;
    }

    private static BlockProperties createBlockProperties(boolean isAdvanced) {
        Map<Direction, VoxelShape> groundShapes = isAdvanced
                ? ADVANCED_SOLAR_PANEL_SHAPES
                : SOLAR_PANEL_SHAPES;
        Map<Direction, VoxelShape> stairShapes = isAdvanced
                ? ADVANCED_SOLAR_PANEL_STAIR_SHAPES
                : SOLAR_PANEL_STAIR_SHAPES;

        return new BlockProperties()
                .hasHorizontalFacing()
                .addStateProperty(ON_STAIR, false)
                .addStateProperty(POWERED, false)
                .shapeFactory((state, _, _, _) -> (state.getValue(ON_STAIR) ? stairShapes : groundShapes)
                        .get(state.getValue(BlockStateProperties.HORIZONTAL_FACING)))
                .canExistAt(SolarPanelBlock::hasSolidGround)
                .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(ModBlockEntityTypes.SOLAR_PANEL)
                        .shouldTick());
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
        if (direction == Direction.DOWN && state.getValue(ON_STAIR) && !hasSolidGround(level, pos)) {
            ticks.scheduleTick(pos, this, 1);
            return state;
        }

        BlockState updatedState = super.updateShape(state, level, ticks, pos, direction, neighborPos, neighborState, random);
        if (updatedState.is(Blocks.AIR) || direction != Direction.DOWN)
            return updatedState;

        boolean onStair = isValidStair(neighborState);
        updatedState = updatedState.setValue(ON_STAIR, onStair);
        return onStair
                ? updatedState.setValue(BlockStateProperties.HORIZONTAL_FACING, neighborState.getValue(StairBlock.FACING))
                : updatedState;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(ON_STAIR) && !hasSolidGround(level, pos)) {
            level.destroyBlock(pos, true);
        }
    }

    public boolean isAdvanced() {
        return this.isAdvanced;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) {
            ClientScreenHooks.openSolarPanel(level, pos);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return this.isAdvanced ? RenderShape.INVISIBLE : RenderShape.MODEL;
    }

    private static Map<Direction, VoxelShape> createRotatedShapes(VoxelShape shape) {
        Map<Direction, VoxelShape> rotatedShapes = new EnumMap<>(Direction.class);
        for (Direction direction : BlockStateProperties.HORIZONTAL_FACING.getPossibleValues()) {
            rotatedShapes.put(direction, BlockProperties.calculateShape(direction, shape));
        }

        return rotatedShapes;
    }

    private static VoxelShape createSolarPanelShape() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0.125, 0, 0.125, 0.875, 0.375, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.875, 0.0625, 0.375, 1, 0.3125, 0.625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.4375, 0.375, 0.4375, 0.5625, 0.5625, 0.5625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0.0625, 0.375, 0.125, 0.3125, 0.625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.1875, 0.0625, 0.0625, 0.8125, 0.3125, 0.125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.78125, 0.09375, 0.78125, 0.90625, 0.15625, 0.90625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.78125, 0.21875, 0.78125, 0.90625, 0.28125, 0.90625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.15625, 0.21875, 0.15625, 0.28125, 0.28125, 0.28125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.15625, 0.09375, 0.15625, 0.28125, 0.15625, 0.28125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0.40625, 0, 1, 0.71875, 1), BooleanOp.OR);

        return shape.optimize();
    }

    private static VoxelShape createSolarPanelStairShape() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0.875, 0.0625, 0.375, 1, 0.3125, 0.625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.4375, 0.125, 0.4375, 0.5625, 0.5625, 0.5625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0.0625, 0.375, 0.125, 0.3125, 0.625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.1875, -0.375, 0.4375, 0.8125, -0.125, 0.5), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.15625, -0.21875, 0.53125, 0.28125, -0.15625, 0.65625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.15625, -0.34375, 0.53125, 0.28125, -0.28125, 0.65625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.125, 0.0625, 0.375, 0.875, 0.3125, 0.625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0, 0.5, 0.1875, 0.0625, 0.625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.8125, 0, 0.5, 0.9375, 0.0625, 0.625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0.1875, 0.125, 1, 0.3125, 0.25), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0.3125, 0.25, 1, 0.4375, 0.375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0.4375, 0.375, 1, 0.5625, 0.5), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0.5625, 0.5, 1, 0.6875, 0.625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0.6875, 0.625, 1, 0.8125, 0.75), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0.8125, 0.75, 1, 0.9375, 0.875), BooleanOp.OR);

        return shape.optimize();
    }

    private static VoxelShape createAdvancedSolarPanelShape() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0.125, 0, 0.125, 0.875, 0.375, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.875, 0.0625, 0.375, 1, 0.3125, 0.625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.4375, 0.375, 0.4375, 0.5625, 0.5, 0.5625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0.8125, 0, 1, 0.9375, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0.0625, 0.375, 0.125, 0.3125, 0.625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.1875, 0.0625, 0.0625, 0.8125, 0.3125, 0.125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.78125, 0.09375, 0.78125, 0.90625, 0.15625, 0.90625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.78125, 0.21875, 0.78125, 0.90625, 0.28125, 0.90625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.15625, 0.21875, 0.15625, 0.28125, 0.28125, 0.28125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.15625, 0.09375, 0.15625, 0.28125, 0.15625, 0.28125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0.5, 0.4375, 1, 0.625, 0.5625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.9375, 0.625, 0.4375, 1, 0.8125, 0.5625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0.625, 0.4375, 0.0625, 0.8125, 0.5625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.03125, 0.78125, 0.40625, 0.09375, 0.96875, 0.59375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.90625, 0.78125, 0.40625, 1.03125, 0.96875, 0.59375), BooleanOp.OR);

        return shape.optimize();
    }

    private static VoxelShape createAdvancedSolarPanelStairShape() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0.875, 0.0625, 0.375, 1, 0.3125, 0.625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.4375, 0.3125, 0.4375, 0.5625, 0.5, 0.5625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0.8125, 0, 1, 0.9375, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0.0625, 0.375, 0.125, 0.3125, 0.625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.1875, -0.375, 0.4375, 0.8125, -0.125, 0.5), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.15625, -0.21875, 0.53125, 0.28125, -0.15625, 0.65625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.15625, -0.34375, 0.53125, 0.28125, -0.28125, 0.65625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0.5, 0.4375, 1, 0.625, 0.5625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.9375, 0.625, 0.4375, 1, 0.8125, 0.5625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0.625, 0.4375, 0.0625, 0.8125, 0.5625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.03125, 0.78125, 0.40625, 0.09375, 0.96875, 0.59375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.90625, 0.78125, 0.40625, 1.03125, 0.96875, 0.59375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.125, 0.0625, 0.375, 0.875, 0.3125, 0.625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0, 0.5, 0.1875, 0.0625, 0.625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.8125, 0, 0.5, 0.9375, 0.0625, 0.625), BooleanOp.OR);

        return shape.optimize();
    }
}
