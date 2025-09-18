package dev.turtywurty.industria.item;

import dev.turtywurty.industria.block.abstraction.Wrenchable;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WrenchItem extends Item {
    public WrenchItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        if (!(world instanceof ServerWorld serverWorld))
            return ActionResult.PASS;

        PlayerEntity player = context.getPlayer();
        if (player == null)
            return ActionResult.PASS;

        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof Wrenchable wrenchable))
            return ActionResult.PASS;

        return wrenchable.onWrenched(serverWorld, pos, player, context);
    }
}
