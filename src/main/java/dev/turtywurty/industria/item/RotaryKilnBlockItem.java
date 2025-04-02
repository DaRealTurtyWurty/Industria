package dev.turtywurty.industria.item;

import dev.turtywurty.industria.block.RotaryKilnBlock;
import dev.turtywurty.industria.blockentity.RotaryKilnControllerBlockEntity;
import dev.turtywurty.industria.init.BlockInit;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

public class RotaryKilnBlockItem extends BlockItem {
    public RotaryKilnBlockItem(Item.Settings settings) {
        super(BlockInit.ROTARY_KILN_CONTROLLER, settings);
    }

    @Nullable
    protected BlockState getPlacementState(ItemPlacementContext context) {
        Direction facing = context.getHorizontalPlayerFacing();
        BlockPos infrontPos = context.getBlockPos().offset(facing);
        BlockState infrontState = context.getWorld().getBlockState(infrontPos);

        BlockState toPlace;
        if(infrontState.isOf(BlockInit.ROTARY_KILN)) {
            int segmentIndex = infrontState.get(RotaryKilnBlock.SEGMENT_INDEX);
            if(segmentIndex < 15) {
                toPlace = infrontState.with(RotaryKilnBlock.SEGMENT_INDEX, segmentIndex + 1);
            } else {
                toPlace = BlockInit.ROTARY_KILN_CONTROLLER.getDefaultState();
            }
        } else if(infrontState.isOf(BlockInit.ROTARY_KILN_CONTROLLER)) {
            toPlace = BlockInit.ROTARY_KILN.getDefaultState().with(RotaryKilnBlock.SEGMENT_INDEX, 1);
        } else {
            toPlace = BlockInit.ROTARY_KILN_CONTROLLER.getDefaultState();
        }

        if(toPlace.getBlock() != BlockInit.ROTARY_KILN_CONTROLLER) {
            BlockPos controllerPos = infrontPos.offset(facing, toPlace.get(RotaryKilnBlock.SEGMENT_INDEX) - 1);
            if(context.getWorld().getBlockEntity(controllerPos) instanceof RotaryKilnControllerBlockEntity blockEntity) {
                blockEntity.addKilnSegment(context.getBlockPos());
            }
        }

        toPlace = toPlace.with(Properties.HORIZONTAL_FACING, facing.getOpposite());
        return toPlace != null && canPlace(context, toPlace) ? toPlace : null;
    }

    @Override
    public boolean canBeNested() {
        return true;
    }
}
