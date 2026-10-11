package dev.turtywurty.industria.molding;

import com.mojang.serialization.Codec;
import dev.turtywurty.industria.util.enums.StringRepresentable;

import java.util.Locale;

public enum MoldPiece implements StringRepresentable {
    WHOLE,
    HALF_1,
    HALF_2;

    public static final Codec<MoldPiece> CODEC = StringRepresentable.fromEnum(MoldPiece.class);

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
