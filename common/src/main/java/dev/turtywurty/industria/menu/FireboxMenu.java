package dev.turtywurty.industria.menu;

import dev.turtywurty.industria.blockentity.FireboxBlockEntity;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorage;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.init.ModMenuTypes;
import dev.turtywurty.industria.menu.base.IndustriaScreenHandler;
import dev.turtywurty.industria.menu.slot.PredicateSlot;
import dev.turtywurty.industria.network.BlockPosPayload;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;
import java.util.function.Predicate;

public class FireboxMenu extends IndustriaScreenHandler<FireboxBlockEntity, BlockPosPayload> {
    public FireboxMenu(int syncId, Inventory playerInventory, BlockPosPayload payload) {
        super(ModMenuTypes.FIREBOX.get(), 2, syncId, playerInventory, payload, FireboxBlockEntity.class);
    }

    public FireboxMenu(int syncId, Inventory playerInventory, FireboxBlockEntity blockEntity,
                       WrappedContainerStorage<?> wrappedContainerStorage, ContainerData data) {
        super(ModMenuTypes.FIREBOX.get(), syncId, playerInventory, blockEntity, wrappedContainerStorage, data);
    }

    @Override
    protected void addBlockEntitySlots(Inventory playerInventory) {
        SimpleContainer inventory = Objects.requireNonNull(this.wrappedContainerStorage.getInventory(0));

        Predicate<ItemStack> fuelPredicate = stack -> playerInventory.player.level().fuelValues().isFuel(stack);
        for (int slot = 0; slot < 3; slot++) {
            addSlot(new PredicateSlot(inventory, slot, 62 + (slot * 18), 35, fuelPredicate));
        }
    }

    @Override
    protected int getInventorySize() {
        return 3;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.context, player, ModBlocks.FIREBOX.get());
    }

    public float getBurnProgress() {
        return this.propertyDelegate.get(0) / 1000f;
    }

    public boolean isBurning() {
        return this.propertyDelegate.get(1) == 1;
    }
}
