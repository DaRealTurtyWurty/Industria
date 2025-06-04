package dev.turtywurty.industria.screenhandler;

import dev.turtywurty.industria.blockentity.OilPumpJackBlockEntity;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedInventoryStorage;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.ScreenHandlerTypeInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.screenhandler.base.IndustriaScreenHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;

public class OilPumpJackScreenHandler extends IndustriaScreenHandler<OilPumpJackBlockEntity, BlockPosPayload> {
    public OilPumpJackScreenHandler(int syncId, PlayerInventory playerInv, BlockPosPayload payload) {
        super(ScreenHandlerTypeInit.OIL_PUMP_JACK, syncId, playerInv, payload, OilPumpJackBlockEntity.class);
    }

    public OilPumpJackScreenHandler(int syncId, PlayerInventory playerInv, OilPumpJackBlockEntity blockEntity, WrappedInventoryStorage<?> wrappedInventoryStorage) {
        super(ScreenHandlerTypeInit.OIL_PUMP_JACK, syncId, playerInv, blockEntity, wrappedInventoryStorage);
    }

    @Override
    protected int getInventorySize() {
        return 0;
    }

    @Override
    protected void addBlockEntitySlots(PlayerInventory playerInventory) {
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(this.context, player, BlockInit.OIL_PUMP_JACK);
    }
}
