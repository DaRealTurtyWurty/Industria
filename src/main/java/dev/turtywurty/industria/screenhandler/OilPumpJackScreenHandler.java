package dev.turtywurty.industria.screenhandler;

import dev.turtywurty.industria.blockentity.OilPumpJackBlockEntity;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.ScreenHandlerTypeInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;

public class OilPumpJackScreenHandler extends ScreenHandler {
    private final OilPumpJackBlockEntity blockEntity;
    private final ScreenHandlerContext context;

    public OilPumpJackScreenHandler(int syncId, PlayerInventory playerInv, BlockPosPayload payload) {
        this(syncId, playerInv, (OilPumpJackBlockEntity) playerInv.player.getWorld().getBlockEntity(payload.pos()));
    }

    public OilPumpJackScreenHandler(int syncId, PlayerInventory playerInv, OilPumpJackBlockEntity blockEntity) {
        super(ScreenHandlerTypeInit.OIL_PUMP_JACK, syncId);
        this.blockEntity = blockEntity;
        this.context = ScreenHandlerContext.create(blockEntity.getWorld(), blockEntity.getPos());
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(this.context, player, BlockInit.OIL_PUMP_JACK);
    }

    public OilPumpJackBlockEntity getBlockEntity() {
        return this.blockEntity;
    }
}
