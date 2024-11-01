package dev.turtywurty.industria.screenhandler;

import dev.turtywurty.industria.blockentity.UpgradeStationBlockEntity;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.ScreenHandlerTypeInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;

public class UpgradeStationScreenHandler extends ScreenHandler {
    private final UpgradeStationBlockEntity blockEntity;
    private final ScreenHandlerContext context;

    public UpgradeStationScreenHandler(int syncId, PlayerInventory inventory, BlockPosPayload payload) {
        this(syncId, inventory, (UpgradeStationBlockEntity) inventory.player.getWorld().getBlockEntity(payload.pos()));
    }

    public UpgradeStationScreenHandler(int syncId, PlayerInventory inventory, UpgradeStationBlockEntity blockEntity) {
        super(ScreenHandlerTypeInit.UPGRADE_STATION, syncId);

        this.blockEntity = blockEntity;
        this.context = ScreenHandlerContext.create(blockEntity.getWorld(), blockEntity.getPos());
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(this.context, player, BlockInit.UPGRADE_STATION);
    }

    public UpgradeStationBlockEntity getBlockEntity() {
        return this.blockEntity;
    }
}
