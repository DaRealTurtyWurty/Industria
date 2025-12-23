package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.blockentity.RotaryKilnControllerBlockEntity;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.MultiblockTypeInit;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class RotaryKilnBlock extends IndustriaBlock {
    public static final IntProperty SEGMENT_INDEX = IntProperty.of("segment_index", 1, 15);

    public RotaryKilnBlock(Settings settings) {
        super(settings, new BlockProperties()
                .hasComparatorOutput()
                .hasHorizontalFacing()
                .shapeFactory((state, world, pos, context) ->
                        getVoxelShape(world, pos, state.get(Properties.HORIZONTAL_FACING)))
                .hasBlockEntityRenderer()
                .addStateProperty(SEGMENT_INDEX, 1)
                .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(() -> BlockEntityTypeInit.ROTARY_KILN)
                        .multiblockProperties(MultiblockTypeInit.ROTARY_KILN).build()));
    }

    public static VoxelShape getVoxelShape(BlockView world, BlockPos pos, Direction facing) {
        VoxelShape[] shapes = RotaryKilnControllerBlock.SHAPES.get(facing);
        if (shapes == null)
            return VoxelShapes.empty();

        BlockState state = world.getBlockState(pos);
        int segmentIndex = state.contains(SEGMENT_INDEX) ? state.get(SEGMENT_INDEX) : 0;

        if (segmentIndex >= shapes.length)
            return VoxelShapes.empty();

        BlockPos controllerPos = pos.offset(facing.getOpposite(), segmentIndex);
        BlockState primaryState = world.getBlockState(controllerPos);
        VoxelShape shape = primaryState.getOutlineShape(world, controllerPos, null);

        return shape.offset(
                -facing.getOffsetX() * segmentIndex,
                -facing.getOffsetY() * segmentIndex,
                -facing.getOffsetZ() * segmentIndex
        );
    }

    @Override
    protected void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean moved) {
        BlockState newState = world.getBlockState(pos);
        if (!state.isOf(newState.getBlock()) && !world.isClient()) {
            BlockPos controllerPos = pos.offset(state.get(Properties.HORIZONTAL_FACING).getOpposite());
            BlockState controllerState = world.getBlockState(controllerPos);

            BlockEntity blockEntity = null;
            if (controllerState.isOf(BlockInit.ROTARY_KILN_CONTROLLER)) {
                blockEntity = world.getBlockEntity(controllerPos);
            } else if (controllerState.isOf(this)) {
                BlockPos actualControllerPos = controllerPos.offset(controllerState.get(Properties.HORIZONTAL_FACING).getOpposite(), controllerState.get(SEGMENT_INDEX));
                blockEntity = world.getBlockEntity(actualControllerPos);
            }

            if (blockEntity instanceof RotaryKilnControllerBlockEntity controllerBlockEntity) {
                controllerBlockEntity.removeKilnSegment(pos);
            }
        }

        super.onStateReplaced(state, world, pos, moved);
    }
}
