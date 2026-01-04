package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.blockentity.FractionalDistillationControllerBlockEntity;
import dev.turtywurty.industria.blockentity.FractionalDistillationTowerBlockEntity;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class FractionalDistillationTowerBlock extends IndustriaBlock {
    public FractionalDistillationTowerBlock(Properties settings) {
        super(settings, new BlockProperties()
                .hasHorizontalFacing()
                .canExistAt((world, pos) -> {
                    BlockState blockState = world.getBlockState(pos.below());
                    return blockState.is(BlockInit.FRACTIONAL_DISTILLATION_TOWER) || blockState.is(BlockInit.FRACTIONAL_DISTILLATION_CONTROLLER);
                })
                .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(() -> BlockEntityTypeInit.FRACTIONAL_DISTILLATION_TOWER)
                        .shouldTick()));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (!world.isClientSide() && world.getBlockEntity(pos) instanceof FractionalDistillationTowerBlockEntity blockEntity) {
            BlockPos controllerPos = blockEntity.getControllerPos();
            if (controllerPos != null && world.getBlockEntity(controllerPos) instanceof FractionalDistillationControllerBlockEntity controller) {
                player.openMenu(controller);
            }
        }

        return InteractionResult.SUCCESS_SERVER;
    }
}
