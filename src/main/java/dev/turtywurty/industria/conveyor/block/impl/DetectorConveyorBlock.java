package dev.turtywurty.industria.conveyor.block.impl;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.conveyor.ConveyorItem;
import dev.turtywurty.industria.conveyor.ConveyorNetwork;
import dev.turtywurty.industria.conveyor.ConveyorNetworkManager;
import dev.turtywurty.industria.conveyor.ConveyorStorage;
import dev.turtywurty.industria.conveyor.block.ConveyorInput;
import dev.turtywurty.industria.conveyor.block.ConveyorOutput;
import dev.turtywurty.industria.conveyor.block.ConveyorTopology;
import dev.turtywurty.industria.conveyor.block.impl.entity.DetectorConveyorBlockEntity;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class DetectorConveyorBlock extends AbstractHorizontalConveyorBlock {
    public DetectorConveyorBlock(BlockBehaviour.Properties settings) {
        super(settings, new IndustriaBlock.BlockProperties()
                .hasComparatorOutput()
                .comparatorOutput((state, level, pos, direction) -> getSignalStrength(state, level, pos))
                .blockEntityProperties(new IndustriaBlock.BlockProperties.BlockBlockEntityProperties<>(() -> BlockEntityTypeInit.DETECTOR_CONVEYOR)
                        .shouldTick()
                        .rightClickToOpenGui())
        );
    }

    @Override
    public int getSpeed(Level level, BlockPos pos, BlockState state) {
        return 5;
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    public static int getSignalStrength(BlockState state, BlockGetter level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)
                || !(state.getBlock() instanceof DetectorConveyorBlock detectorConveyor))
            return 0;

        ConveyorNetwork networkAt = detectorConveyor.getNetworkManager(serverLevel).getNetworkAt(pos);
        if (networkAt == null)
            return 0;

        ConveyorStorage storageAt = networkAt.getStorage().getStorageAt(level, pos);
        if (storageAt == null)
            return 0;

        if (level.getBlockEntity(pos) instanceof DetectorConveyorBlockEntity blockEntity) {
            for (ConveyorItem item : storageAt.getItems()) {
                if (blockEntity.doesMatchFilter(item.getStack()))
                    return 15;
            }

            return 0;
        }

        return !storageAt.getItems().isEmpty() ? 15 : 0;
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

        return getSignalStrength(state, level, pos);
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return canEmitTowards(state, direction) ? getSignalStrength(state, level, pos) : 0;
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
