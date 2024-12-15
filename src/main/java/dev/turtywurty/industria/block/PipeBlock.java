package dev.turtywurty.industria.block;

import dev.turtywurty.industria.blockentity.PipeBlockEntity;
import dev.turtywurty.industria.blockentity.util.TickableBlockEntity;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.block.WireOrientation;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public abstract class PipeBlock<B extends PipeBlockEntity<?, ?>> extends Block implements Waterloggable, BlockEntityProvider {
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

    public static final EnumProperty<ConnectorType> NORTH = EnumProperty.of("north", ConnectorType.class);
    public static final EnumProperty<ConnectorType> SOUTH = EnumProperty.of("south", ConnectorType.class);
    public static final EnumProperty<ConnectorType> WEST = EnumProperty.of("west", ConnectorType.class);
    public static final EnumProperty<ConnectorType> EAST = EnumProperty.of("east", ConnectorType.class);
    public static final EnumProperty<ConnectorType> UP = EnumProperty.of("up", ConnectorType.class);
    public static final EnumProperty<ConnectorType> DOWN = EnumProperty.of("down", ConnectorType.class);

    protected final VoxelShape[] pipeShapes = new VoxelShape[Direction.values().length];
    protected final VoxelShape[] blockConnectorShapes = new VoxelShape[Direction.values().length];

    protected final VoxelShape[] shapeCache = new VoxelShape[ConnectorType.VALUES.length * ConnectorType.VALUES.length * ConnectorType.VALUES.length * ConnectorType.VALUES.length * ConnectorType.VALUES.length * ConnectorType.VALUES.length];

    private final Class<B> blockEntityClass;

    public PipeBlock(Settings settings, Class<B> blockEntityClass, int diameter) {
        super(settings);
        setDefaultState(getDefaultState()
                .with(NORTH, ConnectorType.NONE)
                .with(SOUTH, ConnectorType.NONE)
                .with(WEST, ConnectorType.NONE)
                .with(EAST, ConnectorType.NONE)
                .with(UP, ConnectorType.NONE)
                .with(DOWN, ConnectorType.NONE)
                .with(WATERLOGGED, false));

        this.blockEntityClass = blockEntityClass;

        for (Direction direction : Direction.values()) {
            pipeShapes[direction.ordinal()] = createCableShape(direction, diameter);
            blockConnectorShapes[direction.ordinal()] = createBlockConnectorShape(direction);
        }

        createShapeCache();
    }

    private static VoxelShape createCableShape(Direction direction, int diameter) {
        double min = diameter / 16.0;
        double max = 1 - min;

        return switch (direction) {
            case NORTH -> VoxelShapes.cuboid(min, min, 0, max, max, min);
            case SOUTH -> VoxelShapes.cuboid(min, min, max, max, max, 1);
            case WEST -> VoxelShapes.cuboid(0, min, min, min, max, max);
            case EAST -> VoxelShapes.cuboid(max, min, min, 1, max, max);
            case UP -> VoxelShapes.cuboid(min, max, min, max, 1, max);
            case DOWN -> VoxelShapes.cuboid(min, 0, min, max, min, max);
        };
    }

    private static VoxelShape createBlockConnectorShape(Direction direction) {
        double min = 0.25;
        double max = 0.75;

        return switch (direction) {
            case NORTH -> VoxelShapes.cuboid(min, min, 0, max, max, 0.125);
            case SOUTH -> VoxelShapes.cuboid(min, min, 0.875, max, max, 1);
            case WEST -> VoxelShapes.cuboid(0, min, min, 0.125, max, max);
            case EAST -> VoxelShapes.cuboid(0.875, min, min, 1, max, max);
            case UP -> VoxelShapes.cuboid(min, max, min, max, 1, max);
            case DOWN -> VoxelShapes.cuboid(min, 0, min, max, 0.125, max);
        };
    }

    protected abstract BlockApiLookup<?, Direction> getStorageLookup();

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
        VoxelShape shape = VoxelShapes.cuboid(.4, .4, .4, .6, .6, .6);
        shape = combineShape(shape, north, pipeShapes[Direction.NORTH.ordinal()], blockConnectorShapes[Direction.NORTH.ordinal()]);
        shape = combineShape(shape, south, pipeShapes[Direction.SOUTH.ordinal()], blockConnectorShapes[Direction.SOUTH.ordinal()]);
        shape = combineShape(shape, west, pipeShapes[Direction.WEST.ordinal()], blockConnectorShapes[Direction.WEST.ordinal()]);
        shape = combineShape(shape, east, pipeShapes[Direction.EAST.ordinal()], blockConnectorShapes[Direction.EAST.ordinal()]);
        shape = combineShape(shape, up, pipeShapes[Direction.UP.ordinal()], blockConnectorShapes[Direction.UP.ordinal()]);
        shape = combineShape(shape, down, pipeShapes[Direction.DOWN.ordinal()], blockConnectorShapes[Direction.DOWN.ordinal()]);
        return shape;
    }

    protected ConnectorType getConnectorType(World world, BlockPos connectorPos, Direction facing) {
        BlockPos pos = connectorPos.offset(facing);
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

    public boolean isConnectable(World world, BlockPos connectorPos, Direction facing) {
        BlockPos pos = connectorPos.offset(facing);
        BlockState state = world.getBlockState(pos);
        if (state.isAir())
            return false;

        return getStorageLookup().find(world, pos, facing.getOpposite()) != null;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        ConnectorType north = state.get(NORTH);
        ConnectorType south = state.get(SOUTH);
        ConnectorType west = state.get(WEST);
        ConnectorType east = state.get(EAST);
        ConnectorType up = state.get(UP);
        ConnectorType down = state.get(DOWN);
        int index = calculateShapeIndex(north, south, west, east, up, down);
        return shapeCache[index];
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        if (state.get(WATERLOGGED)) {
            tickView.getFluidTickScheduler().scheduleTick(tickView.createOrderedTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world)));
        }

        return calculateState((World) world, pos, state);
    }

    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, @Nullable WireOrientation wireOrientation, boolean notify) {
        super.neighborUpdate(state, world, pos, sourceBlock, wireOrientation, notify);
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!world.isClient && blockEntityClass.isInstance(blockEntity)) {
            blockEntity.markDirty();
        }

        BlockState blockState = calculateState(world, pos, state);
        if (blockState != state) {
            world.setBlockState(pos, blockState);
        }
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(WATERLOGGED, NORTH, SOUTH, WEST, EAST, UP, DOWN);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        World world = ctx.getWorld();
        BlockPos pos = ctx.getBlockPos();
        BlockState state = getDefaultState().with(WATERLOGGED, world.getFluidState(pos).getFluid() == Fluids.WATER);
        return calculateState(world, pos, state);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return TickableBlockEntity.createTicker(world);
    }

    protected abstract BlockEntityType<B> getBlockEntityType();

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return getBlockEntityType().instantiate(pos, state);
    }

    protected abstract long getAmount(B blockEntity);

    protected abstract long getCapacity(B blockEntity);

    protected abstract String getUnit();

    @SuppressWarnings("unchecked")
    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (player.getMainHandStack().getItem() instanceof BlockItem)
            return ActionResult.PASS;

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!world.isClient && blockEntityClass.isInstance(blockEntity)) {
            player.sendMessage(Text.literal(getAmount((B) blockEntity) + " / " + getCapacity((B) blockEntity) + " " + getUnit()), true);
        }

        return ActionResult.SUCCESS;
    }

    private @NotNull BlockState calculateState(World world, BlockPos pos, BlockState state) {
        ConnectorType north = getConnectorType(world, pos, Direction.NORTH);
        ConnectorType south = getConnectorType(world, pos, Direction.SOUTH);
        ConnectorType west = getConnectorType(world, pos, Direction.WEST);
        ConnectorType east = getConnectorType(world, pos, Direction.EAST);
        ConnectorType up = getConnectorType(world, pos, Direction.UP);
        ConnectorType down = getConnectorType(world, pos, Direction.DOWN);

        return state
                .with(NORTH, north)
                .with(SOUTH, south)
                .with(WEST, west)
                .with(EAST, east)
                .with(UP, up)
                .with(DOWN, down);
    }

    protected static VoxelShape combineShape(VoxelShape shape, ConnectorType connectorType, VoxelShape cableShape, VoxelShape blockShape) {
        if (connectorType == ConnectorType.PIPE) {
            return VoxelShapes.combine(shape, cableShape, BooleanBiFunction.OR);
        } else if (connectorType == ConnectorType.BLOCK) {
            return VoxelShapes.combine(shape, VoxelShapes.combine(blockShape, cableShape, BooleanBiFunction.OR), BooleanBiFunction.OR);
        } else {
            return shape;
        }
    }

    protected static int calculateShapeIndex(ConnectorType north, ConnectorType south, ConnectorType west, ConnectorType east, ConnectorType up, ConnectorType down) {
        int size = ConnectorType.VALUES.length;
        return ((((south.ordinal() * size + north.ordinal()) * size + west.ordinal()) * size + east.ordinal()) * size + up.ordinal()) * size + down.ordinal();
    }

    public enum ConnectorType implements StringIdentifiable {
        NONE,
        PIPE,
        BLOCK;

        public static final ConnectorType[] VALUES = values();

        @Override
        public @NotNull String asString() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
