package dev.turtywurty.industria.menu;

import dev.turtywurty.industria.blockentity.OilPumpJackBlockEntity;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorage;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.init.ModMenuTypes;
import dev.turtywurty.industria.menu.base.IndustriaScreenHandler;
import dev.turtywurty.industria.network.BlockPosPayload;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public class OilPumpJackScreenHandler extends IndustriaScreenHandler<OilPumpJackBlockEntity, BlockPosPayload> {
    public OilPumpJackScreenHandler(int syncId, Inventory playerInv, BlockPosPayload payload) {
        super(ModMenuTypes.OIL_PUMP_JACK.get(), syncId, playerInv, payload, OilPumpJackBlockEntity.class);
    }

    public OilPumpJackScreenHandler(int syncId, Inventory playerInv, OilPumpJackBlockEntity blockEntity, WrappedContainerStorage<?> wrappedContainerStorage) {
        super(ModMenuTypes.OIL_PUMP_JACK.get(), syncId, playerInv, blockEntity, wrappedContainerStorage);
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
        return stillValid(this.context, player, ModBlocks.OIL_PUMP_JACK.get());
    }
}
