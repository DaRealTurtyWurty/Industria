package dev.turtywurty.industria.util.enums;

public interface TraversableEnum<T extends Enum<?>> {
    T next();

    T previous();
}
