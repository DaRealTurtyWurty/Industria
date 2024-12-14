package dev.turtywurty.industria.screenhandler;

import dev.turtywurty.industria.blockentity.FractionalDistillationControllerBlockEntity;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.ScreenHandlerTypeInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;

public class FractionalDistillationControllerScreenHandler extends ScreenHandler {
    private final FractionalDistillationControllerBlockEntity blockEntity;
    private final ScreenHandlerContext context;

    public FractionalDistillationControllerScreenHandler(int syncId, PlayerInventory playerInv, BlockPosPayload payload) {
        this(syncId, playerInv, (FractionalDistillationControllerBlockEntity) playerInv.player.getWorld().getBlockEntity(payload.pos()));
    }

    public FractionalDistillationControllerScreenHandler(int syncId, PlayerInventory playerInv, FractionalDistillationControllerBlockEntity blockEntity) {
        super(ScreenHandlerTypeInit.FRACTIONAL_DISTILLATION_CONTROLLER, syncId);
        this.blockEntity = blockEntity;
        this.context = ScreenHandlerContext.create(blockEntity.getWorld(), blockEntity.getPos());

        addPlayerSlots(playerInv, 8, 84);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(this.context, player, BlockInit.FRACTIONAL_DISTILLATION_CONTROLLER) ||
                canUse(this.context, player, BlockInit.FRACTIONAL_DISTILLATION_TOWER);
    }

    public FractionalDistillationControllerBlockEntity getBlockEntity() {
        return this.blockEntity;
    }
}
