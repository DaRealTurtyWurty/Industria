package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.blockentity.FluidPumpBlockEntity;
import dev.turtywurty.industria.blockentity.util.TickableBlockEntity;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

public class FluidPumpBlock extends IndustriaBlock {
    private static final Map<Direction, Map<DoubleBlockHalf, VoxelShape>> SHAPES = createShapes();

    public FluidPumpBlock(Properties settings) {
        super(settings, new BlockProperties()
                .hasHorizontalFacing()
                .addStateProperty(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER)
                .shapeFactory((state, world, pos, context) -> SHAPES
                        .get(state.getValue(BlockStateProperties.HORIZONTAL_FACING))
                        .get(state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF)))
                .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(() -> BlockEntityTypeInit.FLUID_PUMP)
                        .blockEntityFactory((pos, state) ->
                                state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER
                                        ? BlockEntityTypeInit.FLUID_PUMP.create(pos, state)
                                        : null)
                        .shouldTick()
                        .blockEntityTickerFactory((world, state, type) ->
                                state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER
                                        ? TickableBlockEntity.createTicker(world)
                                        : null)));
    }

    private static Map<Direction, Map<DoubleBlockHalf, VoxelShape>> createShapes() {
        Map<Direction, Map<DoubleBlockHalf, VoxelShape>> shapes = new EnumMap<>(Direction.class);

        // The exported shape is opposite the model's native orientation because the model elements are rotated 180°.
        VoxelShape northShape = BlockProperties.calculateShape(Direction.SOUTH, makeShape());
        for (Direction direction : BlockStateProperties.HORIZONTAL_FACING.getPossibleValues()) {
            VoxelShape fullShape = BlockProperties.calculateShape(direction, northShape);
            Map<DoubleBlockHalf, VoxelShape> halves = new EnumMap<>(DoubleBlockHalf.class);
            halves.put(DoubleBlockHalf.LOWER,
                    Shapes.join(fullShape, Shapes.block(), BooleanOp.AND).optimize());
            halves.put(DoubleBlockHalf.UPPER,
                    Shapes.join(fullShape.move(0, -1, 0), Shapes.block(), BooleanOp.AND).optimize());
            shapes.put(direction, halves);
        }

        return shapes;
    }

    private static VoxelShape makeShape() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0, 1.25, 0, 0.25, 1.3125, 0.0625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 1.25, 0.0625, 0.0625, 1.3125, 0.25), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.1875, 1.3125, 0, 0.25, 1.5, 0.0625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 1.0625, 0.1875, 0.125, 1.125, 0.25), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.1875, 1.4375, 0.0625, 0.25, 1.5, 0.125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 1.0625, 0.1875, 0.0625, 1.25, 0.25), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 1.5625, 0, 0.25, 1.625, 0.0625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 1.5625, 0.0625, 0.0625, 1.625, 0.25), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 1.5625, 0.1875, 0.125, 1.625, 0.25), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.1875, 1.5625, 0.0625, 0.25, 1.625, 0.125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 1, 0, 0.25, 1.0625, 0.0625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 1, 0.0625, 0.0625, 1.0625, 0.25), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 1, 0.1875, 0.125, 1.0625, 0.25), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.1875, 0.8125, 0.0625, 0.25, 0.875, 0.125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.75, 0.8125, 0.9375, 0.8125, 1, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0.5625, 0, 0.25, 0.625, 0.0625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0.5625, 0.0625, 0.0625, 0.625, 0.25), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.5625, 0.1875, 0.125, 0.625, 0.25), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.1875, 0.5625, 0.0625, 0.25, 0.625, 0.125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 1.125, 0, 0.25, 1.1875, 0.0625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 1.125, 0.0625, 0.0625, 1.1875, 0.25), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 1.3125, 0.1875, 0.125, 1.375, 0.25), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.1875, 1.125, 0.0625, 0.25, 1.1875, 0.125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.9375, 1.1875, 0.75, 1, 1.375, 0.8125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.09375, 0.0625, 0.09375, 0.90625, 0.6875, 0.90625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.125, 0.75, 0.125, 0.875, 0.9375, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.09375, 0.9375, 0.09375, 0.90625, 1.875, 0.90625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0, 0.0625, 0.9375, 0.0625, 0.9375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.03125, 0.375625, 0.03125, 0.96875, 0.750625, 0.96875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.375, 1.875, 0.375, 0.625, 2, 0.625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0.3125, 0.3125, 0.125, 0.6875, 0.6875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.34375, 0.375, 0.9375, 0.65625, 0.6875, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.34375, 1, 0.875, 0.65625, 1.75, 0.9375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.34375, 1, 0.875, 0.65625, 1.75, 0.9375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.34375, 1, 0.90625, 0.65625, 1.75, 0.96875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.3125, 1, 0.90625, 0.625, 1.75, 0.96875), BooleanOp.OR);
        return shape;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos upperPos = context.getClickedPos().above();
        if (context.getLevel().isOutsideBuildHeight(upperPos)
                || !context.getLevel().getBlockState(upperPos).canBeReplaced()) {
            return null;
        }

        return super.getStateForPlacement(context);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                            @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER) {
            level.setBlock(pos.above(),
                    state.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER),
                    Block.UPDATE_ALL);
        }
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER
                ? RenderShape.MODEL
                : RenderShape.INVISIBLE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (!level.isClientSide()) {
            BlockPos controllerPos = getControllerPos(pos, state);
            if (level.getBlockEntity(controllerPos) instanceof FluidPumpBlockEntity fluidPump) {
                player.openMenu(fluidPump);
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean moved) {
        super.affectNeighborsAfterRemoval(state, level, pos, moved);

        BlockPos otherPos = state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER
                ? pos.above()
                : pos.below();
        if (level.getBlockState(otherPos).is(this)) {
            level.destroyBlock(otherPos, false);
        }
    }

    public static BlockPos getControllerPos(BlockPos pos, BlockState state) {
        return state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER
                ? pos.below()
                : pos;
    }
}
