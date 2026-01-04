package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.block.abstraction.Wrenchable;
import dev.turtywurty.industria.blockentity.MultiblockDesignerBlockEntity;
import dev.turtywurty.industria.blockentity.MultiblockPieceBlockEntity;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.ComponentTypeInit;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

public class MultiblockDesignerBlock extends IndustriaBlock implements Wrenchable {
    public MultiblockDesignerBlock(Properties settings) {
        super(settings, new BlockProperties()
                .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(
                        () -> BlockEntityTypeInit.MULTIBLOCK_DESIGNER)
                        .rightClickToOpenGui()));
    }

    @Override
    public InteractionResult onWrenched(ServerLevel world, BlockPos pos, Player player, UseOnContext context) {
        ItemStack stack = context.getItemInHand();

        boolean success = false;
        if (stack.hasNonDefault(ComponentTypeInit.MULTIBLOCK_PIECE_POS)) {
            BlockPos piece = stack.remove(ComponentTypeInit.MULTIBLOCK_PIECE_POS);
            if (world.getBlockEntity(piece) instanceof MultiblockPieceBlockEntity pieceEntity) {
                if (pieceEntity.getDesignerPos() != null && world.getBlockEntity(pieceEntity.getDesignerPos()) instanceof MultiblockDesignerBlockEntity oldDesigner) {
                    oldDesigner.removePiece(piece);
                }

                if (world.getBlockEntity(pos) instanceof MultiblockDesignerBlockEntity designer) {
                    designer.addPiece(piece);
                    pieceEntity.setDesignerPos(pos);
                    success = true;
                }
            }
        }

        return success ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }
}