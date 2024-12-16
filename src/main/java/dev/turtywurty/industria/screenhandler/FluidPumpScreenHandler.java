package dev.turtywurty.industria.screenhandler;

import dev.turtywurty.industria.blockentity.FluidPumpBlockEntity;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.ScreenHandlerTypeInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;

public class FluidPumpScreenHandler extends ScreenHandler {
    private final FluidPumpBlockEntity blockEntity;
    private final ScreenHandlerContext context;

    public FluidPumpScreenHandler(int syncId, PlayerInventory playerInventory, BlockPosPayload payload) {
        this(syncId, (FluidPumpBlockEntity) playerInventory.player.getWorld().getBlockEntity(payload.pos()));
    }

    public FluidPumpScreenHandler(int syncId, FluidPumpBlockEntity blockEntity) {
        super(ScreenHandlerTypeInit.FLUID_PUMP, syncId);

        this.blockEntity = blockEntity;
        this.context = ScreenHandlerContext.create(blockEntity.getWorld(), blockEntity.getPos());
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(this.context, player, BlockInit.FLUID_PUMP);
    }

    public FluidPumpBlockEntity getBlockEntity() {
        return this.blockEntity;
    }
}
