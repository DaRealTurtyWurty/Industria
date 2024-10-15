package dev.turtywurty.industria.util.enums;

public interface IndustriaEnum<T extends Enum<?>> extends TraversableEnum<T>, EnumValueCacher<T>, StringRepresentable, TextEnum {
}
