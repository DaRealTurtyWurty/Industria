package dev.turtywurty.industria.item;

import dev.turtywurty.industria.block.RotaryKilnBlock;
import dev.turtywurty.industria.blockentity.RotaryKilnControllerBlockEntity;
import dev.turtywurty.industria.init.BlockInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

public class RotaryKilnBlockItem extends BlockItem {
    public RotaryKilnBlockItem(Item.Properties settings) {
        super(BlockInit.ROTARY_KILN_CONTROLLER, settings);
    }

    @Nullable
    protected BlockState getPlacementState(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection();
        BlockPos infrontPos = context.getClickedPos().relative(facing);
        BlockState infrontState = context.getLevel().getBlockState(infrontPos);

        BlockState toPlace;
        if(infrontState.is(BlockInit.ROTARY_KILN)) {
            int segmentIndex = infrontState.getValue(RotaryKilnBlock.SEGMENT_INDEX);
            if(segmentIndex < 15) {
                toPlace = infrontState.setValue(RotaryKilnBlock.SEGMENT_INDEX, segmentIndex + 1);
            } else {
                toPlace = BlockInit.ROTARY_KILN_CONTROLLER.defaultBlockState();
            }
        } else if(infrontState.is(BlockInit.ROTARY_KILN_CONTROLLER)) {
            toPlace = BlockInit.ROTARY_KILN.defaultBlockState().setValue(RotaryKilnBlock.SEGMENT_INDEX, 1);
        } else {
            toPlace = BlockInit.ROTARY_KILN_CONTROLLER.defaultBlockState();
        }

        if(toPlace.getBlock() != BlockInit.ROTARY_KILN_CONTROLLER) {
            BlockPos controllerPos = infrontPos.relative(facing, toPlace.getValue(RotaryKilnBlock.SEGMENT_INDEX) - 1);
            if(context.getLevel().getBlockEntity(controllerPos) instanceof RotaryKilnControllerBlockEntity blockEntity) {
                blockEntity.addKilnSegment(context.getClickedPos());
            }
        }

        toPlace = toPlace.setValue(BlockStateProperties.HORIZONTAL_FACING, facing.getOpposite());
        return toPlace != null && canPlace(context, toPlace) ? toPlace : null;
    }

    @Override
    public boolean canFitInsideContainerItems() {
        return true;
    }
}
