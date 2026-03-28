package dev.turtywurty.industria.conveyor.block.impl;

import dev.turtywurty.industria.conveyor.ConveyorItem;
import dev.turtywurty.industria.conveyor.ConveyorNetwork;
import dev.turtywurty.industria.conveyor.block.ConveyorInput;
import dev.turtywurty.industria.conveyor.block.ConveyorOutput;
import dev.turtywurty.industria.conveyor.block.ConveyorRoutingState;
import dev.turtywurty.industria.conveyor.block.ConveyorTopology;
import dev.turtywurty.industria.conveyor.block.impl.entity.DelayConveyorBlockEntity;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

public class DelayConveyorBlock extends AbstractHorizontalConveyorBlock {
    public DelayConveyorBlock(Properties settings) {
        super(settings, new BlockProperties()
                .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(() -> BlockEntityTypeInit.DELAY_CONVEYOR)
                        .shouldTick())
        );
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof DelayConveyorBlockEntity blockEntity) {
            int threshold = blockEntity.getThreshold();
            boolean decrease = player.isShiftKeyDown();

            // Loops back around if the threshold goes below 1 or above DelayConveyorBlockEntity.MAX_THRESHOLD. Increase/Decrease by 10.
            threshold = decrease
                    ? (threshold <= 10 ? DelayConveyorBlockEntity.MAX_THRESHOLD : threshold - 10)
                    : (threshold >= DelayConveyorBlockEntity.MAX_THRESHOLD ? 10 : threshold + 10);
            blockEntity.setThreshold(threshold);
            player.sendOverlayMessage(Component.literal("Delay: " + blockEntity.getThreshold() + " ticks (" + (blockEntity.getThreshold() / 20f) + " seconds)"));

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    public int getSpeed(Level level, BlockPos pos, BlockState state) {
        return 5;
    }

    @Override
    public ConveyorOutput selectOutput(Level level, BlockPos pos, BlockState state, ConveyorItem item, ConveyorNetwork network, ConveyorRoutingState routingState) {
        if (level.getBlockEntity(pos) instanceof DelayConveyorBlockEntity blockEntity) {
            long gameTime = level.getGameTime();
            if (blockEntity.isItemDelayed(item.getId(), gameTime))
                return null;
        }

        return getTopology(level, pos, state).outputs().getFirst();
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
