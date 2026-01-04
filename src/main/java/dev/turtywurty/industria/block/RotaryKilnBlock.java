package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.blockentity.RotaryKilnControllerBlockEntity;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.MultiblockTypeInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RotaryKilnBlock extends IndustriaBlock {
    public static final IntegerProperty SEGMENT_INDEX = IntegerProperty.create("segment_index", 1, 15);

    public RotaryKilnBlock(Properties settings) {
        super(settings, new BlockProperties()
                .hasComparatorOutput()
                .hasHorizontalFacing()
                .shapeFactory((state, world, pos, context) ->
                        getVoxelShape(world, pos, state.getValue(BlockStateProperties.HORIZONTAL_FACING)))
                .hasBlockEntityRenderer()
                .addStateProperty(SEGMENT_INDEX, 1)
                .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(() -> BlockEntityTypeInit.ROTARY_KILN)
                        .multiblockProperties(MultiblockTypeInit.ROTARY_KILN).build()));
    }

    public static VoxelShape getVoxelShape(BlockGetter world, BlockPos pos, Direction facing) {
        VoxelShape[] shapes = RotaryKilnControllerBlock.SHAPES.get(facing);
        if (shapes == null)
            return Shapes.empty();

        BlockState state = world.getBlockState(pos);
        int segmentIndex = state.hasProperty(SEGMENT_INDEX) ? state.getValue(SEGMENT_INDEX) : 0;

        if (segmentIndex >= shapes.length)
            return Shapes.empty();

        BlockPos controllerPos = pos.relative(facing.getOpposite(), segmentIndex);
        BlockState primaryState = world.getBlockState(controllerPos);
        VoxelShape shape = primaryState.getShape(world, controllerPos, null);

        return shape.move(
                -facing.getStepX() * segmentIndex,
                -facing.getStepY() * segmentIndex,
                -facing.getStepZ() * segmentIndex
        );
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel world, BlockPos pos, boolean moved) {
        BlockState newState = world.getBlockState(pos);
        if (!state.is(newState.getBlock()) && !world.isClientSide()) {
            BlockPos controllerPos = pos.relative(state.getValue(BlockStateProperties.HORIZONTAL_FACING).getOpposite());
            BlockState controllerState = world.getBlockState(controllerPos);

            BlockEntity blockEntity = null;
            if (controllerState.is(BlockInit.ROTARY_KILN_CONTROLLER)) {
                blockEntity = world.getBlockEntity(controllerPos);
            } else if (controllerState.is(this)) {
                BlockPos actualControllerPos = controllerPos.relative(controllerState.getValue(BlockStateProperties.HORIZONTAL_FACING).getOpposite(), controllerState.getValue(SEGMENT_INDEX));
                blockEntity = world.getBlockEntity(actualControllerPos);
            }

            if (blockEntity instanceof RotaryKilnControllerBlockEntity controllerBlockEntity) {
                controllerBlockEntity.removeKilnSegment(pos);
            }
        }

        super.affectNeighborsAfterRemoval(state, world, pos, moved);
    }
}
