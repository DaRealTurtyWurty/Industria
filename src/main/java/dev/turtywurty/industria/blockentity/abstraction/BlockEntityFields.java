package dev.turtywurty.industria.blockentity.abstraction;

import java.util.HashMap;
import java.util.Map;

public class BlockEntityFields<B extends IndustriaBlockEntity<B>> {
    private final Map<String, BlockEntityField<?, B>> fields = new HashMap<>();

    public <T> BlockEntityField<T, B> addField(String name, T defaultValue) {
        BlockEntityField<T, B> field = new BlockEntityField<>(defaultValue);
        this.fields.put(name, field);
        return field;
    }

    public <T> BlockEntityField<T, B> addField(String name, T defaultValue, BlockEntityField.FieldGetter<T, B> getter, BlockEntityField.FieldSetter<T, B> setter) {
        BlockEntityField<T, B> field = new BlockEntityField<>(defaultValue, getter, setter);
        this.fields.put(name, field);
        return field;
    }

    public boolean containsField(String name) {
        return this.fields.containsKey(name);
    }

    public <T> BlockEntityField<T, B> getField(String name, Class<T> type) {
        try {
            if (!containsField(name)) {
                throw new IllegalArgumentException("Field with name '" + name + "' does not exist");
            }

            BlockEntityField<?, B> field = this.fields.get(name);
            if (field == null) {
                throw new IllegalArgumentException("Field with name '" + name + "' does not exist");
            }

            if (type.isInstance(field.getDefaultValue())) {
                //noinspection unchecked
                return (BlockEntityField<T, B>) field;
            } else {
                throw new IllegalArgumentException("Field with name '" + name + "' is not of type " + type.getSimpleName());
            }
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Field with name '" + name + "' is not of type " + type.getSimpleName());
        }
    }

    public <T> T getFieldValue(String name, Class<T> type, B blockEntity) {
        return getField(name, type).get(blockEntity);
    }

    public int getFieldValueInt(String name, B blockEntity) {
        return getFieldValue(name, Integer.class, blockEntity);
    }

    public long getFieldValueLong(String name, B blockEntity) {
        return getFieldValue(name, Long.class, blockEntity);
    }

    public float getFieldValueFloat(String name, B blockEntity) {
        return getFieldValue(name, Float.class, blockEntity);
    }

    public double getFieldValueDouble(String name, B blockEntity) {
        return getFieldValue(name, Double.class, blockEntity);
    }

    public boolean getFieldValueBoolean(String name, B blockEntity) {
        return getFieldValue(name, Boolean.class, blockEntity);
    }

    public String getFieldValueString(String name, B blockEntity) {
        return getFieldValue(name, String.class, blockEntity);
    }

    public <T> void setFieldValue(String name, Class<T> type, B blockEntity, T value) {
        getField(name, type).set(blockEntity, value);
    }

    public void setFieldValueInt(String name, B blockEntity, int value) {
        setFieldValue(name, Integer.class, blockEntity, value);
    }

    public void setFieldValueLong(String name, B blockEntity, long value) {
        setFieldValue(name, Long.class, blockEntity, value);
    }

    public void setFieldValueFloat(String name, B blockEntity, float value) {
        setFieldValue(name, Float.class, blockEntity, value);
    }

    public void setFieldValueDouble(String name, B blockEntity, double value) {
        setFieldValue(name, Double.class, blockEntity, value);
    }

    public void setFieldValueBoolean(String name, B blockEntity, boolean value) {
        setFieldValue(name, Boolean.class, blockEntity, value);
    }

    public void setFieldValueString(String name, B blockEntity, String value) {
        setFieldValue(name, String.class, blockEntity, value);
    }
}
