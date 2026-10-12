package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.blockentity.CastingBasinBlockEntity;
import dev.turtywurty.industria.init.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class CastingBasinBlock extends IndustriaBlock {
    public CastingBasinBlock(Properties settings) {
        super(settings, new BlockProperties()
            .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(
                ModBlockEntityTypes.CASTING_BASIN)
                .shouldTick()
                .dropContentsOnBreak()));
    }

    @Override
    protected InteractionResult useWithoutItem(
        BlockState state,
        Level level,
        BlockPos pos,
        Player player,
        BlockHitResult hit
    ) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof CastingBasinBlockEntity blockEntity) {
            blockEntity.collectOutput(player);
        }

        return InteractionResult.SUCCESS;
    }
}
