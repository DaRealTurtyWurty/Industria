package dev.turtywurty.industria.conveyor.block.impl;

import dev.turtywurty.industria.conveyor.ConveyorItem;
import dev.turtywurty.industria.conveyor.ConveyorNetwork;
import dev.turtywurty.industria.conveyor.ConveyorNetworkManager;
import dev.turtywurty.industria.conveyor.ConveyorStorage;
import dev.turtywurty.industria.conveyor.block.BaseConveyorBlock;
import dev.turtywurty.industria.conveyor.block.ConveyorConnectionType;
import dev.turtywurty.industria.conveyor.block.ConveyorInput;
import dev.turtywurty.industria.conveyor.block.ConveyorOutput;
import dev.turtywurty.industria.conveyor.block.ConveyorTopology;
import dev.turtywurty.industria.persistent.LevelConveyorNetworks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
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

public class SideInjectorConveyorBlock extends BaseConveyorBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final Property<Boolean> ENABLED = BooleanProperty.create("enabled");

    public SideInjectorConveyorBlock(Properties settings) {
        super(settings, new BlockProperties()
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
        boolean waterlogged = fluidState.getType() == Fluids.WATER;
        boolean enabled = !level.hasNeighborSignal(pos);
        Direction preferredFacing = context.getHorizontalDirection();

        for (Direction facing : getPlacementOrder(preferredFacing)) {
            BlockState candidateState = defaultBlockState()
                    .setValue(FACING, facing)
                    .setValue(WATERLOGGED, waterlogged)
                    .setValue(ENABLED, enabled);

            if (isValidInjectorPlacement(level, pos, candidateState))
                return candidateState;
        }

        return null;
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

        boolean waterlogged = level.getFluidState(pos).getType() == Fluids.WATER;
        boolean enabled = !level.hasNeighborSignal(pos);
        BlockState resolved = current
                .setValue(WATERLOGGED, waterlogged)
                .setValue(ENABLED, enabled);

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

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return isValidInjectorPlacement(level, pos, state);
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
        return isEnabled(level, pos, state) ? 5 : 0;
    }

    @Override
    public boolean isEnabled(Level level, BlockPos pos, BlockState state) {
        return state.getValue(ENABLED);
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

    @Override
    public ConveyorConnectionType getConnectionType(Level level, BlockPos pos, BlockState state, ConveyorOutput output) {
        return ConveyorConnectionType.SIDE_INJECT;
    }

    @Override
    public boolean canConnectToConveyor(Level level, BlockPos pos, BlockState state, ConveyorOutput output, BlockPos targetPos, BlockState targetState) {
        return targetPos.equals(output.deliveryPos()) && super.canConnectToConveyor(level, pos, state, output, targetPos, targetState);
    }

    private static Direction[] getPlacementOrder(Direction preferredFacing) {
        return new Direction[] {
                preferredFacing,
                preferredFacing.getClockWise(),
                preferredFacing.getCounterClockWise(),
                preferredFacing.getOpposite()
        };
    }

    private static boolean isValidInjectorPlacement(LevelReader level, BlockPos pos, BlockState state) {
        if (!(state.getBlock() instanceof SideInjectorConveyorBlock sideInjector))
            return false;

        if (!(level instanceof Level actualLevel))
            return false;

        Direction facing = state.getValue(FACING);
        BlockPos targetPos = pos.relative(facing);
        BlockState targetState = level.getBlockState(targetPos);
        ConveyorTopology topology = sideInjector.getTopology(actualLevel, pos, state);
        if (topology.outputs().isEmpty())
            return false;

        ConveyorOutput output = topology.outputs().getFirst();
        return sideInjector.canConnectToConveyor(actualLevel, pos, state, output, targetPos, targetState);
    }
}
