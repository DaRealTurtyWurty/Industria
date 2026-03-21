package dev.turtywurty.industria.conveyor.block.impl;

import dev.turtywurty.industria.conveyor.ConveyorItem;
import dev.turtywurty.industria.conveyor.ConveyorNetwork;
import dev.turtywurty.industria.conveyor.ConveyorNetworkManager;
import dev.turtywurty.industria.conveyor.ConveyorStorage;
import dev.turtywurty.industria.conveyor.block.*;
import dev.turtywurty.industria.persistent.LevelConveyorNetworks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class LadderConveyorBlock extends BaseConveyorBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty UPWARD = BooleanProperty.create("upward");
    public static final EnumProperty<LinePosition> LINE_POSITION = EnumProperty.create("line_position", LinePosition.class);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final Property<Boolean> ENABLED = BooleanProperty.create("enabled");

    public LadderConveyorBlock(Properties settings) {
        super(settings, new BlockProperties()
                .addStateProperty(FACING, Direction.NORTH)
                .addStateProperty(WATERLOGGED, false)
                .addStateProperty(ENABLED, true)
                .addStateProperty(UPWARD, false)
                .addStateProperty(LINE_POSITION, LinePosition.SINGLE)
                .notPlaceFacingOpposite()
                .hasComparatorOutput()
        );
    }

    @Override
    protected InteractionResult useItemOn(ItemStack itemStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (itemStack.isEmpty()) {
            if (player.isShiftKeyDown() && hand == InteractionHand.MAIN_HAND) {
                if (level.isClientSide())
                    return InteractionResult.SUCCESS;

                if (level instanceof ServerLevel serverLevel) {
                    ConveyorNetworkManager networkManager = getNetworkManager(serverLevel);
                    boolean recreated = networkManager.recreateNetworkAt(serverLevel, pos);
                    player.sendSystemMessage(Component.literal(recreated
                            ? "Recreated conveyor network."
                            : "Failed to recreate conveyor network."));

                    return InteractionResult.SUCCESS_SERVER;
                }

                return InteractionResult.SUCCESS_SERVER;
            }

            return super.useItemOn(itemStack, state, level, pos, player, hand, hitResult);
        }

        if (level.isClientSide())
            return InteractionResult.SUCCESS;

        if (level instanceof ServerLevel serverLevel) {
            ConveyorNetworkManager networkManager = getNetworkManager(serverLevel);
            ConveyorNetwork networkAt = networkManager.getNetworkAt(pos);
            if (networkAt == null) {
                networkManager.traverseCreateNetwork(serverLevel, pos);
                networkAt = networkManager.getNetworkAt(pos);
            }

            if (networkAt == null)
                return InteractionResult.SUCCESS_SERVER;

            ConveyorStorage storageAt = networkAt.getStorage().getStorageAt(level, pos);
            if (storageAt == null || !storageAt.canAcceptIncomingItem())
                return InteractionResult.PASS;

            ConveyorItem conveyorItem = new ConveyorItem(pos, itemStack.copyWithCount(1));
            if (!storageAt.addItem(conveyorItem))
                return InteractionResult.PASS;

            selectOutput(level, pos, state, conveyorItem, networkAt, networkManager);

            if (!player.isCreative())
                itemStack.shrink(1);

            networkManager.syncNetwork(serverLevel, networkAt);
            LevelConveyorNetworks.getOrCreate(serverLevel).setDirty();
            return InteractionResult.CONSUME;
        }

        return super.useItemOn(itemStack, state, level, pos, player, hand, hitResult);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        FluidState fluidState = level.getFluidState(pos);
        Direction facing = resolvePlacementFacing(level, pos, context);
        boolean upward = resolvePlacementUpward(level, pos, context);

        return defaultBlockState()
                .setValue(FACING, facing)
                .setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER)
                .setValue(ENABLED, !level.hasNeighborSignal(pos))
                .setValue(UPWARD, upward)
                .setValue(LINE_POSITION, resolveLinePosition(level, pos, facing, upward));
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);

        if (level.isClientSide() || oldState.is(this))
            return;

        if (level instanceof ServerLevel serverLevel) {
            getNetworkManager(serverLevel).placeConveyor(serverLevel, pos);
            serverLevel.scheduleTick(pos, this, 1);
        }
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
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
                                     Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourState,
                                     RandomSource random) {
        if (state.getValue(WATERLOGGED)) {
            ticks.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        ticks.scheduleTick(pos, this, 1);
        return super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        BlockState current = level.getBlockState(pos);
        if (!current.is(this))
            return;

        BlockState resolved = resolveState(level, pos, current);

        if (resolved != current) {
            level.setBlock(pos, resolved, Block.UPDATE_ALL);
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        BlockState newState = level.getBlockState(pos);
        if (!state.is(newState.getBlock())) {
            getNetworkManager(level).removeConveyor(level, pos);
        }

        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED)
                ? Fluids.WATER.getSource(false)
                : super.getFluidState(state);
    }

    public ConveyorNetworkManager getNetworkManager(ServerLevel level) {
        return LevelConveyorNetworks.getOrCreate(level).getNetworkManager();
    }

    @Override
    public int getItemLimit(BlockGetter level, BlockPos pos, BlockState state) {
        return 3;
    }

    @Override
    public int getSpeed(Level level, BlockPos pos, BlockState state) {
        return isEnabled(level, pos, state) ? 2 : 0;
    }

    @Override
    public boolean isEnabled(Level level, BlockPos pos, BlockState state) {
        return state.getValue(ENABLED);
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

    private BlockState resolveState(Level level, BlockPos pos, BlockState current) {
        boolean waterlogged = level.getFluidState(pos).getType() == Fluids.WATER;
        boolean enabled = !level.hasNeighborSignal(pos);
        Direction facing = resolveFacingFromNeighbors(level, pos, current.getValue(FACING));
        boolean upward = resolveUpwardFromNeighbors(level, pos, current.getValue(UPWARD));
        LinePosition linePosition = resolveLinePosition(level, pos, facing, upward);

        return current
                .setValue(FACING, facing)
                .setValue(WATERLOGGED, waterlogged)
                .setValue(ENABLED, enabled)
                .setValue(UPWARD, upward)
                .setValue(LINE_POSITION, linePosition);
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
