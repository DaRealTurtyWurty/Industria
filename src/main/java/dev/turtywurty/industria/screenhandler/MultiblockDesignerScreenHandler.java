package dev.turtywurty.industria.screenhandler;

import dev.turtywurty.industria.blockentity.MultiblockDesignerBlockEntity;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.ScreenHandlerTypeInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;

public class MultiblockDesignerScreenHandler extends ScreenHandler {
    private final ScreenHandlerContext context;
    private final MultiblockDesignerBlockEntity blockEntity;

    public MultiblockDesignerScreenHandler(int syncId, PlayerInventory playerInv, MultiblockDesignerBlockEntity blockEntity) {
        super(ScreenHandlerTypeInit.MULTIBLOCK_DESIGNER, syncId);
        this.context = ScreenHandlerContext.create(blockEntity.getWorld(), blockEntity.getPos());
        this.blockEntity = blockEntity;
    }

    public MultiblockDesignerScreenHandler(int syncId, PlayerInventory playerInv, BlockPosPayload payload) {
        this(syncId, playerInv, (MultiblockDesignerBlockEntity) playerInv.player.getWorld().getBlockEntity(payload.pos()));
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(context, player, BlockInit.MULTIBLOCK_DESIGNER);
    }

    public MultiblockDesignerBlockEntity getBlockEntity() {
        return this.blockEntity;
    }
}
