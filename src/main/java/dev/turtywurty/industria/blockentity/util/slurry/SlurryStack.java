package dev.turtywurty.industria.blockentity.util.slurry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.turtywurty.fabricslurryapi.api.SlurryVariant;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

import java.util.Objects;

public record SlurryStack(SlurryVariant variant, long amount) {
    public static final SlurryStack EMPTY = new SlurryStack(SlurryVariant.blank(), 0);

    public static final MapCodec<SlurryStack> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    SlurryVariant.CODEC.fieldOf("variant").forGetter(SlurryStack::variant),
                    Codec.LONG.fieldOf("amount").forGetter(SlurryStack::amount)
            ).apply(instance, SlurryStack::new)
    );

    public static final PacketCodec<RegistryByteBuf, SlurryStack> PACKET_CODEC = PacketCodec.tuple(
            SlurryVariant.PACKET_CODEC, SlurryStack::variant,
            PacketCodecs.LONG, SlurryStack::amount,
            SlurryStack::new
    );

    public boolean isEmpty() {
        return this.amount <= 0 || this.variant.isBlank();
    }

    public boolean matches(SlurryStack other) {
        if(Objects.equals(this, other))
            return true;

        return other == null ? isEmpty() : this.variant.equals(other.variant()) && this.amount >= other.amount();
    }

    public SlurryStack withAmount(long amount) {
        return amount <= 0 ? EMPTY : new SlurryStack(this.variant, amount);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        SlurryStack that = (SlurryStack) obj;
        return amount == that.amount && variant.equals(that.variant);
    }
}
