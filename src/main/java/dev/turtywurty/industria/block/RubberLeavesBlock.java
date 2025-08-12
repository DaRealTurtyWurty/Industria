package dev.turtywurty.industria.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.ParticleUtil;
import net.minecraft.particle.TintedParticleEffect;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

import java.util.OptionalInt;

public class RubberLeavesBlock extends Block implements Waterloggable {
    public static final int DECAY_DISTANCE = 13;
    public static final IntProperty DISTANCE = IntProperty.of("distance", 1, DECAY_DISTANCE);
    public static final BooleanProperty PERSISTENT = Properties.PERSISTENT;
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    public static final MapCodec<RubberLeavesBlock> CODEC = createCodec(RubberLeavesBlock::new);
    private static final int TICK_DELAY = 1;

    public RubberLeavesBlock(Settings settings) {
        super(settings.mapColor(MapColor.DARK_GREEN)
                .strength(0.2F)
                .ticksRandomly()
                .sounds(BlockSoundGroup.GRASS)
                .nonOpaque()
                .allowsSpawning(Blocks::canSpawnOnLeaves)
                .suffocates(Blocks::never)
                .blockVision(Blocks::never)
                .burnable()
                .pistonBehavior(PistonBehavior.DESTROY)
                .solidBlock(Blocks::never));

        setDefaultState(this.stateManager.getDefaultState()
                .with(DISTANCE, DECAY_DISTANCE)
                .with(PERSISTENT, Boolean.FALSE)
                .with(WATERLOGGED, Boolean.FALSE));
    }

    private static BlockState updateDistanceFromLogs(BlockState state, WorldAccess world, BlockPos pos) {
        int distance = DECAY_DISTANCE;
        var mutable = new BlockPos.Mutable();

        for (Direction direction : Direction.values()) {
            mutable.set(pos, direction);
            distance = Math.min(distance, getDistanceFromLog(world.getBlockState(mutable)) + 1);
            if (distance == 1) {
                break;
            }
        }

        return state.with(DISTANCE, distance);
    }

    private static int getDistanceFromLog(BlockState state) {
        return getOptionalDistanceFromLog(state).orElse(DECAY_DISTANCE);
    }

    public static OptionalInt getOptionalDistanceFromLog(BlockState state) {
        if (state.isIn(BlockTags.LOGS)) {
            return OptionalInt.of(0);
        } else if (state.contains(DISTANCE)) {
            return OptionalInt.of(state.get(DISTANCE));
        } else if (state.contains(LeavesBlock.DISTANCE)) {
            return OptionalInt.of(state.get(LeavesBlock.DISTANCE));
        } else {
            return OptionalInt.empty();
        }
    }

    @Override
    public MapCodec<? extends RubberLeavesBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getSidesShape(BlockState state, BlockView world, BlockPos pos) {
        return VoxelShapes.empty();
    }

    @Override
    protected boolean hasRandomTicks(BlockState state) {
        return state.get(DISTANCE) == DECAY_DISTANCE && !state.get(PERSISTENT);
    }

    @Override
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (this.shouldDecay(state)) {
            dropStacks(state, world, pos);
            world.removeBlock(pos, false);
        }
    }

    protected boolean shouldDecay(BlockState state) {
        return !state.get(PERSISTENT) && state.get(DISTANCE) == DECAY_DISTANCE;
    }

    @Override
    protected int getOpacity(BlockState state) {
        return 1;
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        world.setBlockState(pos, updateDistanceFromLogs(state, world, pos), Block.NOTIFY_ALL);
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView,
                                                   BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        if (state.get(WATERLOGGED)) {
            tickView.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }

        int distance = getDistanceFromLog(neighborState) + 1;
        if (distance != 1 || state.get(DISTANCE) != distance) {
            tickView.scheduleBlockTick(pos, this, TICK_DELAY);
        }

        return state;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(DISTANCE, PERSISTENT, WATERLOGGED);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
        BlockState blockState = getDefaultState()
                .with(PERSISTENT, Boolean.TRUE)
                .with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
        return updateDistanceFromLogs(blockState, ctx.getWorld(), ctx.getBlockPos());
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (world.hasRain(pos.up())) {
            if (random.nextInt(15) == 1) {
                BlockPos blockPos = pos.down();
                BlockState blockState = world.getBlockState(blockPos);
                if (!blockState.isOpaque() || !blockState.isSideSolidFullSquare(world, blockPos, Direction.UP)) {
                    ParticleUtil.spawnParticle(world, pos, random, ParticleTypes.DRIPPING_WATER);
                }
            }
        }
    }

    private void spawnLeafParticle(World world, BlockPos pos, Random random, BlockState state, BlockPos posBelow) {
        if (!(random.nextFloat() >= 0.01F)) {
            if (!isFaceFullSquare(state.getCollisionShape(world, posBelow), Direction.UP)) {
                this.spawnLeafParticle(world, pos, random);
            }
        }
    }

    protected void spawnLeafParticle(World world, BlockPos pos, Random random) {
        var tintedParticleEffect = TintedParticleEffect.create(ParticleTypes.TINTED_LEAVES, world.getBlockColor(pos));
        ParticleUtil.spawnParticle(world, pos, random, tintedParticleEffect);
    }
}
