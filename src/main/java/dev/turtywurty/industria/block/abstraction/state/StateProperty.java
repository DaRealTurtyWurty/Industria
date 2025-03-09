package dev.turtywurty.industria.block.abstraction.state;

import net.minecraft.state.property.Property;

public final class StateProperty<T extends Comparable<T>> {
    private final Property<T> delegate;
    private T defaultValue;

    public StateProperty(Property<T> delegate, T defaultValue) {
        this.delegate = delegate;
        this.defaultValue = defaultValue;
    }

    public Property<T> delegate() {
        return this.delegate;
    }

    public T defaultValue() {
        return this.defaultValue;
    }

    public void setDefaultValue(T defaultValue) {
        this.defaultValue = defaultValue;
    }
}
