package dev.turtywurty.industria.screenhandler;

import dev.turtywurty.industria.blockentity.CombustionGeneratorBlockEntity;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorage;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.ScreenHandlerTypeInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.screenhandler.base.IndustriaScreenHandler;
import dev.turtywurty.industria.screenhandler.slot.PredicateSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public class CombustionGeneratorScreenHandler extends IndustriaScreenHandler<CombustionGeneratorBlockEntity, BlockPosPayload> {
    public CombustionGeneratorScreenHandler(int syncId, Inventory playerInventory, BlockPosPayload payload) {
        super(ScreenHandlerTypeInit.COMBUSTION_GENERATOR, syncId, playerInventory, payload, CombustionGeneratorBlockEntity.class);
    }

    public CombustionGeneratorScreenHandler(int syncId, Inventory playerInventory, CombustionGeneratorBlockEntity blockEntity, WrappedContainerStorage<?> wrappedContainerStorage) {
        super(ScreenHandlerTypeInit.COMBUSTION_GENERATOR, syncId, playerInventory, blockEntity, wrappedContainerStorage);
    }

    @Override
    protected int getInventorySize() {
        return 1;
    }

    @Override
    protected void addBlockEntitySlots(Inventory playerInventory) {
        addSlot(new PredicateSlot(this.wrappedContainerStorage.getInventory(0), 0, 81, 42,
                itemStack -> this.blockEntity.isValid(itemStack, 0)));
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.context, player, BlockInit.COMBUSTION_GENERATOR);
    }

    public long getEnergy() {
        return this.blockEntity.getEnergyStorage().getAmount();
    }

    public long getMaxEnergy() {
        return this.blockEntity.getEnergyStorage().getCapacity();
    }

    public int getBurnTime() {
        return this.blockEntity.getBurnTime();
    }

    public int getFuelTime() {
        return this.blockEntity.getFuelTime();
    }
}
