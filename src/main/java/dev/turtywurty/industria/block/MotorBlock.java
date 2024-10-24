package dev.turtywurty.industria.block;

import com.mojang.serialization.MapCodec;
import dev.turtywurty.industria.blockentity.MotorBlockEntity;
import dev.turtywurty.industria.blockentity.util.TickableBlockEntity;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;

public class MotorBlock extends HorizontalFacingBlock implements BlockEntityProvider {
    private static final MapCodec<MotorBlock> CODEC = createCodec(MotorBlock::new);

    private static final EnumMap<Direction, VoxelShape> SHAPES = new EnumMap<>(Direction.class);

    public MotorBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(FACING, Direction.NORTH));

        runShapeCalculation(VoxelShapes.union(
                VoxelShapes.cuboid(0.25, 0, 0.25, 0.75, 0.4375, 0.75),
                VoxelShapes.cuboid(0.0625, 0.1875, 0.4375, 0.25, 0.3125, 0.5625)
        ).simplify());
    }

    private static void runShapeCalculation(VoxelShape shape) {
        for (final Direction direction : Direction.values()) {
            SHAPES.put(direction, calculateShapes(direction, shape));
        }
    }

    private static VoxelShape calculateShapes(Direction to, VoxelShape shape) {
        final VoxelShape[] buffer = {shape, VoxelShapes.empty()};

        final int times = (to.getHorizontal() - Direction.NORTH.getHorizontal() + 4) % 4;
        for (int i = 0; i < times; i++) {
            buffer[0].forEachBox((minX, minY, minZ, maxX, maxY, maxZ) ->
                    buffer[1] = VoxelShapes.union(buffer[1],
                            VoxelShapes.cuboid(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX)));
            buffer[0] = buffer[1];
            buffer[1] = VoxelShapes.empty();
        }

        return buffer[0];
    }

    @Override
    protected MapCodec<? extends HorizontalFacingBlock> getCodec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntityTypeInit.MOTOR.instantiate(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return TickableBlockEntity.createTicker(world);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(FACING);
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        return getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof MotorBlockEntity motorBlockEntity) {
                player.openHandledScreen(motorBlockEntity);
            }
        }

        return ActionResult.SUCCESS;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPES.get(state.get(FACING));
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }
}
