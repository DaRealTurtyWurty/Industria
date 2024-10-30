package dev.turtywurty.industria.block;

import dev.turtywurty.industria.multiblock.MultiblockType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class MultiblockControllerBlock extends Block implements BlockEntityProvider {
    private final MultiblockType<?> type;

    public MultiblockControllerBlock(Settings settings, MultiblockType<?> type) {
        super(settings);

        this.type = type;
    }

    public MultiblockType<?> getType() {
        return this.type;
    }

    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            this.type.onMultiblockBreak(world, pos);
        }

        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient) {
            this.type.onPrimaryBlockUse(world, player, hit, pos);
        }

        return ActionResult.SUCCESS;
    }
}
