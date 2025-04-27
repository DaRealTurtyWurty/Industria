package dev.turtywurty.industria.block;

import dev.turtywurty.industria.init.AttachmentTypeInit;
import dev.turtywurty.industria.multiblock.MultiblockData;
import dev.turtywurty.industria.util.CachedVoxelShapes;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public class MultiblockBlock extends Block {
    public static final CachedVoxelShapes SHAPE_CACHE = new CachedVoxelShapes((world, pos) -> {
        MultiblockData data = getMultiblockData(world, pos);
        if (data == null || data.primaryPos() == null)
            return VoxelShapes.empty();

        BlockState primaryState = world.getBlockState(data.primaryPos());
        Direction direction = data.type().hasDirectionProperty() ? primaryState.get(Properties.HORIZONTAL_FACING, null) : Direction.NORTH;
        if(direction == null)
            direction = Direction.NORTH;

        Vec3i offset = getOffsetFromPrimary(data.primaryPos(), pos, null);

        VoxelShape shape = data.type().getShape(world, data.primaryPos(), direction);
        return shape != null ? shape.offset(-offset.getX(), -offset.getY(), -offset.getZ()) : VoxelShapes.empty();
    });

    public MultiblockBlock(Settings settings) {
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

    public static MultiblockData getMultiblockData(WorldView world, BlockPos pos) {
        Map<BlockPos, MultiblockData> map = world.getChunk(pos).getAttached(AttachmentTypeInit.MULTIBLOCK_ATTACHMENT);
        if (map == null)
            return null;

        return map.get(pos);
    }

    public static BlockPos getPrimaryPos(WorldView world, BlockPos pos) {
        MultiblockData data = getMultiblockData(world, pos);
        return data != null ? data.primaryPos() : null;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient) {
            MultiblockData data = getMultiblockData(world, pos);
            if (data == null)
                return ActionResult.FAIL;

            BlockPos primaryPos = data.primaryPos();
            if (primaryPos == null)
                return ActionResult.FAIL;

            data.type().onPrimaryBlockUse(world, player, hit, primaryPos);
        }

        return ActionResult.SUCCESS;
    }

    @Override
    protected void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean moved) {
        super.onStateReplaced(state, world, pos, moved);

        BlockState newState = world.getBlockState(pos);
        if (!world.isClient && !state.isOf(newState.getBlock())) {
            MultiblockData data = getMultiblockData(world, pos);
            if (data == null)
                return;

            BlockPos primaryPos = data.primaryPos();
            if (primaryPos == null)
                return;

            data.type().onMultiblockBreak(world, primaryPos);
        }
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.INVISIBLE;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if (!(world instanceof WorldView worldView)) {
            return VoxelShapes.empty();
        }

        return SHAPE_CACHE.getShape(worldView, pos);
    }
}
