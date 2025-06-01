package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.blockentity.FractionalDistillationControllerBlockEntity;
import dev.turtywurty.industria.blockentity.FractionalDistillationTowerBlockEntity;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FractionalDistillationTowerBlock extends IndustriaBlock {
    public FractionalDistillationTowerBlock(Settings settings) {
        super(settings, new BlockProperties()
                .hasHorizontalFacing()
                .canExistAt((world, pos) -> {
                    BlockState blockState = world.getBlockState(pos.down());
                    return blockState.isOf(BlockInit.FRACTIONAL_DISTILLATION_TOWER) || blockState.isOf(BlockInit.FRACTIONAL_DISTILLATION_CONTROLLER);
                })
                .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(() -> BlockEntityTypeInit.FRACTIONAL_DISTILLATION_TOWER)
                        .shouldTick()));
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient && world.getBlockEntity(pos) instanceof FractionalDistillationTowerBlockEntity blockEntity) {
            BlockPos controllerPos = blockEntity.getControllerPos();
            if (controllerPos != null && world.getBlockEntity(controllerPos) instanceof FractionalDistillationControllerBlockEntity controller) {
                player.openHandledScreen(controller);
            }
        }

        return ActionResult.SUCCESS_SERVER;
    }
}
