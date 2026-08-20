package dev.turtywurty.industria.screenhandler;

import dev.turtywurty.industria.blockentity.OilPumpJackBlockEntity;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorage;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.ScreenHandlerTypeInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.screenhandler.base.IndustriaScreenHandler;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public class OilPumpJackScreenHandler extends IndustriaScreenHandler<OilPumpJackBlockEntity, BlockPosPayload> {
    public OilPumpJackScreenHandler(int syncId, Inventory playerInv, BlockPosPayload payload) {
        super(ScreenHandlerTypeInit.OIL_PUMP_JACK, syncId, playerInv, payload, OilPumpJackBlockEntity.class);
    }

    public OilPumpJackScreenHandler(int syncId, Inventory playerInv, OilPumpJackBlockEntity blockEntity, WrappedContainerStorage<?> wrappedContainerStorage) {
        super(ScreenHandlerTypeInit.OIL_PUMP_JACK, syncId, playerInv, blockEntity, wrappedContainerStorage);
    }

    @Override
    protected int getInventorySize() {
        return 0;
    }

    @Override
    protected void addBlockEntitySlots(Inventory playerInventory) {
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.context, player, BlockInit.OIL_PUMP_JACK);
    }
}
