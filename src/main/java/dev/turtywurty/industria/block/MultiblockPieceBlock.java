package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.block.abstraction.Wrenchable;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.ComponentTypeInit;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;

public class MultiblockPieceBlock extends IndustriaBlock implements Wrenchable {
    public MultiblockPieceBlock(Settings settings) {
        super(settings, new BlockProperties()
                .blockEntityProperties(
                        new BlockProperties.BlockBlockEntityProperties<>(() -> BlockEntityTypeInit.MULTIBLOCK_PIECE))
                .hasBlockEntityRenderer());
    }

    @Override
    public ActionResult onWrenched(ServerWorld world, BlockPos pos, PlayerEntity player, ItemUsageContext context) {
        ItemStack stack = context.getStack();
        stack.set(ComponentTypeInit.MULTIBLOCK_PIECE_POS, pos);
        return ActionResult.SUCCESS;
    }
}