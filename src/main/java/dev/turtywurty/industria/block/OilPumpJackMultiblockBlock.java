package dev.turtywurty.industria.block;

import dev.turtywurty.industria.blockentity.OilPumpJackBlockEntity;
import dev.turtywurty.industria.init.AttachmentTypeInit;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public class OilPumpJackMultiblockBlock extends Block {
    public OilPumpJackMultiblockBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient) {
            Map<String, BlockPos> map = world.getChunk(pos).getAttached(AttachmentTypeInit.OIL_PUMP_JACK_ATTACHMENT);
            if (map == null)
                return ActionResult.FAIL;

            BlockPos primaryPos = map.get(pos.toShortString());
            if (primaryPos == null)
                return ActionResult.FAIL;

            if (world.getBlockEntity(primaryPos) instanceof OilPumpJackBlockEntity oilPumpJack) {
                player.openHandledScreen(oilPumpJack);
            }
        }

        return ActionResult.success(world.isClient);
    }

    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        super.onStateReplaced(state, world, pos, newState, moved);

        if (!world.isClient && !state.isOf(newState.getBlock())) {
            Map<String, BlockPos> map = world.getChunk(pos).getAttached(AttachmentTypeInit.OIL_PUMP_JACK_ATTACHMENT);
            if (map == null)
                return;

            BlockPos primaryPos = map.get(pos.toShortString());
            if (primaryPos == null)
                return;

            if (world.getBlockEntity(primaryPos) instanceof OilPumpJackBlockEntity oilPumpJack) {
                oilPumpJack.breakMachine();
            }
        }
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.INVISIBLE;
    }
}
