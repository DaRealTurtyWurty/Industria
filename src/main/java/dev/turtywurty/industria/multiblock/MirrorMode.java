package dev.turtywurty.industria.multiblock;

import com.mojang.serialization.Codec;

import java.util.Locale;

public enum MirrorMode {
    NONE,
    X,
    Z;

    public static final Codec<MirrorMode> CODEC = Codec.STRING.xmap(
            MirrorMode::fromString,
            MirrorMode::name
    );

    private static MirrorMode fromString(String name) {
        try {
            return MirrorMode.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return NONE;
        }
    }
}
