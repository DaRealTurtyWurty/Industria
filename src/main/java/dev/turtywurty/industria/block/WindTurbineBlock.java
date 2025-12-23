package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.blockentity.WindTurbineBlockEntity;
import dev.turtywurty.industria.blockentity.util.TickableBlockEntity;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WindTurbineBlock extends IndustriaBlock {
    public static final IntProperty PART = IntProperty.of("part", 0, 3);

    private static final Map<Direction, List<VoxelShape>> SHAPES = new HashMap<>();

    public WindTurbineBlock(Settings settings) {
        super(settings, new BlockProperties()
                .hasHorizontalFacing()
                .addStateProperty(PART, 0)
                .hasBlockEntityRenderer()
                .shapeFactory((state, world, pos, context) ->
                        SHAPES.get(state.get(Properties.HORIZONTAL_FACING)).get(state.get(PART)))
                .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(() -> BlockEntityTypeInit.WIND_TURBINE)
                        .blockEntityFactory((pos, state) ->
                                state.get(PART) == 0 ? BlockEntityTypeInit.WIND_TURBINE.instantiate(pos, state) : null)
                        .shouldTick()
                        .blockEntityTickerFactory((world, state, type) ->
                                state.get(PART) == 0 ? TickableBlockEntity.createTicker(world) : null)));

        VoxelShape shape = createShape();
        Map<Direction, VoxelShape> directionalShapes = runShapeCalculation(shape);
        for (Direction direction : Direction.values()) {
            VoxelShape directionalShape = directionalShapes.get(direction);
            for (int part = 0; part <= 3; part++) {
                SHAPES.computeIfAbsent(direction, direction1 -> new ArrayList<>())
                        .add(directionalShape.offset(0, -part, 0));
            }
        }
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient()) {
            BlockPos blockEntityPos = pos;
            if (state.get(PART) != 0) {
                blockEntityPos = pos.down(state.get(PART));
            }

            if (world.getBlockEntity(blockEntityPos) instanceof WindTurbineBlockEntity windTurbine) {
                player.openHandledScreen(windTurbine);
            }
        }

        return ActionResult.SUCCESS;
    }

    private static Map<Direction, VoxelShape> runShapeCalculation(VoxelShape shape) {
        Map<Direction, VoxelShape> shapes = new HashMap<>();
        for (final Direction direction : Direction.values()) {
            shapes.put(direction, calculateShapes(direction, shape));
        }

        return shapes;
    }

    private static VoxelShape calculateShapes(Direction to, VoxelShape shape) {
        final VoxelShape[] buffer = {shape, VoxelShapes.empty()};

        final int times = (to.getHorizontalQuarterTurns() - Direction.NORTH.getHorizontalQuarterTurns() + 4) % 4;
        for (int i = 0; i < times; i++) {
            buffer[0].forEachBox((minX, minY, minZ, maxX, maxY, maxZ) ->
                    buffer[1] = VoxelShapes.union(buffer[1],
                            VoxelShapes.cuboid(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX)));
            buffer[0] = buffer[1];
            buffer[1] = VoxelShapes.empty();
        }

        return buffer[0];
    }

    private static VoxelShape createShape() {
        VoxelShape shape = VoxelShapes.empty();
        shape = VoxelShapes.combine(shape, VoxelShapes.cuboid(0.0625, 0, 0.0625, 0.9375, 0.125, 0.9375), BooleanBiFunction.OR);
        shape = VoxelShapes.combine(shape, VoxelShapes.cuboid(0.125, 0.125, 0.125, 0.875, 0.8125, 0.875), BooleanBiFunction.OR);
        shape = VoxelShapes.combine(shape, VoxelShapes.cuboid(0.25, 0.8125, 0.25, 0.75, 2.125, 0.75), BooleanBiFunction.OR);
        shape = VoxelShapes.combine(shape, VoxelShapes.cuboid(0.3125, 3.5, 0.125, 0.6875, 3.875, 0.8125), BooleanBiFunction.OR);
        shape = VoxelShapes.combine(shape, VoxelShapes.cuboid(0.4375, 0, 0.4375, 0.5625, 0.125, 0.5625), BooleanBiFunction.OR);
        shape = VoxelShapes.combine(shape, VoxelShapes.cuboid(0.375, 3.5625, 0.0625, 0.625, 3.8125, 0.125), BooleanBiFunction.OR);
        shape = VoxelShapes.combine(shape, VoxelShapes.cuboid(0.375, 2.125, 0.375, 0.625, 3.5, 0.625), BooleanBiFunction.OR);
        shape = VoxelShapes.combine(shape, VoxelShapes.cuboid(0.4375, 3.625, 0, 0.5625, 3.75, 0.0625), BooleanBiFunction.OR);

        return shape.simplify();
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        if (state.get(PART) != 0)
            return;

        for (int i = 1; i <= 3; i++) {
            BlockPos blockPos = pos.up(i);
            world.setBlockState(blockPos, state.with(PART, i));
        }
    }

    @Override
    protected void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean moved) {
        super.onStateReplaced(state, world, pos, moved);

        BlockPos blockPos = pos;
        if (state.get(PART) != 0) {
            blockPos = pos.down(state.get(PART));
        }

        for (int i = 0; i <= 3; i++) {
            BlockPos blockPos1 = blockPos.up(i);
            BlockState atPosState = world.getBlockState(blockPos1);
            if (atPosState.isOf(this)) {
                world.breakBlock(blockPos1, false);
            }
        }
    }
}
