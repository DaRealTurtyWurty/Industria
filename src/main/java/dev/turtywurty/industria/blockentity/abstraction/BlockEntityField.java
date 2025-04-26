package dev.turtywurty.industria.blockentity.abstraction;

import org.jetbrains.annotations.Nullable;

public class BlockEntityField<T, B extends IndustriaBlockEntity<?>> {
    private final T defaultValue;
    private final @Nullable FieldGetter<T, B> getter;
    private final @Nullable FieldSetter<T, B> setter;

    public BlockEntityField(T defaultValue, @Nullable FieldGetter<T, B> getter, @Nullable FieldSetter<T, B> setter) {
        this.defaultValue = defaultValue;
        this.getter = getter;
        this.setter = setter;
    }

    public BlockEntityField(T defaultValue) {
        this(defaultValue, null, null);
    }

    public T getDefaultValue() {
        return this.defaultValue;
    }

    public @Nullable FieldGetter<T, B> getGetter() {
        return this.getter;
    }

    public @Nullable FieldSetter<T, B> getSetter() {
        return this.setter;
    }

    public T get(B blockEntity) {
        return this.getter != null ? this.getter.get(blockEntity) : this.defaultValue;
    }

    public void set(B blockEntity, T value) {
        if (this.setter != null) {
            this.setter.set(blockEntity, value);
        }
    }

    @FunctionalInterface
    public interface FieldGetter<T, B extends IndustriaBlockEntity<?>> {
        T get(B blockEntity);
    }

    @FunctionalInterface
    public interface FieldSetter<T, B extends IndustriaBlockEntity<?>> {
        void set(B blockEntity, T value);
    }
}
