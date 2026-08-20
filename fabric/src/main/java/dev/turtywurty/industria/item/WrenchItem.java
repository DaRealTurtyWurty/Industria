package dev.turtywurty.industria.item;

import dev.turtywurty.industria.block.abstraction.Wrenchable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class WrenchItem extends Item {
    public WrenchItem(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        if (!(world instanceof ServerLevel serverWorld))
            return InteractionResult.PASS;

        Player player = context.getPlayer();
        if (player == null)
            return InteractionResult.PASS;

        BlockPos pos = context.getClickedPos();
        BlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof Wrenchable wrenchable))
            return InteractionResult.PASS;

        return wrenchable.onWrenched(serverWorld, pos, player, context);
    }
}
