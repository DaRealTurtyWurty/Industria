package dev.turtywurty.industria.conveyor.block.impl;

import dev.turtywurty.industria.conveyor.ConveyorItem;
import dev.turtywurty.industria.conveyor.ConveyorNetwork;
import dev.turtywurty.industria.conveyor.ConveyorNetworkManager;
import dev.turtywurty.industria.conveyor.block.ConveyorInput;
import dev.turtywurty.industria.conveyor.block.ConveyorOutput;
import dev.turtywurty.industria.conveyor.block.ConveyorRoutingState;
import dev.turtywurty.industria.conveyor.block.ConveyorTopology;
import dev.turtywurty.industria.persistent.LevelConveyorNetworks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.Orientation;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class AlternatorConveyorBlock extends AbstractHorizontalConveyorBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final String LEFT_OUTPUT_ID = "left";
    public static final String RIGHT_OUTPUT_ID = "right";

    public AlternatorConveyorBlock(Properties settings) {
        super(settings, new BlockProperties()
                .addStateProperty(POWERED, false));
    }

    @Override
    protected BlockState applyPlacementState(BlockState state, BlockPlaceContext context, Direction facing) {
        return super.applyPlacementState(state, context, facing)
                .setValue(POWERED, context.getLevel().hasNeighborSignal(context.getClickedPos()));
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, block, orientation, movedByPiston);

        if (level.isClientSide())
            return;

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.scheduleTick(pos, this, 1);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        BlockState current = level.getBlockState(pos);
        if (!current.is(this))
            return;

        BlockState resolved = resolveState(level, pos, current);
        if (resolved == current)
            return;

        level.setBlock(pos, resolved, Block.UPDATE_ALL);

        ConveyorNetworkManager manager = getNetworkManager(level);
        if (manager == null)
            return;

        manager.recreateNetworkAt(level, pos);
        ConveyorNetwork network = manager.getNetworkAt(pos);
        if (network != null) {
            manager.syncNetwork(level, network);
            LevelConveyorNetworks.getOrCreate(level).setDirty();
        }
    }

    @Override
    protected BlockState resolveState(ServerLevel level, BlockPos pos, BlockState current) {
        return super.resolveState(level, pos, current)
                .setValue(POWERED, level.hasNeighborSignal(pos));
    }

    @Override
    public int getSpeed(Level level, BlockPos pos, BlockState state) {
        return 5;
    }

    @Override
    public ConveyorTopology getTopology(Level level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(FACING);
        Direction back = facing.getOpposite();
        Direction left = facing.getCounterClockWise();
        Direction right = facing.getClockWise();

        return new ConveyorTopology(
                List.of(new ConveyorInput("in", pos.relative(back))),
                List.of(
                        new ConveyorOutput(LEFT_OUTPUT_ID, pos.relative(left), pos),
                        new ConveyorOutput(RIGHT_OUTPUT_ID, pos.relative(right), pos)
                )
        );
    }

    @Override
    public ConveyorOutput selectOutput(Level level, BlockPos pos, BlockState state, ConveyorItem item, ConveyorNetwork network, ConveyorRoutingState routingState) {
        ConveyorTopology topology = getTopology(level, pos, state);
        if (topology.outputs().isEmpty())
            return null;

        String outputId = state.getValue(POWERED) ? RIGHT_OUTPUT_ID : LEFT_OUTPUT_ID;
        for (ConveyorOutput output : topology.outputs()) {
            if (output.id().equals(outputId)) {
                item.setSelectedOutputId(output.id());
                return output;
            }
        }

        return null;
    }
}
