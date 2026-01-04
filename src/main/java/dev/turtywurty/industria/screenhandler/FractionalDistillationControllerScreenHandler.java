package dev.turtywurty.industria.screenhandler;

import dev.turtywurty.industria.blockentity.FractionalDistillationControllerBlockEntity;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.ScreenHandlerTypeInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;

public class FractionalDistillationControllerScreenHandler extends AbstractContainerMenu {
    private final FractionalDistillationControllerBlockEntity blockEntity;
    private final ContainerLevelAccess context;

    public FractionalDistillationControllerScreenHandler(int syncId, Inventory playerInv, BlockPosPayload payload) {
        this(syncId, playerInv, (FractionalDistillationControllerBlockEntity) playerInv.player.level().getBlockEntity(payload.pos()));
    }

    public FractionalDistillationControllerScreenHandler(int syncId, Inventory playerInv, FractionalDistillationControllerBlockEntity blockEntity) {
        super(ScreenHandlerTypeInit.FRACTIONAL_DISTILLATION_CONTROLLER, syncId);
        this.blockEntity = blockEntity;
        this.context = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());

        addStandardInventorySlots(playerInv, 8, 84);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.context, player, BlockInit.FRACTIONAL_DISTILLATION_CONTROLLER) ||
                stillValid(this.context, player, BlockInit.FRACTIONAL_DISTILLATION_TOWER);
    }

    public FractionalDistillationControllerBlockEntity getBlockEntity() {
        return this.blockEntity;
    }
}
