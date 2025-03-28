package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.blockentity.RotaryKilnControllerBlockEntity;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.MultiblockTypeInit;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class RotaryKilnBlock extends IndustriaBlock {
    public static final VoxelShape VOXEL_SHAPE = VoxelShapes.fullCube();
    public static final IntProperty SEGMENT_INDEX = IntProperty.of("segment_index", 1, 16);

    public RotaryKilnBlock(Settings settings) {
        super(settings, new BlockProperties()
                .hasComparatorOutput()
                .hasHorizontalFacing()
                .useRotatedShapes(VOXEL_SHAPE)
                .hasBlockEntityRenderer()
                .addStateProperty(SEGMENT_INDEX, 1)
                .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(() -> BlockEntityTypeInit.ROTARY_KILN_CONTROLLER)
                        .multiblockProperties(MultiblockTypeInit.ROTARY_KILN_CONTROLLER).build()));
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        BlockPos controllerPos = pos.offset(state.get(Properties.HORIZONTAL_FACING).getOpposite());
        BlockState controllerState = world.getBlockState(controllerPos);
        if (controllerState.isOf(BlockInit.ROTARY_KILN_CONTROLLER)) {
            addKilnSegment(world, pos, controllerPos);
        } else if (controllerState.isOf(this)) {
            world.setBlockState(controllerPos, controllerState.with(SEGMENT_INDEX, controllerState.get(SEGMENT_INDEX) + 1));
            BlockPos actualControllerPos = controllerPos.offset(controllerState.get(Properties.HORIZONTAL_FACING).getOpposite(), controllerState.get(SEGMENT_INDEX));
            addKilnSegment(world, pos, actualControllerPos);
        }
    }

    private void addKilnSegment(World world, BlockPos pos, BlockPos controllerPos) {
        BlockEntity blockEntity = world.getBlockEntity(controllerPos);
        if (blockEntity instanceof RotaryKilnControllerBlockEntity controllerBlockEntity) {
            controllerBlockEntity.addKilnSegment(pos);
        }
    }
}
