package dev.turtywurty.industria.block.abstraction;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;

public interface Wrenchable {
    InteractionResult onWrenched(ServerLevel world, BlockPos pos, Player player, UseOnContext context);
}
