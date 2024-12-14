package dev.turtywurty.industria.screenhandler;

import dev.turtywurty.industria.blockentity.InductionHeaterBlockEntity;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.ScreenHandlerTypeInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;

public class InductionHeaterScreenHandler extends ScreenHandler {
    private final InductionHeaterBlockEntity blockEntity;
    private final ScreenHandlerContext context;

    public InductionHeaterScreenHandler(int syncId, PlayerInventory playerInv, BlockPosPayload payload) {
        this(syncId, playerInv, (InductionHeaterBlockEntity) playerInv.player.getWorld().getBlockEntity(payload.pos()));
    }

    public InductionHeaterScreenHandler(int syncId, PlayerInventory playerInv, InductionHeaterBlockEntity blockEntity) {
        super(ScreenHandlerTypeInit.INDUCTION_HEATER, syncId);
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
        return canUse(this.context, player, BlockInit.INDUCTION_HEATER);
    }

    public InductionHeaterBlockEntity getBlockEntity() {
        return this.blockEntity;
    }
}
