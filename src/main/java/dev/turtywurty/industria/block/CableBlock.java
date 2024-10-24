package dev.turtywurty.industria.block;

import dev.turtywurty.industria.blockentity.CableBlockEntity;
import dev.turtywurty.industria.blockentity.util.TickableBlockEntity;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
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
import net.minecraft.world.tick.OrderedTick;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

import java.util.Locale;

public class CableBlock extends Block implements Waterloggable, BlockEntityProvider {
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    public static final EnumProperty<ConnectorType> NORTH = EnumProperty.of("north", ConnectorType.class);
    public static final EnumProperty<ConnectorType> SOUTH = EnumProperty.of("south", ConnectorType.class);
    public static final EnumProperty<ConnectorType> WEST = EnumProperty.of("west", ConnectorType.class);
    public static final EnumProperty<ConnectorType> EAST = EnumProperty.of("east", ConnectorType.class);
    public static final EnumProperty<ConnectorType> UP = EnumProperty.of("up", ConnectorType.class);
    public static final EnumProperty<ConnectorType> DOWN = EnumProperty.of("down", ConnectorType.class);

    private static final VoxelShape SHAPE_CABLE_NORTH = VoxelShapes.cuboid(0.375, 0.375, 0, 0.625, 0.625, 0.375);
    private static final VoxelShape SHAPE_CABLE_SOUTH = VoxelShapes.cuboid(0.375, 0.375, 0.625, 0.625, 0.625, 1);
    private static final VoxelShape SHAPE_CABLE_WEST = VoxelShapes.cuboid(0, 0.375, 0.375, 0.375, 0.625, 0.625);
    private static final VoxelShape SHAPE_CABLE_EAST = VoxelShapes.cuboid(0.625, 0.375, 0.375, 1, 0.625, 0.625);
    private static final VoxelShape SHAPE_CABLE_UP = VoxelShapes.cuboid(0.375, 0.625, 0.375, 0.625, 1, 0.625);
    private static final VoxelShape SHAPE_CABLE_DOWN = VoxelShapes.cuboid(0.375, 0, 0.375, 0.625, 0.375, 0.625);
    private static final VoxelShape SHAPE_BLOCK_NORTH = VoxelShapes.cuboid(0.25, 0.25, 0, 0.75, 0.75, 0.125);
    private static final VoxelShape SHAPE_BLOCK_SOUTH = VoxelShapes.cuboid(0.25, 0.25, 0.875, 0.75, 0.75, 1);
    private static final VoxelShape SHAPE_BLOCK_WEST = VoxelShapes.cuboid(0, 0.25, 0.25, 0.125, 0.75, 0.75);
    private static final VoxelShape SHAPE_BLOCK_EAST = VoxelShapes.cuboid(0.875, 0.25, 0.25, 1, 0.75, 0.75);
    private static final VoxelShape SHAPE_BLOCK_UP = VoxelShapes.cuboid(0.25, 0.875, 0.25, 0.75, 1, 0.75);
    private static final VoxelShape SHAPE_BLOCK_DOWN = VoxelShapes.cuboid(0.25, 0, 0.25, 0.75, 0.125, 0.75);
    private static VoxelShape[] shapeCache = null;

    public CableBlock(Settings settings) {
        super(settings);
        createShapes();

        setDefaultState(getDefaultState().with(WATERLOGGED, false));
    }

    private static int calculateShapeIndex(ConnectorType north, ConnectorType south, ConnectorType west, ConnectorType east, ConnectorType up, ConnectorType down) {
        int size = ConnectorType.values().length;
        return ((((south.ordinal() * size + north.ordinal()) * size + west.ordinal()) * size + east.ordinal()) * size + up.ordinal()) * size + down.ordinal();
    }

    private static void createShapes() {
        if (shapeCache == null) {
            int length = ConnectorType.values().length;
            shapeCache = new VoxelShape[length * length * length * length * length * length];

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
    }

    private static VoxelShape createShape(ConnectorType north, ConnectorType south, ConnectorType west, ConnectorType east, ConnectorType up, ConnectorType down) {
        VoxelShape shape = VoxelShapes.cuboid(.4, .4, .4, .6, .6, .6);
        shape = combineShape(shape, north, SHAPE_CABLE_NORTH, SHAPE_BLOCK_NORTH);
        shape = combineShape(shape, south, SHAPE_CABLE_SOUTH, SHAPE_BLOCK_SOUTH);
        shape = combineShape(shape, west, SHAPE_CABLE_WEST, SHAPE_BLOCK_WEST);
        shape = combineShape(shape, east, SHAPE_CABLE_EAST, SHAPE_BLOCK_EAST);
        shape = combineShape(shape, up, SHAPE_CABLE_UP, SHAPE_BLOCK_UP);
        shape = combineShape(shape, down, SHAPE_CABLE_DOWN, SHAPE_BLOCK_DOWN);
        return shape;
    }

    private static VoxelShape combineShape(VoxelShape shape, ConnectorType connectorType, VoxelShape cableShape, VoxelShape blockShape) {
        if (connectorType == ConnectorType.CABLE) {
            return VoxelShapes.combine(shape, cableShape, BooleanBiFunction.OR);
        } else if (connectorType == ConnectorType.BLOCK) {
            return VoxelShapes.combine(shape, VoxelShapes.combine(blockShape, cableShape, BooleanBiFunction.OR), BooleanBiFunction.OR);
        } else {
            return shape;
        }
    }

    private static ConnectorType getConnectorType(World world, BlockPos connectorPos, Direction facing) {
        BlockPos pos = connectorPos.offset(facing);
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (block instanceof CableBlock) {
            return ConnectorType.CABLE;
        } else if (isConnectable(world, connectorPos, facing)) {
            return ConnectorType.BLOCK;
        } else {
            return ConnectorType.NONE;
        }
    }

    public static boolean isConnectable(World world, BlockPos connectorPos, Direction facing) {
        BlockPos pos = connectorPos.offset(facing);
        BlockState state = world.getBlockState(pos);
        if (state.isAir())
            return false;

        return EnergyStorage.SIDED.find(world, pos, facing.getOpposite()) != null;
    }

    public static @NotNull BlockState calculateState(World world, BlockPos pos, BlockState state) {
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
            tickView.getFluidTickScheduler().scheduleTick(new OrderedTick<>(Fluids.WATER, pos, Fluids.WATER.getTickRate(world), 0L));   // TODO: Figure out what the sub tick order is
        }

        return calculateState((World) world, pos, state);
    }

    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, @Nullable WireOrientation wireOrientation, boolean notify) {
        super.neighborUpdate(state, world, pos, sourceBlock, wireOrientation, notify);
        if (!world.isClient && world.getBlockEntity(pos) instanceof CableBlockEntity cable) {
            cable.markDirty();
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

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntityTypeInit.CABLE.instantiate(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return TickableBlockEntity.createTicker(world);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if(player.getActiveItem().getItem() instanceof BlockItem)
            return ActionResult.PASS;

        if (!world.isClient) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof CableBlockEntity cable) {
                player.sendMessage(Text.literal(cable.getEnergy().amount + " / " + cable.getEnergy().getCapacity() + " FE"), true);
            }
        }

        return ActionResult.SUCCESS;
    }

    public enum ConnectorType implements StringIdentifiable {
        NONE,
        CABLE,
        BLOCK;

        public static final ConnectorType[] VALUES = values();

        @Override
        public @NotNull String asString() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
