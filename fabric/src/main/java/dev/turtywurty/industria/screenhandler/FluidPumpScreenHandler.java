package dev.turtywurty.industria.screenhandler;

import dev.turtywurty.industria.blockentity.FluidPumpBlockEntity;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.ScreenHandlerTypeInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;

public class FluidPumpScreenHandler extends AbstractContainerMenu {
    private final FluidPumpBlockEntity blockEntity;
    private final ContainerLevelAccess context;

    public FluidPumpScreenHandler(int syncId, Inventory playerInventory, BlockPosPayload payload) {
        this(syncId, (FluidPumpBlockEntity) playerInventory.player.level().getBlockEntity(payload.pos()));
    }

    public FluidPumpScreenHandler(int syncId, FluidPumpBlockEntity blockEntity) {
        super(ScreenHandlerTypeInit.FLUID_PUMP, syncId);

        this.blockEntity = blockEntity;
        this.context = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.context, player, BlockInit.FLUID_PUMP);
    }

    public FluidPumpBlockEntity getBlockEntity() {
        return this.blockEntity;
    }
}
