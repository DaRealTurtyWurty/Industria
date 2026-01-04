package dev.turtywurty.industria.multiblock.old;

import dev.turtywurty.industria.init.AttachmentTypeInit;
import dev.turtywurty.industria.util.CachedVoxelShapes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public class AutoMultiblockBlock extends Block {
    public static final CachedVoxelShapes SHAPE_CACHE = new CachedVoxelShapes((world, pos) -> {
        MultiblockData data = getMultiblockData(world, pos);
        if (data == null || data.primaryPos() == null)
            return Shapes.empty();

        BlockState primaryState = world.getBlockState(data.primaryPos());
        Direction direction = data.type().hasDirectionProperty() ? primaryState.getValueOrElse(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH) : Direction.NORTH;
        Vec3i offset = getOffsetFromPrimary(data.primaryPos(), pos, null);

        VoxelShape shape = data.type().getShape(world, data.primaryPos(), direction);
        return shape != null ? shape.move(-offset.getX(), -offset.getY(), -offset.getZ()) : Shapes.empty();
    });

    public AutoMultiblockBlock(Properties settings) {
        super(settings);
    }

    public static Vec3i getOffsetFromPrimary(BlockPos primaryPos, BlockPos pos, @Nullable Direction multiblockRotation) {
        int dx = pos.getX() - primaryPos.getX();
        int dy = pos.getY() - primaryPos.getY();
        int dz = pos.getZ() - primaryPos.getZ();

        if (multiblockRotation == null) {
            multiblockRotation = Direction.NORTH;
        }

        return switch (multiblockRotation) {
            case NORTH -> new Vec3i(dx, dy, dz);
            case SOUTH -> new Vec3i(-dx, dy, -dz);
            case WEST -> new Vec3i(dz, dy, -dx);
            case EAST -> new Vec3i(-dz, dy, dx);
            default -> new Vec3i(dx, dy, dz);
        };
    }

    public static MultiblockData getMultiblockData(LevelReader world, BlockPos pos) {
        Map<BlockPos, MultiblockData> map = world.getChunk(pos).getAttached(AttachmentTypeInit.MULTIBLOCK_ATTACHMENT);
        if (map == null)
            return null;

        return map.get(pos);
    }

    public static BlockPos getPrimaryPos(LevelReader world, BlockPos pos) {
        MultiblockData data = getMultiblockData(world, pos);
        return data != null ? data.primaryPos() : null;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (!world.isClientSide()) {
            MultiblockData data = getMultiblockData(world, pos);
            if (data == null)
                return InteractionResult.FAIL;

            BlockPos primaryPos = data.primaryPos();
            if (primaryPos == null)
                return InteractionResult.FAIL;

            data.type().onPrimaryBlockUse(world, player, hit, primaryPos);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel world, BlockPos pos, boolean moved) {
        BlockState newState = world.getBlockState(pos);
        if (!world.isClientSide() && !state.is(newState.getBlock())) {
            MultiblockData data = getMultiblockData(world, pos);
            if (data == null)
                return;

            BlockPos primaryPos = data.primaryPos();
            if (primaryPos == null)
                return;

            data.type().onMultiblockBreak(world, primaryPos);
        }

        super.affectNeighborsAfterRemoval(state, world, pos, moved);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        if (!(world instanceof LevelReader worldView)) {
            return Shapes.empty();
        }

        return SHAPE_CACHE.getShape(worldView, pos);
    }
}
