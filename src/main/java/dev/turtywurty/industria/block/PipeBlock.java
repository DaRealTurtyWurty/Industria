package dev.turtywurty.industria.block;

import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.persistent.WorldPipeNetworks;
import dev.turtywurty.industria.pipe.PipeNetwork;
import dev.turtywurty.industria.pipe.PipeNetworkManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.Locale;

public abstract class PipeBlock<S, N extends PipeNetwork<S>, A extends Number> extends Block implements SimpleWaterloggedBlock {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public static final EnumProperty<ConnectorType> NORTH = EnumProperty.create("north", ConnectorType.class);
    public static final EnumProperty<ConnectorType> SOUTH = EnumProperty.create("south", ConnectorType.class);
    public static final EnumProperty<ConnectorType> WEST = EnumProperty.create("west", ConnectorType.class);
    public static final EnumProperty<ConnectorType> EAST = EnumProperty.create("east", ConnectorType.class);
    public static final EnumProperty<ConnectorType> UP = EnumProperty.create("up", ConnectorType.class);
    public static final EnumProperty<ConnectorType> DOWN = EnumProperty.create("down", ConnectorType.class);

    protected final VoxelShape[] pipeShapes = new VoxelShape[Direction.values().length];
    protected final VoxelShape[] blockConnectorShapes = new VoxelShape[Direction.values().length];

    protected final VoxelShape[] shapeCache = new VoxelShape[ConnectorType.VALUES.length * ConnectorType.VALUES.length * ConnectorType.VALUES.length * ConnectorType.VALUES.length * ConnectorType.VALUES.length * ConnectorType.VALUES.length];

    private final TransferType<S, ?, A> transferType;

    public PipeBlock(Properties settings, int diameter, TransferType<S, ?, A> transferType) {
        super(settings);
        registerDefaultState(defaultBlockState()
                .setValue(NORTH, ConnectorType.NONE)
                .setValue(SOUTH, ConnectorType.NONE)
                .setValue(WEST, ConnectorType.NONE)
                .setValue(EAST, ConnectorType.NONE)
                .setValue(UP, ConnectorType.NONE)
                .setValue(DOWN, ConnectorType.NONE)
                .setValue(WATERLOGGED, false));

        this.transferType = transferType;

        for (Direction direction : Direction.values()) {
            pipeShapes[direction.ordinal()] = createCableShape(direction, diameter);
            blockConnectorShapes[direction.ordinal()] = createBlockConnectorShape(direction);
        }

        createShapeCache();
    }

    public PipeNetworkManager<S, N> getNetworkManager(ServerLevel world) {
        return WorldPipeNetworks.getOrCreate(world).getNetworkManager(getTransferType());
    }

    public TransferType<S, ?, A> getTransferType() {
        return this.transferType;
    }

    private static VoxelShape createCableShape(Direction direction, int diameter) {
        double min = diameter / 16.0;
        double max = 1 - min;

        return switch (direction) {
            case NORTH -> Shapes.box(min, min, 0, max, max, min);
            case SOUTH -> Shapes.box(min, min, max, max, max, 1);
            case WEST -> Shapes.box(0, min, min, min, max, max);
            case EAST -> Shapes.box(max, min, min, 1, max, max);
            case UP -> Shapes.box(min, max, min, max, 1, max);
            case DOWN -> Shapes.box(min, 0, min, max, min, max);
        };
    }

    private static VoxelShape createBlockConnectorShape(Direction direction) {
        double min = 0.25;
        double max = 0.75;

        return switch (direction) {
            case NORTH -> Shapes.box(min, min, 0, max, max, 0.125);
            case SOUTH -> Shapes.box(min, min, 0.875, max, max, 1);
            case WEST -> Shapes.box(0, min, min, 0.125, max, max);
            case EAST -> Shapes.box(0.875, min, min, 1, max, max);
            case UP -> Shapes.box(min, max, min, max, 1, max);
            case DOWN -> Shapes.box(min, 0, min, max, 0.125, max);
        };
    }

    protected static VoxelShape combineShape(VoxelShape shape, ConnectorType connectorType, VoxelShape cableShape, VoxelShape blockShape) {
        if (connectorType == ConnectorType.PIPE) {
            return Shapes.joinUnoptimized(shape, cableShape, BooleanOp.OR);
        } else if (connectorType == ConnectorType.BLOCK) {
            return Shapes.joinUnoptimized(shape, Shapes.joinUnoptimized(blockShape, cableShape, BooleanOp.OR), BooleanOp.OR);
        } else {
            return shape;
        }
    }

    protected static int calculateShapeIndex(ConnectorType north, ConnectorType south, ConnectorType west, ConnectorType east, ConnectorType up, ConnectorType down) {
        int size = ConnectorType.VALUES.length;
        return ((((south.ordinal() * size + north.ordinal()) * size + west.ordinal()) * size + east.ordinal()) * size + up.ordinal()) * size + down.ordinal();
    }

    protected void createShapeCache() {
        for (ConnectorType up : ConnectorType.VALUES) {
            for (ConnectorType down : ConnectorType.VALUES) {
                for (ConnectorType north : ConnectorType.VALUES) {
                    for (ConnectorType south : ConnectorType.VALUES) {
                        for (ConnectorType east : ConnectorType.VALUES) {
                            for (ConnectorType west : ConnectorType.VALUES) {
                                int idx = calculateShapeIndex(north, south, west, east, up, down);
                                shapeCache[idx] = createShape(north, south, west, east, up, down);
                            }
                        }
                    }
                }
            }
        }
    }

    private VoxelShape createShape(ConnectorType north, ConnectorType south, ConnectorType west, ConnectorType east, ConnectorType up, ConnectorType down) {
        VoxelShape shape = Shapes.box(.4, .4, .4, .6, .6, .6);
        shape = combineShape(shape, north, pipeShapes[Direction.NORTH.ordinal()], blockConnectorShapes[Direction.NORTH.ordinal()]);
        shape = combineShape(shape, south, pipeShapes[Direction.SOUTH.ordinal()], blockConnectorShapes[Direction.SOUTH.ordinal()]);
        shape = combineShape(shape, west, pipeShapes[Direction.WEST.ordinal()], blockConnectorShapes[Direction.WEST.ordinal()]);
        shape = combineShape(shape, east, pipeShapes[Direction.EAST.ordinal()], blockConnectorShapes[Direction.EAST.ordinal()]);
        shape = combineShape(shape, up, pipeShapes[Direction.UP.ordinal()], blockConnectorShapes[Direction.UP.ordinal()]);
        shape = combineShape(shape, down, pipeShapes[Direction.DOWN.ordinal()], blockConnectorShapes[Direction.DOWN.ordinal()]);
        return shape;
    }

    protected ConnectorType getConnectorType(Level world, BlockPos connectorPos, Direction facing) {
        BlockPos pos = connectorPos.relative(facing);
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (block == this) {
            return ConnectorType.PIPE;
        } else if (isConnectable(world, connectorPos, facing)) {
            return ConnectorType.BLOCK;
        } else {
            return ConnectorType.NONE;
        }
    }

    public boolean isConnectable(Level world, BlockPos connectorPos, Direction facing) {
        BlockPos pos = connectorPos.relative(facing);
        BlockState state = world.getBlockState(pos);
        if (state.isAir())
            return false;

        return this.transferType.lookup(world, pos, facing.getOpposite()) != null;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        ConnectorType north = state.getValue(NORTH);
        ConnectorType south = state.getValue(SOUTH);
        ConnectorType west = state.getValue(WEST);
        ConnectorType east = state.getValue(EAST);
        ConnectorType up = state.getValue(UP);
        ConnectorType down = state.getValue(DOWN);
        int index = calculateShapeIndex(north, south, west, east, up, down);
        return shapeCache[index];
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader world, ScheduledTickAccess tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        if (state.getValue(WATERLOGGED)) {
            tickView.getFluidTicks().schedule(tickView.createTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world)));
        }

        return calculateState((Level) world, pos, state);
    }

    @Override
    protected void neighborChanged(BlockState state, Level world, BlockPos pos, Block sourceBlock, @Nullable Orientation wireOrientation, boolean notify) {
        super.neighborChanged(state, world, pos, sourceBlock, wireOrientation, notify);
        BlockState blockState = calculateState(world, pos, state);
        if (blockState != state) {
            world.setBlockAndUpdate(pos, blockState);

            if (!world.isClientSide() && state.isAir() && world instanceof ServerLevel serverWorld) {
                getNetworkManager(serverWorld).placePipe(serverWorld, pos);
            }
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(WATERLOGGED, NORTH, SOUTH, WEST, EAST, UP, DOWN);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Level world = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        if(!world.isClientSide() && world instanceof ServerLevel serverWorld) {
            getNetworkManager(serverWorld).placePipe(serverWorld, pos);
        }

        BlockState state = defaultBlockState().setValue(WATERLOGGED, world.getFluidState(pos).getType() == Fluids.WATER);
        return calculateState(world, pos, state);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel world, BlockPos pos, boolean moved) {
        BlockState newState = world.getBlockState(pos);
        if(!state.is(newState.getBlock()) && !world.isClientSide()) {
            getNetworkManager(world).removePipe(world, pos);
        }

        super.affectNeighborsAfterRemoval(state, world, pos, moved);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    public abstract A getAmount(S storage);

    public abstract A getCapacity(S storage);

    public abstract String getUnit();

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (player.getMainHandItem().getItem() instanceof BlockItem)
            return InteractionResult.PASS;

        if(world instanceof ServerLevel serverWorld) {
            PipeNetworkManager<S, N> networkManager = getNetworkManager(serverWorld);
            PipeNetwork<S> network = networkManager.getNetwork(pos);
            if (network == null) {
                networkManager.traverseCreateNetwork(serverWorld, pos);
                network = networkManager.getNetwork(pos);
                if(network == null)
                    return InteractionResult.PASS;
            }

            A amount = getAmount(network.getStorage(pos));
            A capacity = getCapacity(network.getStorage(pos));

            var df = new DecimalFormat("#.##");
            var scientific = new DecimalFormat("#.##E0");

            String amountStr = df.format(amount).replace(".00", "");
            String capacityStr = df.format(capacity).replace(".00", "");

            // replace with scientific notation if the number is too large
            if (amountStr.length() > 6) {
                amountStr = scientific.format(amount);
            }

            if (capacityStr.length() > 6) {
                capacityStr = scientific.format(capacity);
            }

            if(!(this instanceof HeatPipeBlock)) {
                player.displayClientMessage(Component.literal("Pipe at " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ": ")
                        .append(Component.literal(amountStr + " " + getUnit() + " / " + capacityStr + " " + getUnit())
                                .withStyle(ChatFormatting.GREEN)), true);
            } else {
                player.displayClientMessage(Component.literal("Heat pipe at " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ": ")
                        .append(Component.literal(amount + " " + getUnit())
                                .withStyle(ChatFormatting.RED)), true);
            }
            return InteractionResult.SUCCESS_SERVER;
        }

        return InteractionResult.SUCCESS;
    }

    private @NotNull BlockState calculateState(Level world, BlockPos pos, BlockState state) {
        ConnectorType north = getConnectorType(world, pos, Direction.NORTH);
        ConnectorType south = getConnectorType(world, pos, Direction.SOUTH);
        ConnectorType west = getConnectorType(world, pos, Direction.WEST);
        ConnectorType east = getConnectorType(world, pos, Direction.EAST);
        ConnectorType up = getConnectorType(world, pos, Direction.UP);
        ConnectorType down = getConnectorType(world, pos, Direction.DOWN);

        return state
                .setValue(NORTH, north)
                .setValue(SOUTH, south)
                .setValue(WEST, west)
                .setValue(EAST, east)
                .setValue(UP, up)
                .setValue(DOWN, down);
    }

    public enum ConnectorType implements StringRepresentable {
        NONE,
        PIPE,
        BLOCK;

        public static final ConnectorType[] VALUES = values();

        @Override
        public @NotNull String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
