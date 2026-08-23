package dev.turtywurty.industria.menu;

import dev.turtywurty.industria.blockentity.InductionHeaterBlockEntity;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.init.ModMenuTypes;
import dev.turtywurty.industria.network.BlockPosPayload;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;

public class InductionHeaterScreenHandler extends AbstractContainerMenu {
    private final InductionHeaterBlockEntity blockEntity;
    private final ContainerLevelAccess context;

    public InductionHeaterScreenHandler(int syncId, Inventory playerInv, BlockPosPayload payload) {
        this(syncId, (InductionHeaterBlockEntity) playerInv.player.level().getBlockEntity(payload.pos()));
    }

    public InductionHeaterScreenHandler(int syncId, InductionHeaterBlockEntity blockEntity) {
        super(ModMenuTypes.INDUCTION_HEATER.get(), syncId);
        this.blockEntity = blockEntity;
        this.context = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.context, player, ModBlocks.INDUCTION_HEATER.get());
    }

    public InductionHeaterBlockEntity getBlockEntity() {
        return this.blockEntity;
    }
}
