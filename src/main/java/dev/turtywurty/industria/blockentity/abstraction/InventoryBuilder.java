package dev.turtywurty.industria.blockentity.abstraction;

import dev.turtywurty.industria.blockentity.abstraction.component.*;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiPredicate;

public class InventoryBuilder {
    private final Map<Class<? extends Component>, Component> components = new HashMap<>();
    private final int size;
    private final IndustriaSimpleInventory inventory;

    private ItemStack[] stacks;

    public InventoryBuilder(int size) {
        if(size <= 0) {
            throw new IllegalArgumentException("Size must be greater than 0!");
        }

        this.size = size;
        this.inventory = new IndustriaSimpleInventory(this.size);
    }

    public <T extends Component> InventoryBuilder addComponent(T component) {
        this.components.put(component.getClass(), component);
        return this;
    }

    public InventoryBuilder setStacks(ItemStack... stacks) {
        if (stacks.length > this.size) {
            throw new IllegalArgumentException("Stacks length is greater than the inventory size!");
        }

        this.stacks = stacks;
        return this;
    }

    public InventoryBuilder setStack(int slot, ItemStack stack) {
        if (slot >= this.size) {
            throw new IllegalArgumentException("Slot is greater than the inventory size!");
        }

        if (this.stacks == null) {
            this.stacks = new ItemStack[this.size];
        }

        this.stacks[slot] = stack;
        return this;
    }

    public InventoryBuilder outputOnly() {
        return addComponent(new OutputOnlyInventoryComponent());
    }

    public InventoryBuilder syncing(IndustriaBlockEntity<?> blockEntity) {
        return addComponent(new SyncingComponent(blockEntity));
    }

    public InventoryBuilder recipeInput() {
        return addComponent(new RecipeInputComponent(this.inventory));
    }

    public InventoryBuilder predicate(BiPredicate<Integer, ItemStack> predicate) {
        return addComponent(new StackPredicateComponent(predicate));
    }

    private <T extends Component> void addComponentToInventory(IndustriaSimpleInventory inventory, Class<? extends Component> clazz, Component component) {
        // Safe because we know clazz and component match due to addComponent
        @SuppressWarnings("unchecked")
        Class<T> typedClazz = (Class<T>) clazz;
        @SuppressWarnings("unchecked")
        T typedComponent = (T) component;
        inventory.addComponent(typedClazz, typedComponent);
    }

    public IndustriaSimpleInventory build() {
        var inventory = new IndustriaSimpleInventory(this.size);
        if (this.stacks != null) {
            for (int index = 0; index < this.stacks.length; index++) {
                inventory.setStack(index, this.stacks[index]);
            }
        }

        this.components.forEach((clazz, component) -> addComponentToInventory(inventory, clazz, component));
        return inventory;
    }
}
