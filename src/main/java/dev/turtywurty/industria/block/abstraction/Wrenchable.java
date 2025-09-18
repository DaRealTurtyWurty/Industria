package dev.turtywurty.industria.block.abstraction;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;

public interface Wrenchable {
    ActionResult onWrenched(ServerWorld world, BlockPos pos, PlayerEntity player, ItemUsageContext context);
}
