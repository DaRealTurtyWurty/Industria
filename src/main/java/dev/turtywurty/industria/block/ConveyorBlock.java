package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.conveyor.ConveyorItem;
import dev.turtywurty.industria.conveyor.ConveyorNetwork;
import dev.turtywurty.industria.conveyor.ConveyorNetworkManager;
import dev.turtywurty.industria.conveyor.ConveyorStorage;
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
import java.util.Locale;
import java.util.Set;

public class ConveyorBlock extends IndustriaBlock {
    public static final EnumProperty<ConveyorShape> SHAPE = EnumProperty.create("shape", ConveyorShape.class);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final Property<Boolean> ENABLED = BooleanProperty.create("enabled");

    public ConveyorBlock(Properties settings) {
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

    public int getItemLimit(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        ConveyorShape shape = state.getValue(SHAPE);
        return switch (shape) {
            case STRAIGHT, TURN_LEFT, TURN_RIGHT -> 5;
            case UP, DOWN -> 3;
        };
    }

    public int getSpeed(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!state.getValue(ENABLED))
            return 0;

        ConveyorShape shape = state.getValue(SHAPE);
        return switch (shape) {
            case STRAIGHT, TURN_LEFT, TURN_RIGHT -> 5;
            case UP, DOWN -> 2;
        };
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

    public Ports getConveyorPorts(BlockPos pos, Direction facing, ConveyorShape shape) {
        Direction back = facing.getOpposite();
        Direction left = facing.getCounterClockWise();
        Direction right = facing.getClockWise();
        BlockPos forward = pos.relative(facing);

        BlockPos inputPos, outputPos;
        switch (shape) {
            case STRAIGHT -> {
                inputPos = pos.relative(back);
                outputPos = forward;
            }
            case UP -> {
                inputPos = pos.relative(back);
                outputPos = forward.above();
            }
            case DOWN -> {
                inputPos = pos.relative(back).above();
                outputPos = forward;
            }
            case TURN_LEFT -> {
                inputPos = pos.relative(right);
                outputPos = forward;
            }
            case TURN_RIGHT -> {
                inputPos = pos.relative(left);
                outputPos = forward;
            }
            default -> throw new IllegalStateException("Unexpected conveyor shape: " + shape);
        }

        return new Ports(inputPos, outputPos);
    }

    public ConveyorShape computeBestShape(Level level, BlockPos pos, Direction facing, ConveyorShape currentShape) {
        ConveyorShape[] candidates = ConveyorShape.values();

        ConveyorShape bestShape = ConveyorShape.STRAIGHT;
        int bestScore = Integer.MIN_VALUE;
        for (ConveyorShape candidate : candidates) {
            Ports ports = getConveyorPorts(pos, facing, candidate);

            if (!isPhysicallyValid(level, pos, facing, candidate, ports))
                continue;

            int score = scoreShape(level, pos, facing, candidate, ports);
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
                Ports ports = getConveyorPorts(pos, facing, candidate);

                if (!isPhysicallyValid(level, pos, facing, candidate, ports))
                    continue;

                int score = scoreShape(level, pos, facing, candidate, ports);
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

    public boolean isPhysicallyValid(Level level, BlockPos pos, Direction facing, ConveyorShape shape, Ports ports) {
        // Check if input and output positions are within world bounds.
        if (!level.isInValidBounds(pos) || !level.isInValidBounds(ports.inputPos()) || !level.isInValidBounds(ports.outputPos()))
            return false;

        if (level.getBlockState(ports.outputPos()).isSolidRender())
            return false;

        // Check for solid blocks in the conveyor's space.
        for (BlockPos checkPos : BlockPos.betweenClosed(pos, pos.above())) {
            if (level.getBlockState(checkPos).isSolidRender())
                return false;
        }

        return true;
    }

    public int scoreShape(Level level, BlockPos pos, Direction facing, ConveyorShape shape, Ports ports) {
        int score = 0;

        // Downstream scoring: Prefer connecting to other conveyors, then inventories, then empty space.
        if (connectsToConveyorInput(level, pos, ports.outputPos())) {
            score += 100;
        } else if (connectsToInventory(level, pos, ports.outputPos())) {
            score += 60;
        } else if (level.isEmptyBlock(ports.outputPos())) {
            score += 5;
        }

        // Upstream scoring: Prefer receiving from conveyors, then inserting into inventories.
        if (connectsFromConveyorOutput(level, ports.inputPos(), pos)) {
            score += 80;
        } else if (inventoryCanInsert(level, pos, ports.inputPos())) {
            score += 40;
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
        Ports targetPorts = getConveyorPorts(toPos, targetState.getValue(FACING), targetState.getValue(SHAPE));

        // accepts input if its port fromPos is the same as our fromPos
        return targetPorts.inputPos().equals(fromPos);
    }

    public boolean connectsFromConveyorOutput(Level level, BlockPos fromPos, BlockPos toPos) {
        if (!ConveyorNetwork.isConveyor(level, fromPos))
            return false;

        BlockState fromState = level.getBlockState(fromPos);
        Ports fromPorts = getConveyorPorts(fromPos, fromState.getValue(FACING), fromState.getValue(SHAPE));

        // provides output if its port outputPos is the same as our toPos
        return fromPorts.outputPos().equals(toPos);
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

        Ports oldPorts = getConveyorPorts(pos, oldState.getValue(FACING), oldState.getValue(SHAPE));
        Ports newPorts = getConveyorPorts(pos, newState.getValue(FACING), newState.getValue(SHAPE));

        positionsToRecheck.add(oldPorts.inputPos());
        positionsToRecheck.add(oldPorts.outputPos());
        positionsToRecheck.add(newPorts.inputPos());
        positionsToRecheck.add(newPorts.outputPos());

        for (Direction direction : Direction.values()) {
            positionsToRecheck.add(pos.relative(direction));
        }

        for (BlockPos checkPos : positionsToRecheck) {
            if (!level.isInValidBounds(checkPos))
                continue;

            BlockState checkState = level.getBlockState(checkPos);
            if (checkState.getBlock() instanceof ConveyorBlock) {
                serverLevel.scheduleTick(checkPos, checkState.getBlock(), 1);
            }
        }
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

    public record Ports(BlockPos inputPos, BlockPos outputPos) {
    }
}
