package dev.turtywurty.industria.util.enums;

import com.mojang.serialization.Codec;

import java.util.Objects;

public interface StringRepresentable {
    String getSerializedName();

    static <T extends Enum<?> & StringRepresentable> T getEnumByName(Class<T> enumClass, String serializedName) {
        return getEnumByName(enumClass.getEnumConstants(), serializedName);
    }

    static <T extends Enum<?> & StringRepresentable> T getEnumByName(T[] values, String serializedName) {
        for (T value : values) {
            if (Objects.equals(value.getSerializedName(), serializedName))
                return value;
        }

        return null;
    }

    static <T extends Enum<?> & StringRepresentable> Codec<T> fromEnum(Class<T> enumClass) {
        T[] values = enumClass.getEnumConstants();
        return Codec.STRING.xmap(serializedName -> getEnumByName(values, serializedName),
            StringRepresentable::getSerializedName);
    }
}
