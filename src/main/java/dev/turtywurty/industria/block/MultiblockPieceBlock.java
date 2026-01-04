package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.block.abstraction.Wrenchable;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.ComponentTypeInit;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

public class MultiblockPieceBlock extends IndustriaBlock implements Wrenchable {
    public MultiblockPieceBlock(Properties settings) {
        super(settings, new BlockProperties()
                .blockEntityProperties(
                        new BlockProperties.BlockBlockEntityProperties<>(() -> BlockEntityTypeInit.MULTIBLOCK_PIECE))
                .hasBlockEntityRenderer());
    }

    @Override
    public InteractionResult onWrenched(ServerLevel world, BlockPos pos, Player player, UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        stack.set(ComponentTypeInit.MULTIBLOCK_PIECE_POS, pos);
        return InteractionResult.SUCCESS;
    }
}