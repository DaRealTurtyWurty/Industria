package dev.turtywurty.industria.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.OptionalInt;

public class RubberLeavesBlock extends Block implements SimpleWaterloggedBlock {
    public static final MapCodec<RubberLeavesBlock> CODEC = simpleCodec(RubberLeavesBlock::new);

    public static final int DECAY_DISTANCE = 13;
    public static final IntegerProperty DISTANCE = IntegerProperty.create("distance", 1, DECAY_DISTANCE);
    public static final BooleanProperty PERSISTENT = BlockStateProperties.PERSISTENT;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final int TICK_DELAY = 1;

    public RubberLeavesBlock(Properties settings) {
        super(settings.mapColor(MapColor.PLANT)
                .strength(0.2F)
                .randomTicks()
                .sound(SoundType.GRASS)
                .noOcclusion()
                .isValidSpawn(Blocks::ocelotOrParrot)
                .isSuffocating(Blocks::never)
                .isViewBlocking(Blocks::never)
                .ignitedByLava()
                .pushReaction(PushReaction.DESTROY)
                .isRedstoneConductor(Blocks::never));

        registerDefaultState(this.stateDefinition.any()
                .setValue(DISTANCE, DECAY_DISTANCE)
                .setValue(PERSISTENT, Boolean.FALSE)
                .setValue(WATERLOGGED, Boolean.FALSE));
    }

    @Override
    public MapCodec<? extends RubberLeavesBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getBlockSupportShape(BlockState state, BlockGetter world, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return state.getValue(DISTANCE) == DECAY_DISTANCE && !state.getValue(PERSISTENT);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        if (this.shouldDecay(state)) {
            dropResources(state, world, pos);
            world.removeBlock(pos, false);
        }
    }

    protected boolean shouldDecay(BlockState state) {
        return !state.getValue(PERSISTENT) && state.getValue(DISTANCE) == DECAY_DISTANCE;
    }

    @Override
    protected int getLightBlock(BlockState state) {
        return 1;
    }

    @Override
    protected void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        world.setBlock(pos, updateDistanceFromLogs(state, world, pos), Block.UPDATE_ALL);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader world, ScheduledTickAccess tickView,
                                     BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        if (state.getValue(WATERLOGGED)) {
            tickView.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        }

        int distance = getDistanceFromLog(neighborState) + 1;
        if (distance != 1 || state.getValue(DISTANCE) != distance) {
            tickView.scheduleTick(pos, this, TICK_DELAY);
        }

        return state;
    }

    private static BlockState updateDistanceFromLogs(BlockState state, LevelAccessor world, BlockPos pos) {
        int distance = DECAY_DISTANCE;
        var mutable = new BlockPos.MutableBlockPos();

        for (Direction direction : Direction.values()) {
            mutable.setWithOffset(pos, direction);
            distance = Math.min(distance, getDistanceFromLog(world.getBlockState(mutable)) + 1);
            if (distance == 1) {
                break;
            }
        }

        return state.setValue(DISTANCE, distance);
    }

    private static int getDistanceFromLog(BlockState state) {
        return getOptionalDistanceFromLog(state).orElse(DECAY_DISTANCE);
    }

    public static OptionalInt getOptionalDistanceFromLog(BlockState state) {
        if (state.is(BlockTags.LOGS)) {
            return OptionalInt.of(0);
        } else if (state.hasProperty(DISTANCE)) {
            return OptionalInt.of(state.getValue(DISTANCE));
        } else if (state.hasProperty(LeavesBlock.DISTANCE)) {
            return OptionalInt.of(state.getValue(LeavesBlock.DISTANCE));
        } else {
            return OptionalInt.empty();
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DISTANCE, PERSISTENT, WATERLOGGED);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        FluidState fluidState = ctx.getLevel().getFluidState(ctx.getClickedPos());
        BlockState blockState = defaultBlockState()
                .setValue(PERSISTENT, Boolean.TRUE)
                .setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
        return updateDistanceFromLogs(blockState, ctx.getLevel(), ctx.getClickedPos());
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
        if (world.isRainingAt(pos.above())) {
            if (random.nextInt(15) == 1) {
                BlockPos blockPos = pos.below();
                BlockState blockState = world.getBlockState(blockPos);
                if (!blockState.canOcclude() || !blockState.isFaceSturdy(world, blockPos, Direction.UP)) {
                    ParticleUtils.spawnParticleBelow(world, pos, random, ParticleTypes.DRIPPING_WATER);
                }
            }
        }
    }

    private void spawnLeafParticle(Level world, BlockPos pos, RandomSource random, BlockState state, BlockPos posBelow) {
        if (!(random.nextFloat() >= 0.01F)) {
            if (!isFaceFull(state.getCollisionShape(world, posBelow), Direction.UP)) {
                this.spawnLeafParticle(world, pos, random);
            }
        }
    }

    protected void spawnLeafParticle(Level world, BlockPos pos, RandomSource random) {
        var tintedParticleEffect = ColorParticleOption.create(ParticleTypes.TINTED_LEAVES, world.getClientLeafTintColor(pos));
        ParticleUtils.spawnParticleBelow(world, pos, random, tintedParticleEffect);
    }
}
