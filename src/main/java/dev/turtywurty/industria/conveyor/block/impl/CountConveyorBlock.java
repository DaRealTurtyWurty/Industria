package dev.turtywurty.industria.conveyor.block.impl;

import dev.turtywurty.industria.conveyor.ConveyorItem;
import dev.turtywurty.industria.conveyor.block.ConveyorInput;
import dev.turtywurty.industria.conveyor.block.ConveyorOutput;
import dev.turtywurty.industria.conveyor.block.ConveyorRoutingState;
import dev.turtywurty.industria.conveyor.block.ConveyorTopology;
import dev.turtywurty.industria.conveyor.block.impl.entity.CountConveyorBlockEntity;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

public class CountConveyorBlock extends AbstractHorizontalConveyorBlock {
    public CountConveyorBlock(Properties settings) {
        super(settings, new BlockProperties()
                .hasComparatorOutput()
                .comparatorOutput((_, level, pos, _) -> getSignalStrength(level, pos))
                .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(() -> BlockEntityTypeInit.COUNT_CONVEYOR)
                        .shouldTick())
        );
    }

    @Override
    public void onOutputUsed(Level level, BlockPos pos, BlockState state, ConveyorOutput output, ConveyorItem item, ConveyorRoutingState routingState) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof CountConveyorBlockEntity blockEntity) {
            blockEntity.increaseCount();
        }
    }

    @Override
    public int getSpeed(Level level, BlockPos pos, BlockState state) {
        return 5;
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    public static int getSignalStrength(BlockGetter level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof CountConveyorBlockEntity blockEntity
                && blockEntity.isPowered() ? 15 : 0;
    }

    public static void updateRedstoneOutput(Level level, BlockPos pos, BlockState state) {
        Block block = state.getBlock();
        level.updateNeighborsAt(pos, block);
        level.updateNeighborsAt(pos.below(), block);
        level.updateNeighbourForOutputSignal(pos, block);
    }

    private static boolean canEmitTowards(BlockState state, Direction direction) {
        Direction facing = state.getValue(FACING);
        return direction == Direction.DOWN
                || direction == facing.getClockWise()
                || direction == facing.getCounterClockWise();
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        if (!canEmitTowards(state, direction))
            return 0;

        return getSignalStrength(level, pos);
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return canEmitTowards(state, direction) ? getSignalStrength(level, pos) : 0;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof CountConveyorBlockEntity blockEntity) {
            int threshold = blockEntity.getThreshold();
            boolean decrease = player.isShiftKeyDown();

            // Loops back around if the threshold goes below 1 or above CountConveyorBlockEntity.MAX_THRESHOLD
            threshold = decrease
                    ? (threshold <= 1 ? CountConveyorBlockEntity.MAX_THRESHOLD : threshold - 1)
                    : (threshold >= CountConveyorBlockEntity.MAX_THRESHOLD ? 1 : threshold + 1);
            blockEntity.setThreshold(threshold);
            player.sendOverlayMessage(Component.literal("Count Threshold: " + blockEntity.getThreshold() + " items"));

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    public ConveyorTopology getTopology(Level level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(FACING);
        Direction back = facing.getOpposite();

        return new ConveyorTopology(
                List.of(new ConveyorInput("in", pos.relative(back))),
                List.of(new ConveyorOutput("out", pos.relative(facing), pos))
        );
    }
}
