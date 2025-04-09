package dev.turtywurty.industria.blockentity.util.gas;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.turtywurty.gasapi.api.Gas;
import dev.turtywurty.gasapi.api.GasVariant;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

import java.util.Objects;

public record GasStack(GasVariant variant, long amount) {
    public GasStack(Gas gas, long amount) {
        this(GasVariant.of(gas), amount);
    }

    public static final GasStack EMPTY = new GasStack(GasVariant.blank(), 0);

    public static final MapCodec<GasStack> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    GasVariant.CODEC.fieldOf("variant").forGetter(GasStack::variant),
                    Codec.LONG.fieldOf("amount").forGetter(GasStack::amount)
            ).apply(instance, GasStack::new)
    );

    public static final PacketCodec<RegistryByteBuf, GasStack> PACKET_CODEC = PacketCodec.tuple(
            GasVariant.PACKET_CODEC, GasStack::variant,
            PacketCodecs.LONG, GasStack::amount,
            GasStack::new
    );

    public boolean isEmpty() {
        return this.amount <= 0 || this.variant.isBlank();
    }

    public boolean matches(GasStack other) {
        if(Objects.equals(this, other))
            return true;

        return other == null ? isEmpty() : this.variant.equals(other.variant()) && this.amount >= other.amount();
    }

    public GasStack withAmount(long amount) {
        return amount <= 0 ? EMPTY : new GasStack(this.variant, amount);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        GasStack that = (GasStack) obj;
        return amount == that.amount && variant.equals(that.variant);
    }
}
