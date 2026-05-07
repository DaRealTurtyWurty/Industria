package dev.turtywurty.industria.util;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Locale;

public enum AgitatorPortType {
    ITEM,
    FLUID,
    GAS,
    SLURRY;

    public static final Codec<AgitatorPortType> CODEC = Codec.STRING.xmap(
            AgitatorPortType::fromName,
            AgitatorPortType::getSerializedName
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, AgitatorPortType> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            AgitatorPortType::ordinal,
            AgitatorPortType::fromOrdinal
    );

    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static AgitatorPortType fromName(String name) {
        for (AgitatorPortType value : values()) {
            if (value.getSerializedName().equals(name))
                return value;
        }

        return ITEM;
    }

    public static AgitatorPortType fromOrdinal(int ordinal) {
        AgitatorPortType[] values = values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : ITEM;
    }
}
