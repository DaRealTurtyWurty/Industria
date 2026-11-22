package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

public class HotAirBlowerBlock extends IndustriaBlock {
    public HotAirBlowerBlock(Properties settings) {
        super(settings, new BlockProperties()
            .hasHorizontalFacing());
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockState state = super.getStateForPlacement(ctx);
        if (state == null)
            return null;

        Direction clickedFace = ctx.getClickedFace();
        Direction airOutput = clickedFace.getAxis().isHorizontal()
            ? clickedFace.getOpposite()
            : ctx.getHorizontalDirection().getOpposite();

        return state.setValue(BlockStateProperties.HORIZONTAL_FACING, airOutput);
    }
}
