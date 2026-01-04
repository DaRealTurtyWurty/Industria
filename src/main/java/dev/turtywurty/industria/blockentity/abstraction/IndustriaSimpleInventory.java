package dev.turtywurty.industria.blockentity.abstraction;

import dev.turtywurty.industria.blockentity.abstraction.component.*;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

public class IndustriaSimpleInventory extends SimpleContainer {
    private final ComponentContainer componentContainer = new ComponentContainer();

    public IndustriaSimpleInventory(int size) {
        super(size);
    }

    public IndustriaSimpleInventory(ItemStack... stacks) {
        super(stacks);
    }

    @Override
    public void setChanged() {
        super.setChanged();

        if(this.componentContainer.hasComponent(SyncingComponent.class)) {
            this.componentContainer.getComponent(SyncingComponent.class).sync();
        }
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        boolean valid = true;
        if(this.componentContainer.hasComponent(StackPredicateComponent.class)) {
            valid = this.componentContainer.getComponent(StackPredicateComponent.class).test(slot, stack);
        }

        if(this.componentContainer.hasComponent(OutputOnlyInventoryComponent.class)) {
            valid = false;
        }

        return super.canPlaceItem(slot, stack) && valid;
    }

    public <T extends Component> T addComponent(Class<T> clazz, T component) {
        this.componentContainer.addComponent(clazz, component);
        return component;
    }
}