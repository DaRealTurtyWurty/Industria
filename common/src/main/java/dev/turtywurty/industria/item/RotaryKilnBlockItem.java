package dev.turtywurty.industria.item;

import dev.turtywurty.industria.block.RotaryKilnBlock;
import dev.turtywurty.industria.blockentity.RotaryKilnControllerBlockEntity;
import dev.turtywurty.industria.init.ModBlocks;
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
        super(ModBlocks.ROTARY_KILN_CONTROLLER.get(), settings);
    }

    @Nullable
    protected BlockState getPlacementState(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection();
        BlockPos infrontPos = context.getClickedPos().relative(facing);
        BlockState infrontState = context.getLevel().getBlockState(infrontPos);

        BlockState toPlace;
        if (infrontState.is(ModBlocks.ROTARY_KILN.get())) {
            int segmentIndex = infrontState.getValue(RotaryKilnBlock.SEGMENT_INDEX);
            if (segmentIndex < 15) {
                toPlace = infrontState.setValue(RotaryKilnBlock.SEGMENT_INDEX, segmentIndex + 1);
            } else {
                toPlace = ModBlocks.ROTARY_KILN_CONTROLLER.get().defaultBlockState();
            }
        } else if (infrontState.is(ModBlocks.ROTARY_KILN_CONTROLLER.get())) {
            toPlace = ModBlocks.ROTARY_KILN.get().defaultBlockState().setValue(RotaryKilnBlock.SEGMENT_INDEX, 1);
        } else {
            toPlace = ModBlocks.ROTARY_KILN_CONTROLLER.get().defaultBlockState();
        }

        if (toPlace.getBlock() != ModBlocks.ROTARY_KILN_CONTROLLER.get()) {
            BlockPos controllerPos = infrontPos.relative(facing, toPlace.getValue(RotaryKilnBlock.SEGMENT_INDEX) - 1);
            if (context.getLevel().getBlockEntity(controllerPos) instanceof RotaryKilnControllerBlockEntity blockEntity) {
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
