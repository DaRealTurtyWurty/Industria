package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.blockentity.TreeTapBlockEntity;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class TreeTapBlock extends IndustriaBlock {
    private static final VoxelShape SHAPE = makeShape();
    private static final int[][] CONNECTED_OFFSETS = createConnectedOffsets();

    public TreeTapBlock(Properties settings) {
        super(settings, new BlockProperties()
                .hasHorizontalFacing()
                .hasBlockEntityRenderer()
                .canExistAt(TreeTapBlock::canExist)
                .useRotatedShapes(SHAPE)
                .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(() -> BlockEntityTypeInit.TREE_TAP)
                        .shouldTick()));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockState state = super.getStateForPlacement(ctx);
        if (state == null)
            return null;

        Direction facing = getDirectionWhereCanBePlaced(ctx.getLevel(), ctx.getClickedPos());
        if (facing == null)
            return null;

        return state.setValue(BlockStateProperties.HORIZONTAL_FACING, facing.getOpposite());
    }

    public static Set<BlockPos> findLatexSources(LevelReader level, BlockPos pos, Set<BlockPos> previouslyVisited, Set<BlockPos> visited) {
        visited.add(pos);

        for (int[] offset : CONNECTED_OFFSETS) {
            BlockPos neighbourPos = pos.offset(offset[0], offset[1], offset[2]);
            if (previouslyVisited.contains(neighbourPos) || visited.contains(neighbourPos))
                continue;

            BlockState neighbourState = level.getBlockState(neighbourPos);
            if (neighbourState.getBlock() instanceof LatexBlock) {
                visited.add(neighbourPos);
                findLatexSources(level, neighbourPos, previouslyVisited, visited);
            }
        }

        return visited;
    }

    public static boolean isLatexSourceTapped(LevelReader level, Set<BlockPos> sources) {
        return isLatexSourceTapped(level, sources, null);
    }

    public static boolean isLatexSourceTapped(LevelReader level, Set<BlockPos> sources, @Nullable BlockPos ignoredTapPos) {
        for (BlockPos source : sources) {
            for (Direction direction : Direction.values()) {
                BlockPos neighbourPos = source.relative(direction);
                if (level.getBlockState(neighbourPos).getBlock() instanceof TreeTapBlock
                        && !Objects.equals(neighbourPos, ignoredTapPos))
                    return true;
            }
        }

        return false;
    }

    private static Direction getDirectionWhereCanBePlaced(LevelReader level, BlockPos pos) {
        Map<Set<BlockPos>, Boolean> sourceTappedMap = new HashMap<>(Direction.values().length);
        Set<BlockPos> previouslyVisited = new HashSet<>();

        for (Direction direction : BlockStateProperties.HORIZONTAL_FACING.getPossibleValues()) {
            BlockPos neighbourPos = pos.relative(direction);
            if (!(level.getBlockState(neighbourPos).getBlock() instanceof LatexBlock))
                continue;

            Set<BlockPos> visited = findLatexSources(level, neighbourPos, previouslyVisited, new HashSet<>());
            if (visited.isEmpty())
                continue;

            previouslyVisited.addAll(visited);
            sourceTappedMap.put(visited, isLatexSourceTapped(level, visited));

            if (!sourceTappedMap.get(visited))
                return direction;
        }

        return null;
    }

    private static boolean canSurvive(LevelReader level, BlockPos pos, BlockState state) {
        Direction attachedDirection = state.getValue(BlockStateProperties.HORIZONTAL_FACING).getOpposite();
        BlockPos attachedPos = pos.relative(attachedDirection);
        if (!(level.getBlockState(attachedPos).getBlock() instanceof LatexBlock))
            return false;

        Set<BlockPos> sources = findLatexSources(level, attachedPos, Set.of(), new HashSet<>());
        return !sources.isEmpty() && !isLatexSourceTapped(level, sources, pos);
    }

    private static boolean canExist(LevelReader level, BlockPos pos) {
        return getDirectionWhereCanBePlaced(level, pos) != null;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        return canSurvive(world, pos, state);
    }

    @Override
    protected void neighborChanged(BlockState state, Level world, BlockPos pos, Block sourceBlock, @Nullable Orientation wireOrientation, boolean notify) {
        super.neighborChanged(state, world, pos, sourceBlock, wireOrientation, notify);
        if (!world.isClientSide() && world.getBlockEntity(pos) instanceof TreeTapBlockEntity treeTapBlockEntity) {
            treeTapBlockEntity.markLatexNetworkDirty();
        }
    }

    public static VoxelShape makeShape() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(-0.125, 0.75, 0.875, 0.375, 0.875, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.125, 0.75, 2, 1.125, 0.875, 2.125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.125, 0.75, 1, 0, 0.875, 2), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(1, 0.75, 1, 1.125, 0.875, 2), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.625, 0.75, 0.875, 1.125, 0.875, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.25, 0.875, 0.875, 0.375, 1, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.625, 0.875, 0.875, 0.75, 1, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.25, 0.625, 0.875, 0.75, 0.75, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.375, 0.5, 0.6875, 0.625, 0.875, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0, 0.3125, 0.9375, 0.375, 1), BooleanOp.OR);

        return shape;
    }

    private static int[][] createConnectedOffsets() {
        int[][] offsets = new int[26][3];
        int index = 0;
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0)
                        continue;

                    offsets[index++] = new int[]{x, y, z};
                }
            }
        }

        return offsets;
    }
}
