package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.block.abstraction.Wrenchable;
import dev.turtywurty.industria.blockentity.MultiblockDesignerBlockEntity;
import dev.turtywurty.industria.blockentity.MultiblockPieceBlockEntity;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.ComponentTypeInit;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;

public class MultiblockDesignerBlock extends IndustriaBlock implements Wrenchable {
    public MultiblockDesignerBlock(Settings settings) {
        super(settings, new BlockProperties()
                .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(
                        () -> BlockEntityTypeInit.MULTIBLOCK_DESIGNER)
                        .rightClickToOpenGui()));
    }

    @Override
    public ActionResult onWrenched(ServerWorld world, BlockPos pos, PlayerEntity player, ItemUsageContext context) {
        ItemStack stack = context.getStack();

        boolean success = false;
        if (stack.hasChangedComponent(ComponentTypeInit.MULTIBLOCK_PIECE_POS)) {
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

        return success ? ActionResult.SUCCESS : ActionResult.PASS;
    }
}