package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.blockentity.TappingOutletBlockEntity;
import dev.turtywurty.industria.init.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class TappingOutletBlock extends IndustriaBlock {
    public TappingOutletBlock(Properties settings) {
        super(settings, new BlockProperties()
                .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(
                        ModBlockEntityTypes.TAPPING_OUTLET)
                        .shouldTick())
                .hasHorizontalFacing());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TappingOutletBlockEntity blockEntity) {
            blockEntity.startPouring();
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockState state = super.getStateForPlacement(ctx);
        if (state == null)
            return null;

        Direction face = ctx.getClickedFace();
        if (face.getAxis().isHorizontal())
            return state.setValue(BlockStateProperties.HORIZONTAL_FACING, face);

        return state;
    }
}
