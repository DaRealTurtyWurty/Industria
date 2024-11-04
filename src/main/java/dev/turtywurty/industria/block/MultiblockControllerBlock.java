package dev.turtywurty.industria.block;

import dev.turtywurty.industria.multiblock.MultiblockType;
import dev.turtywurty.industria.multiblock.Multiblockable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

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

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        if(!world.isClient) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if(blockEntity instanceof Multiblockable multiblockable) {
                multiblockable.buildMultiblock(world, pos, state, placer, itemStack, blockEntity::markDirty);
            }
        }
    }
}
