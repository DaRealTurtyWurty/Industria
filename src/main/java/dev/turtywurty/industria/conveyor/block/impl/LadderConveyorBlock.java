package dev.turtywurty.industria.conveyor.block.impl;

import dev.turtywurty.industria.conveyor.ConveyorItem;
import dev.turtywurty.industria.conveyor.ConveyorNetwork;
import dev.turtywurty.industria.conveyor.block.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class LadderConveyorBlock extends AbstractPoweredConveyorBlock {
    public static final BooleanProperty UPWARD = BooleanProperty.create("upward");
    public static final EnumProperty<LinePosition> LINE_POSITION = EnumProperty.create("line_position", LinePosition.class);

    public LadderConveyorBlock(Properties settings) {
        super(settings, new BlockProperties()
                .addStateProperty(UPWARD, false)
                .addStateProperty(LINE_POSITION, LinePosition.SINGLE)
        );
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction facing = resolvePlacementFacing(level, pos, context);
        boolean upward = resolvePlacementUpward(level, pos, context);

        return applyPlacementState(defaultBlockState().setValue(UPWARD, upward), context, facing)
                .setValue(LINE_POSITION, resolveLinePosition(level, pos, facing, upward));
    }

    @Override
    public ConveyorOutput selectOutput(Level level, BlockPos pos, BlockState state, ConveyorItem item, ConveyorNetwork network, ConveyorRoutingState routingState) {
        ConveyorTopology topology = getTopology(level, pos, state);
        ConveyorOutput verticalOutput = null;
        ConveyorOutput horizontalOutput = null;
        for (ConveyorOutput output : topology.outputs()) {
            if ("vertical".equals(output.id())) {
                verticalOutput = output;
            } else if ("horizontal".equals(output.id())) {
                horizontalOutput = output;
            }
        }

        if (verticalOutput != null && isUsableOutput(level, pos, state, verticalOutput, network))
            return verticalOutput;

        if (horizontalOutput != null && isUsableOutput(level, pos, state, horizontalOutput, network))
            return horizontalOutput;

        return verticalOutput != null ? verticalOutput : horizontalOutput;
    }

    @Override
    public ConveyorTopology getTopology(Level level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(FACING);
        boolean upward = state.getValue(UPWARD);

        BlockPos verticalInput = upward ? pos.below() : pos.above();
        BlockPos verticalOutput = upward ? pos.above() : pos.below();
        BlockPos horizontalInput = pos.relative(facing.getOpposite());
        BlockPos horizontalOutput = pos.relative(facing);

        return new ConveyorTopology(
                List.of(
                        new ConveyorInput("vertical", verticalInput),
                        new ConveyorInput("horizontal", horizontalInput)
                ),
                List.of(
                        new ConveyorOutput("vertical", verticalOutput, pos),
                        new ConveyorOutput("horizontal", horizontalOutput, pos)
                )
        );
    }

    private boolean isUsableOutput(Level level, BlockPos pos, BlockState state, ConveyorOutput output, ConveyorNetwork network) {
        if (network.getConnectedBlock(pos, output) != null)
            return true;

        for (BlockPos targetPos : List.of(output.deliveryPos(), output.deliveryPos().above(), output.deliveryPos().below())) {
            BlockState targetState = level.getBlockState(targetPos);
            if (!(targetState.getBlock() instanceof ConveyorLike))
                continue;

            if (canConnectToConveyor(level, pos, state, output, targetPos, targetState))
                return true;
        }

        return false;
    }

    @Override
    protected BlockState resolveState(ServerLevel level, BlockPos pos, BlockState current) {
        BlockState resolved = super.resolveState(level, pos, current);
        Direction facing = resolveFacingFromNeighbors(level, pos, current.getValue(FACING));
        boolean upward = resolveUpwardFromNeighbors(level, pos, current.getValue(UPWARD));
        LinePosition linePosition = resolveLinePosition(level, pos, facing, upward);

        return resolved
                .setValue(FACING, facing)
                .setValue(UPWARD, upward)
                .setValue(LINE_POSITION, linePosition);
    }

    @Override
    protected int getEnabledSpeed(Level level, BlockPos pos, BlockState state) {
        return 2;
    }

    private Direction resolvePlacementFacing(Level level, BlockPos pos, BlockPlaceContext context) {
        return resolveFacingFromNeighbors(level, pos, context.getHorizontalDirection());
    }

    private boolean resolvePlacementUpward(Level level, BlockPos pos, BlockPlaceContext context) {
        return resolveUpwardFromNeighbors(level, pos, getPlacementUpwardFallback(context));
    }

    private Direction resolveFacingFromNeighbors(Level level, BlockPos pos, Direction fallbackFacing) {
        BlockState belowState = level.getBlockState(pos.below());
        if (belowState.getBlock() instanceof LadderConveyorBlock) {
            Direction belowFacing = getNeighborFacing(belowState);
            if (belowFacing != null)
                return belowFacing;
        }

        BlockState aboveState = level.getBlockState(pos.above());
        if (aboveState.getBlock() instanceof LadderConveyorBlock) {
            Direction aboveFacing = getNeighborFacing(aboveState);
            if (aboveFacing != null)
                return aboveFacing;
        }

        Direction bestFacing = fallbackFacing;
        int bestScore = scoreFacingCandidate(level, pos, fallbackFacing);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            int candidateScore = scoreFacingCandidate(level, pos, direction);
            if (candidateScore > bestScore) {
                bestScore = candidateScore;
                bestFacing = direction;
            }
        }

        return bestFacing;
    }

    private int scoreFacingCandidate(Level level, BlockPos pos, Direction facing) {
        int score = 0;

        BlockPos inputPos = pos.relative(facing.getOpposite());
        BlockState inputState = level.getBlockState(inputPos);
        if (inputState.getBlock() instanceof ConveyorLike) {
            score += 2;
            if (outputsToPosition(level, inputPos, inputState, pos)) {
                score += 8;
            }
        }

        BlockPos outputPos = pos.relative(facing);
        BlockState outputState = level.getBlockState(outputPos);
        if (outputState.getBlock() instanceof ConveyorLike) {
            score += 1;
            if (acceptsInputFrom(level, outputPos, outputState, pos)) {
                score += 6;
            }
        }

        return score;
    }

    private static boolean outputsToPosition(Level level, BlockPos conveyorPos, BlockState conveyorState, BlockPos targetPos) {
        if (!(conveyorState.getBlock() instanceof ConveyorLike conveyor))
            return false;

        ConveyorTopology topology = conveyor.getTopology(level, conveyorPos, conveyorState);
        return topology.outputs().stream().anyMatch(output -> output.deliveryPos().equals(targetPos));
    }

    private static boolean acceptsInputFrom(Level level, BlockPos conveyorPos, BlockState conveyorState, BlockPos sourcePos) {
        if (!(conveyorState.getBlock() instanceof ConveyorLike conveyor))
            return false;

        ConveyorTopology topology = conveyor.getTopology(level, conveyorPos, conveyorState);
        return topology.acceptsInputFrom(sourcePos);
    }

    private boolean resolveUpwardFromNeighbors(Level level, BlockPos pos, boolean fallbackUpward) {
        BlockState belowState = level.getBlockState(pos.below());
        if (belowState.getBlock() instanceof LadderConveyorBlock)
            return belowState.getValue(UPWARD);

        BlockState aboveState = level.getBlockState(pos.above());
        if (aboveState.getBlock() instanceof LadderConveyorBlock)
            return aboveState.getValue(UPWARD);

        boolean hasConveyorBelow = belowState.getBlock() instanceof ConveyorLike;
        boolean hasConveyorAbove = aboveState.getBlock() instanceof ConveyorLike;
        if (hasConveyorBelow != hasConveyorAbove)
            return hasConveyorBelow;

        return fallbackUpward;
    }

    private static LinePosition resolveLinePosition(Level level, BlockPos pos, Direction facing, boolean upward) {
        boolean matchesBelow = isMatchingLineSegment(level.getBlockState(pos.below()), facing, upward);
        boolean matchesAbove = isMatchingLineSegment(level.getBlockState(pos.above()), facing, upward);
        if (matchesBelow && matchesAbove)
            return LinePosition.MIDDLE;
        if (matchesBelow)
            return LinePosition.TOP;
        if (matchesAbove)
            return LinePosition.BOTTOM;
        return LinePosition.SINGLE;
    }

    private static boolean isMatchingLineSegment(BlockState state, Direction facing, boolean upward) {
        return state.getBlock() instanceof LadderConveyorBlock
                && state.getValue(FACING) == facing
                && state.getValue(UPWARD) == upward;
    }

    private static boolean getPlacementUpwardFallback(BlockPlaceContext context) {
        return context.getPlayer() == null || !context.getPlayer().isShiftKeyDown();
    }

    @Nullable
    private static Direction getNeighborFacing(BlockState state) {
        if (state.hasProperty(FACING))
            return state.getValue(FACING);

        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING))
            return state.getValue(BlockStateProperties.HORIZONTAL_FACING);

        return null;
    }

    public enum LinePosition implements StringRepresentable {
        SINGLE("single"),
        BOTTOM("bottom"),
        MIDDLE("middle"),
        TOP("top");

        private final String serializedName;

        LinePosition(String serializedName) {
            this.serializedName = serializedName;
        }

        @Override
        public String getSerializedName() {
            return this.serializedName;
        }
    }
}
