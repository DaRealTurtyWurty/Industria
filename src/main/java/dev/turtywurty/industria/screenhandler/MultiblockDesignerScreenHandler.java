package dev.turtywurty.industria.screenhandler;

import dev.turtywurty.industria.blockentity.MultiblockDesignerBlockEntity;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.ScreenHandlerTypeInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;

public class MultiblockDesignerScreenHandler extends AbstractContainerMenu {
    private final ContainerLevelAccess context;
    private final MultiblockDesignerBlockEntity blockEntity;

    public MultiblockDesignerScreenHandler(int syncId, Inventory playerInv, MultiblockDesignerBlockEntity blockEntity) {
        super(ScreenHandlerTypeInit.MULTIBLOCK_DESIGNER, syncId);
        this.context = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());
        this.blockEntity = blockEntity;
    }

    public MultiblockDesignerScreenHandler(int syncId, Inventory playerInv, BlockPosPayload payload) {
        this(syncId, playerInv, (MultiblockDesignerBlockEntity) playerInv.player.level().getBlockEntity(payload.pos()));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(context, player, BlockInit.MULTIBLOCK_DESIGNER);
    }

    public MultiblockDesignerBlockEntity getBlockEntity() {
        return this.blockEntity;
    }
}
