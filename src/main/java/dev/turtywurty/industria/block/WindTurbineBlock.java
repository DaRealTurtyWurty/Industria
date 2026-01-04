package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.blockentity.WindTurbineBlockEntity;
import dev.turtywurty.industria.blockentity.util.TickableBlockEntity;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WindTurbineBlock extends IndustriaBlock {
    public static final IntegerProperty PART = IntegerProperty.create("part", 0, 3);

    private static final Map<Direction, List<VoxelShape>> SHAPES = new HashMap<>();

    public WindTurbineBlock(Properties settings) {
        super(settings, new BlockProperties()
                .hasHorizontalFacing()
                .addStateProperty(PART, 0)
                .hasBlockEntityRenderer()
                .shapeFactory((state, world, pos, context) ->
                        SHAPES.get(state.getValue(BlockStateProperties.HORIZONTAL_FACING)).get(state.getValue(PART)))
                .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(() -> BlockEntityTypeInit.WIND_TURBINE)
                        .blockEntityFactory((pos, state) ->
                                state.getValue(PART) == 0 ? BlockEntityTypeInit.WIND_TURBINE.create(pos, state) : null)
                        .shouldTick()
                        .blockEntityTickerFactory((world, state, type) ->
                                state.getValue(PART) == 0 ? TickableBlockEntity.createTicker(world) : null)));

        VoxelShape shape = createShape();
        Map<Direction, VoxelShape> directionalShapes = runShapeCalculation(shape);
        for (Direction direction : Direction.values()) {
            VoxelShape directionalShape = directionalShapes.get(direction);
            for (int part = 0; part <= 3; part++) {
                SHAPES.computeIfAbsent(direction, direction1 -> new ArrayList<>())
                        .add(directionalShape.move(0, -part, 0));
            }
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (!world.isClientSide()) {
            BlockPos blockEntityPos = pos;
            if (state.getValue(PART) != 0) {
                blockEntityPos = pos.below(state.getValue(PART));
            }

            if (world.getBlockEntity(blockEntityPos) instanceof WindTurbineBlockEntity windTurbine) {
                player.openMenu(windTurbine);
            }
        }

        return InteractionResult.SUCCESS;
    }

    private static Map<Direction, VoxelShape> runShapeCalculation(VoxelShape shape) {
        Map<Direction, VoxelShape> shapes = new HashMap<>();
        for (final Direction direction : Direction.values()) {
            shapes.put(direction, calculateShapes(direction, shape));
        }

        return shapes;
    }

    private static VoxelShape calculateShapes(Direction to, VoxelShape shape) {
        final VoxelShape[] buffer = {shape, Shapes.empty()};

        final int times = (to.get2DDataValue() - Direction.NORTH.get2DDataValue() + 4) % 4;
        for (int i = 0; i < times; i++) {
            buffer[0].forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
                    buffer[1] = Shapes.or(buffer[1],
                            Shapes.box(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX)));
            buffer[0] = buffer[1];
            buffer[1] = Shapes.empty();
        }

        return buffer[0];
    }

    private static VoxelShape createShape() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.joinUnoptimized(shape, Shapes.box(0.0625, 0, 0.0625, 0.9375, 0.125, 0.9375), BooleanOp.OR);
        shape = Shapes.joinUnoptimized(shape, Shapes.box(0.125, 0.125, 0.125, 0.875, 0.8125, 0.875), BooleanOp.OR);
        shape = Shapes.joinUnoptimized(shape, Shapes.box(0.25, 0.8125, 0.25, 0.75, 2.125, 0.75), BooleanOp.OR);
        shape = Shapes.joinUnoptimized(shape, Shapes.box(0.3125, 3.5, 0.125, 0.6875, 3.875, 0.8125), BooleanOp.OR);
        shape = Shapes.joinUnoptimized(shape, Shapes.box(0.4375, 0, 0.4375, 0.5625, 0.125, 0.5625), BooleanOp.OR);
        shape = Shapes.joinUnoptimized(shape, Shapes.box(0.375, 3.5625, 0.0625, 0.625, 3.8125, 0.125), BooleanOp.OR);
        shape = Shapes.joinUnoptimized(shape, Shapes.box(0.375, 2.125, 0.375, 0.625, 3.5, 0.625), BooleanOp.OR);
        shape = Shapes.joinUnoptimized(shape, Shapes.box(0.4375, 3.625, 0, 0.5625, 3.75, 0.0625), BooleanOp.OR);

        return shape.optimize();
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        if (state.getValue(PART) != 0)
            return;

        for (int i = 1; i <= 3; i++) {
            BlockPos blockPos = pos.above(i);
            world.setBlockAndUpdate(blockPos, state.setValue(PART, i));
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel world, BlockPos pos, boolean moved) {
        super.affectNeighborsAfterRemoval(state, world, pos, moved);

        BlockPos blockPos = pos;
        if (state.getValue(PART) != 0) {
            blockPos = pos.below(state.getValue(PART));
        }

        for (int i = 0; i <= 3; i++) {
            BlockPos blockPos1 = blockPos.above(i);
            BlockState atPosState = world.getBlockState(blockPos1);
            if (atPosState.is(this)) {
                world.destroyBlock(blockPos1, false);
            }
        }
    }
}
