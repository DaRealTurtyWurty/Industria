package dev.turtywurty.industria.conveyor.block.impl;

import dev.turtywurty.industria.conveyor.ConveyorItem;
import dev.turtywurty.industria.conveyor.ConveyorNetwork;
import dev.turtywurty.industria.conveyor.ConveyorNetworkManager;
import dev.turtywurty.industria.conveyor.ConveyorStorage;
import dev.turtywurty.industria.conveyor.block.*;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.persistent.LevelConveyorNetworks;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
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

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class BasicConveyorBlock extends BaseConveyorBlock {
    public static final EnumProperty<ConveyorShape> SHAPE = EnumProperty.create("shape", ConveyorShape.class);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final Property<Boolean> ENABLED = BooleanProperty.create("enabled");

    public BasicConveyorBlock(Properties settings) {
        super(settings, new BlockProperties()
                .addStateProperty(SHAPE, ConveyorShape.STRAIGHT)
                .addStateProperty(FACING, Direction.NORTH)
                .addStateProperty(WATERLOGGED, false)
                .addStateProperty(ENABLED, true)
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
                    if (networkManager != null) {
                        boolean recreated = networkManager.recreateNetworkAt(serverLevel, pos);
                        if (recreated) {
                            player.sendSystemMessage(Component.literal("Recreated conveyor network."));
                        } else {
                            player.sendSystemMessage(Component.literal("Failed to recreate conveyor network."));
                        }
                    }

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
            if (networkManager == null)
                return InteractionResult.SUCCESS_SERVER;

            ConveyorNetwork networkAt = networkManager.getNetworkAt(pos);
            if (networkAt == null) {
                networkManager.traverseCreateNetwork(serverLevel, pos);
                networkAt = networkManager.getNetworkAt(pos);
            }

            if (networkAt == null)
                return InteractionResult.SUCCESS_SERVER;

            ConveyorStorage storageAt = networkAt.getStorage().getStorageAt(level, pos);
            if (storageAt == null)
                return InteractionResult.SUCCESS_SERVER;

            if (!storageAt.canAcceptIncomingItem())
                return InteractionResult.PASS;

            if (!storageAt.addItem(new ConveyorItem(pos, itemStack.copyWithCount(1))))
                return InteractionResult.PASS;

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
        FluidState fluid = level.getFluidState(pos);
        boolean waterlogged = fluid.getType() == Fluids.WATER;

        BlockState base = defaultBlockState()
                .setValue(WATERLOGGED, waterlogged)
                .setValue(FACING, context.getHorizontalDirection())
                .setValue(SHAPE, ConveyorShape.STRAIGHT)
                .setValue(ENABLED, true);

        // Run the resolver once so corners/slopes can form immediately if possible.
        return resolveConveyorState(level, pos, base, Reason.PLACED, context);
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
    protected BlockState updateShape(
            BlockState state,
            LevelReader level,
            ScheduledTickAccess ticks,
            BlockPos pos,
            Direction directionToNeighbour,
            BlockPos neighbourPos,
            BlockState neighbourState,
            RandomSource random
    ) {
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

        BlockState resolved = resolveConveyorState(level, pos, current, Reason.NEIGHBOR_CHANGED, null);

        if (resolved == current)
            return;

        level.setBlock(pos, resolved, Block.UPDATE_ALL);

        // Schedule/propagate neighbour resolves
        onConveyorStateChanges(level, pos, current, resolved);

        ConveyorNetworkManager manager = getNetworkManager(level);
        if (manager != null) {
            boolean needsNetworkUpdate =
                    current.getValue(SHAPE) != resolved.getValue(SHAPE) ||
                            current.getValue(FACING) != resolved.getValue(FACING);

            if (needsNetworkUpdate) {
                manager.recreateNetworkAt(level, pos);
                ConveyorNetwork net = manager.getNetworkAt(pos);
                if (net != null) {
                    manager.syncNetwork(level, net);
                    LevelConveyorNetworks.getOrCreate(level).setDirty();
                }
            }
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
        Direction mirroredFacing = mirror.mirror(state.getValue(FACING));
        ConveyorShape mirroredShape = switch (state.getValue(SHAPE)) {
            case TURN_LEFT -> ConveyorShape.TURN_RIGHT;
            case TURN_RIGHT -> ConveyorShape.TURN_LEFT;
            default -> state.getValue(SHAPE);
        };
        return state.setValue(FACING, mirroredFacing).setValue(SHAPE, mirroredShape);
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
    public int getItemLimit(Level level, BlockPos pos, BlockState state) {
        ConveyorShape shape = state.getValue(SHAPE);
        return switch (shape) {
            case STRAIGHT, TURN_LEFT, TURN_RIGHT -> 5;
            case UP, DOWN -> 3;
        };
    }

    @Override
    public int getSpeed(Level level, BlockPos pos, BlockState state) {
        if (!isEnabled(level, pos, state))
            return 0;

        ConveyorShape shape = state.getValue(SHAPE);
        return switch (shape) {
            case STRAIGHT, TURN_LEFT, TURN_RIGHT -> 5;
            case UP, DOWN -> 2;
        };
    }

    @Override
    public boolean isEnabled(Level level, BlockPos pos, BlockState state) {
        return state.getValue(ENABLED);
    }

    public BlockState resolveConveyorState(Level level, BlockPos pos, BlockState oldState, Reason reason, @Nullable BlockPlaceContext context) {
        boolean waterlogged = level.getFluidState(pos).getType() == Fluids.WATER;
        boolean enabled = computeEnabled(level, pos, oldState);

        Direction preferredFacing = reason == Reason.PLACED && context != null
                ? context.getHorizontalDirection()
                : oldState.getValue(FACING);
        ConveyorShape currentShape = oldState.getValue(SHAPE);
        ResolvedOrientation resolvedOrientation = reason == Reason.PLACED
                ? new ResolvedOrientation(preferredFacing, computeBestShape(level, pos, preferredFacing, currentShape))
                : computeBestOrientation(level, pos, preferredFacing, currentShape);

        return oldState.setValue(FACING, resolvedOrientation.facing())
                .setValue(WATERLOGGED, waterlogged)
                .setValue(ENABLED, enabled)
                .setValue(SHAPE, resolvedOrientation.shape());
    }

    @Override
    public ConveyorTopology getTopology(Level level, BlockPos pos, BlockState state) {
        return createTopology(pos, state.getValue(FACING), state.getValue(SHAPE));
    }

    protected ConveyorTopology createTopology(BlockPos pos, Direction facing, ConveyorShape shape) {
        Direction back = facing.getOpposite();
        Direction left = facing.getCounterClockWise();
        Direction right = facing.getClockWise();
        BlockPos forward = pos.relative(facing);

        return switch (shape) {
            case STRAIGHT -> new ConveyorTopology(
                    List.of(new ConveyorInput("in", pos.relative(back))),
                    List.of(new ConveyorOutput("out", forward, pos))
            );
            case UP -> new ConveyorTopology(
                    List.of(new ConveyorInput("in", pos.relative(back))),
                    List.of(new ConveyorOutput("out", forward.above(), pos.above()))
            );
            case DOWN -> new ConveyorTopology(
                    List.of(new ConveyorInput("in", pos.relative(back).above())),
                    List.of(new ConveyorOutput("out", forward, pos))
            );
            case TURN_LEFT -> new ConveyorTopology(
                    List.of(new ConveyorInput("in", pos.relative(right))),
                    List.of(new ConveyorOutput("out", forward, pos))
            );
            case TURN_RIGHT -> new ConveyorTopology(
                    List.of(new ConveyorInput("in", pos.relative(left))),
                    List.of(new ConveyorOutput("out", forward, pos))
            );
        };
    }

    public ConveyorShape computeBestShape(Level level, BlockPos pos, Direction facing, ConveyorShape currentShape) {
        ConveyorShape[] candidates = ConveyorShape.values();

        ConveyorShape bestShape = ConveyorShape.STRAIGHT;
        int bestScore = Integer.MIN_VALUE;
        for (ConveyorShape candidate : candidates) {
            ConveyorTopology topology = createTopology(pos, facing, candidate);

            if (!isPhysicallyValid(level, pos, facing, candidate, topology))
                continue;

            int score = scoreShape(level, pos, facing, candidate, topology);
            if (score > bestScore) {
                bestScore = score;
                bestShape = candidate;
            } else if (score == bestScore) {
                if (candidate == currentShape && bestShape != currentShape) {
                    bestShape = candidate;
                }
            }
        }

        return bestShape;
    }

    public ResolvedOrientation computeBestOrientation(Level level, BlockPos pos, Direction currentFacing, ConveyorShape currentShape) {
        Direction bestFacing = currentFacing;
        ConveyorShape bestShape = currentShape;
        int bestScore = Integer.MIN_VALUE;

        for (Direction facing : Direction.Plane.HORIZONTAL) {
            for (ConveyorShape candidate : ConveyorShape.values()) {
                ConveyorTopology topology = createTopology(pos, facing, candidate);

                if (!isPhysicallyValid(level, pos, facing, candidate, topology))
                    continue;

                int score = scoreShape(level, pos, facing, candidate, topology);
                if (score > bestScore || score == bestScore && isPreferredOrientation(facing, candidate, bestFacing, bestShape, currentFacing, currentShape)) {
                    bestScore = score;
                    bestFacing = facing;
                    bestShape = candidate;
                }
            }
        }

        return new ResolvedOrientation(bestFacing, bestShape);
    }

    private static boolean isPreferredOrientation(Direction candidateFacing, ConveyorShape candidateShape,
                                                  Direction currentBestFacing, ConveyorShape currentBestShape,
                                                  Direction preferredFacing, ConveyorShape preferredShape) {
        return getOrientationPriority(candidateFacing, candidateShape, preferredFacing, preferredShape)
                > getOrientationPriority(currentBestFacing, currentBestShape, preferredFacing, preferredShape);
    }

    private static int getOrientationPriority(Direction candidateFacing, ConveyorShape candidateShape,
                                              Direction preferredFacing, ConveyorShape preferredShape) {
        int priority = 0;
        if (candidateFacing == preferredFacing) {
            priority += 2;
        }

        if (candidateShape == preferredShape) {
            priority += 1;
        }

        return priority;
    }

    public boolean isPhysicallyValid(Level level, BlockPos pos, Direction facing, ConveyorShape shape, ConveyorTopology topology) {
        if (!level.isInValidBounds(pos))
            return false;

        for (ConveyorInput input : topology.inputs()) {
            if (!level.isInValidBounds(input.expectedSourcePos()))
                return false;
        }

        for (ConveyorOutput output : topology.outputs()) {
            if (!level.isInValidBounds(output.deliveryPos()))
                return false;

            if (level.getBlockState(output.deliveryPos()).isSolidRender())
                return false;
        }

        // Check for solid blocks in the conveyor's space.
        for (BlockPos checkPos : BlockPos.betweenClosed(pos, pos.above())) {
            if (level.getBlockState(checkPos).isSolidRender())
                return false;
        }

        return true;
    }

    public int scoreShape(Level level, BlockPos pos, Direction facing, ConveyorShape shape, ConveyorTopology topology) {
        ConveyorInput input = topology.inputs().getFirst();
        ConveyorOutput output = topology.outputs().getFirst();
        int score = 0;

        // Downstream scoring: Prefer connecting to other conveyors, then empty space.
        if (connectsToConveyorInput(level, pos, output.deliveryPos())) {
            score += 100;
        } else if (level.isEmptyBlock(output.deliveryPos())) {
            score += 5;
        }

        // Upstream scoring: Prefer receiving from other conveyors.
        if (connectsFromConveyorOutput(level, input.expectedSourcePos(), pos)) {
            score += 80;
        }

        // Small preference for straight shapes for aesthetic reasons.
        if (shape == ConveyorShape.STRAIGHT) {
            score += 2;
        }

        return score;
    }

    public boolean connectsToConveyorInput(Level level, BlockPos fromPos, BlockPos toPos) {
        if (!ConveyorNetwork.isConveyor(level, toPos))
            return false;

        BlockState targetState = level.getBlockState(toPos);
        if (!(targetState.getBlock() instanceof ConveyorLike targetConveyor))
            return false;

        return targetConveyor.getTopology(level, toPos, targetState).acceptsInputFrom(fromPos);
    }

    public boolean connectsFromConveyorOutput(Level level, BlockPos fromPos, BlockPos toPos) {
        if (!ConveyorNetwork.isConveyor(level, fromPos))
            return false;

        BlockState fromState = level.getBlockState(fromPos);
        if (!(fromState.getBlock() instanceof ConveyorLike fromConveyor))
            return false;

        return fromConveyor.getTopology(level, fromPos, fromState).outputs().stream()
                .anyMatch(output -> output.deliveryPos().equals(toPos));
    }

    public boolean connectsToInventory(Level level, BlockPos fromPos, BlockPos toPos) {
        int dx = fromPos.getX() - toPos.getX();
        int dz = fromPos.getZ() - toPos.getZ();
        Direction sideTowardConveyor = Direction.getApproximateNearest(dx, 0, dz);

        Storage<ItemVariant> storage = TransferType.ITEM.getBlockLookup().find(level, toPos, sideTowardConveyor);
        return storage != null && storage.supportsInsertion();
    }

    public boolean inventoryCanInsert(Level level, BlockPos fromPos, BlockPos toPos) {
        int dx = fromPos.getX() - toPos.getX();
        int dz = fromPos.getZ() - toPos.getZ();
        Direction sideTowardConveyor = Direction.getApproximateNearest(dx, 0, dz);

        Storage<ItemVariant> storage = TransferType.ITEM.getBlockLookup().find(level, toPos, sideTowardConveyor);
        return storage != null && storage.supportsExtraction();
    }

    public boolean computeEnabled(Level level, BlockPos pos, BlockState state) {
        return !level.hasNeighborSignal(pos);
    }

    public void onConveyorStateChanges(Level level, BlockPos pos, BlockState oldState, BlockState newState) {
        if (!(level instanceof ServerLevel serverLevel))
            return;

        Set<BlockPos> positionsToRecheck = new HashSet<>();

        addTopologyPositions(positionsToRecheck, getTopology(level, pos, oldState));
        addTopologyPositions(positionsToRecheck, getTopology(level, pos, newState));

        for (Direction direction : Direction.values()) {
            positionsToRecheck.add(pos.relative(direction));
        }

        for (BlockPos checkPos : positionsToRecheck) {
            if (!level.isInValidBounds(checkPos))
                continue;

            BlockState checkState = level.getBlockState(checkPos);
            if (checkState.getBlock() instanceof ConveyorLike) {
                serverLevel.scheduleTick(checkPos, checkState.getBlock(), 1);
            }
        }
    }

    private static void addTopologyPositions(Set<BlockPos> positions, ConveyorTopology topology) {
        topology.inputs().stream()
                .map(ConveyorInput::expectedSourcePos)
                .forEach(positions::add);
        topology.outputs().stream()
                .map(ConveyorOutput::deliveryPos)
                .forEach(positions::add);
    }

    public enum ConveyorShape implements StringRepresentable {
        STRAIGHT,
        UP,
        DOWN,
        TURN_LEFT,
        TURN_RIGHT;

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public enum Reason {
        PLACED,
        NEIGHBOR_CHANGED
    }

    public record ResolvedOrientation(Direction facing, ConveyorShape shape) {
    }
}
