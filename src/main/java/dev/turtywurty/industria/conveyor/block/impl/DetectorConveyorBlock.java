package dev.turtywurty.industria.conveyor.block.impl;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.conveyor.ConveyorItem;
import dev.turtywurty.industria.conveyor.ConveyorNetwork;
import dev.turtywurty.industria.conveyor.ConveyorNetworkManager;
import dev.turtywurty.industria.conveyor.ConveyorStorage;
import dev.turtywurty.industria.conveyor.block.BaseConveyorBlock;
import dev.turtywurty.industria.conveyor.block.ConveyorInput;
import dev.turtywurty.industria.conveyor.block.ConveyorOutput;
import dev.turtywurty.industria.conveyor.block.ConveyorTopology;
import dev.turtywurty.industria.conveyor.block.impl.entity.DetectorConveyorBlockEntity;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
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
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

public class DetectorConveyorBlock extends BaseConveyorBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public DetectorConveyorBlock(BlockBehaviour.Properties settings) {
        super(settings, new IndustriaBlock.BlockProperties()
                .addStateProperty(FACING, Direction.NORTH)
                .addStateProperty(WATERLOGGED, false)
                .notPlaceFacingOpposite()
                .hasComparatorOutput()
                .comparatorOutput((state, level, pos, direction) -> getSignalStrength(state, level, pos))
                .blockEntityProperties(new IndustriaBlock.BlockProperties.BlockBlockEntityProperties<>(() -> BlockEntityTypeInit.DETECTOR_CONVEYOR)
                        .shouldTick()
                        .rightClickToOpenGui())
        );
    }

    @Override
    protected InteractionResult useItemOn(ItemStack itemStack, BlockState state, Level level, BlockPos pos, Player player,
                                          InteractionHand hand, BlockHitResult hitResult) {
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

        return defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection())
                .setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);

        if (level.isClientSide() || oldState.is(this))
            return;

        if (level instanceof ServerLevel serverLevel) {
            getNetworkManager(serverLevel).placeConveyor(serverLevel, pos);
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
        if (current.getValue(WATERLOGGED) != waterlogged) {
            level.setBlock(pos, current.setValue(WATERLOGGED, waterlogged), Block.UPDATE_ALL);
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
