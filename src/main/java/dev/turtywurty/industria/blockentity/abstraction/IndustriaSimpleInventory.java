package dev.turtywurty.industria.blockentity.abstraction;

import dev.turtywurty.industria.blockentity.abstraction.component.*;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;

public class IndustriaSimpleInventory extends SimpleInventory {
    private final ComponentContainer componentContainer = new ComponentContainer();

    public IndustriaSimpleInventory(int size) {
        super(size);
    }

    public IndustriaSimpleInventory(ItemStack... stacks) {
        super(stacks);
    }

    @Override
    public void markDirty() {
        super.markDirty();

        if(this.componentContainer.hasComponent(SyncingComponent.class)) {
            this.componentContainer.getComponent(SyncingComponent.class).sync();
        }
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        boolean valid = true;
        if(this.componentContainer.hasComponent(StackPredicateComponent.class)) {
            valid = this.componentContainer.getComponent(StackPredicateComponent.class).test(slot, stack);
        }

        if(this.componentContainer.hasComponent(OutputOnlyInventoryComponent.class)) {
            valid = false;
        }

        return super.isValid(slot, stack) && valid;
    }

    public <T extends Component> T addComponent(Class<T> clazz, T component) {
        this.componentContainer.addComponent(clazz, component);
        return component;
    }
}