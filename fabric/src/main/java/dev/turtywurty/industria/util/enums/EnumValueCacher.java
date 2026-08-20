package dev.turtywurty.industria.util.enums;

public interface EnumValueCacher<T extends Enum<?>> {
    T[] getValues();
}
