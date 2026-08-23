package dev.turtywurty.industria.menu;

import dev.turtywurty.industria.blockentity.ThermalGeneratorBlockEntity;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorage;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.init.ModMenuTypes;
import dev.turtywurty.industria.menu.base.IndustriaScreenHandler;
import dev.turtywurty.industria.menu.slot.PredicateSlot;
import dev.turtywurty.industria.network.BlockPosPayload;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.Fluid;

public class ThermalGeneratorScreenHandler extends IndustriaScreenHandler<ThermalGeneratorBlockEntity, BlockPosPayload> {
    public ThermalGeneratorScreenHandler(int syncId, Inventory playerInventory, BlockPosPayload payload) {
        super(ModMenuTypes.THERMAL_GENERATOR.get(), syncId, playerInventory, payload, ThermalGeneratorBlockEntity.class);
    }

    public ThermalGeneratorScreenHandler(int syncId, Inventory playerInventory, ThermalGeneratorBlockEntity blockEntity, WrappedContainerStorage<?> wrappedContainerStorage) {
        super(ModMenuTypes.THERMAL_GENERATOR.get(), syncId, playerInventory, blockEntity, wrappedContainerStorage);
    }

    @Override
    protected int getInventorySize() {
        return 1;
    }

    @Override
    protected void addBlockEntitySlots(Inventory playerInventory) {
        addSlot(new PredicateSlot(this.wrappedContainerStorage.getInventory(0), 0, 80, 35,
                itemStack -> this.blockEntity.isValid(itemStack, 0)));
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.context, player, ModBlocks.THERMAL_GENERATOR.get());
    }

    public long getEnergy() {
        return this.blockEntity.getWrappedEnergyStorage().getAmount();
    }

    public long getMaxEnergy() {
        return this.blockEntity.getWrappedEnergyStorage().getCapacity();
    }

    public long getFluidAmount() {
        return this.blockEntity.getWrappedFluidStorage().getAmount();
    }

    public long getFluidCapacity() {
        return this.blockEntity.getWrappedFluidStorage().getCapacity();
    }

    public Fluid getFluid() {
        return this.blockEntity.getWrappedFluidStorage().getResource().value();
    }
}
