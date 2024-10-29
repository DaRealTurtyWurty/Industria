package dev.turtywurty.industria.block;

import dev.turtywurty.industria.init.AttachmentTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.multiblock.MultiblockData;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import team.reborn.energy.api.EnergyStorage;

import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public class MultiblockBlock extends Block {
    public MultiblockBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient) {
            Map<String, MultiblockData> map = world.getChunk(pos).getAttached(AttachmentTypeInit.MULTIBLOCK_ATTACHMENT);
            if (map == null)
                return ActionResult.FAIL;

            MultiblockData data = map.get(pos.toShortString());

            BlockPos primaryPos = data.primaryPos();
            if (primaryPos == null)
                return ActionResult.FAIL;

            data.type().onPrimaryBlockUse(world, player, hit, primaryPos);
        }

        return ActionResult.SUCCESS;
    }

    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        super.onStateReplaced(state, world, pos, newState, moved);

        if (!world.isClient && !state.isOf(newState.getBlock())) {
            Map<String, MultiblockData> map = world.getChunk(pos).getAttached(AttachmentTypeInit.MULTIBLOCK_ATTACHMENT);
            if (map == null)
                return;

            MultiblockData data = map.get(pos.toShortString());

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

    public static EnergyStorage getEnergyProvider(World world, BlockPos pos, BlockState state, BlockEntity blockEntity, Object context) {
        if(!state.isOf(BlockInit.MULTIBLOCK_BLOCK))
            return null;

        Map<String, MultiblockData> map = world.getChunk(pos).getAttached(AttachmentTypeInit.MULTIBLOCK_ATTACHMENT);
        if (map == null)
            return null;

        MultiblockData data = map.get(pos.toShortString());
        BlockPos primaryPos = data.primaryPos();
        if (primaryPos == null)
            return null;

        return data.type().getEnergyProvider(blockEntity, context instanceof Direction direction ? direction : null);
    }

    public static InventoryStorage getInventoryProvider(World world, BlockPos pos, BlockState state, BlockEntity blockEntity, Object context) {
        if(!state.isOf(BlockInit.MULTIBLOCK_BLOCK))
            return null;

        Map<String, MultiblockData> map = world.getChunk(pos).getAttached(AttachmentTypeInit.MULTIBLOCK_ATTACHMENT);
        if (map == null)
            return null;

        MultiblockData data = map.get(pos.toShortString());
        BlockPos primaryPos = data.primaryPos();
        if (primaryPos == null)
            return null;

        return data.type().getInventoryProvider(blockEntity, context instanceof Direction direction ? direction : null);
    }

    public static Storage<FluidVariant> getFluidProvider(World world, BlockPos pos, BlockState state, BlockEntity blockEntity, Object context) {
        if(!state.isOf(BlockInit.MULTIBLOCK_BLOCK))
            return null;

        Map<String, MultiblockData> map = world.getChunk(pos).getAttached(AttachmentTypeInit.MULTIBLOCK_ATTACHMENT);
        if (map == null)
            return null;

        MultiblockData data = map.get(pos.toShortString());
        BlockPos primaryPos = data.primaryPos();
        if (primaryPos == null)
            return null;

        return data.type().getFluidProvider(blockEntity, context instanceof Direction direction ? direction : null);
    }
}
