package dev.turtywurty.industria.blockentity.abstraction;

import org.jetbrains.annotations.Nullable;

public record BlockEntityField<T, B extends IndustriaBlockEntity<?>>(T defaultValue, @Nullable FieldGetter<T, B> getter,
                                                                     @Nullable FieldSetter<T, B> setter) {
    public BlockEntityField(T defaultValue, @Nullable FieldGetter<T, B> getter, @Nullable FieldSetter<T, B> setter) {
        this.defaultValue = defaultValue;
        this.getter = getter;
        this.setter = setter;
    }

    public BlockEntityField(T defaultValue) {
        this(defaultValue, null, null);
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
