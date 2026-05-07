package dev.turtywurty.industria.util;

import com.mojang.serialization.Codec;
import dev.turtywurty.industria.util.enums.IndustriaEnum;
import net.minecraft.network.chat.Component;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Locale;

public enum AgitatorPortType implements IndustriaEnum<AgitatorPortType> {
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
    private final String serializedName = name().toLowerCase(Locale.ROOT);
    private final Component text = Component.literal(switch (this) {
        case ITEM -> "Item";
        case FLUID -> "Fluid";
        case GAS -> "Gas";
        case SLURRY -> "Slurry";
    });

    @Override
    public String getSerializedName() {
        return this.serializedName;
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

    @Override
    public AgitatorPortType next() {
        AgitatorPortType[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    @Override
    public AgitatorPortType previous() {
        AgitatorPortType[] values = values();
        return values[(ordinal() - 1 + values.length) % values.length];
    }

    @Override
    public AgitatorPortType[] getValues() {
        return values();
    }

    @Override
    public Component getAsText() {
        return this.text;
    }
}
