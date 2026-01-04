package dev.turtywurty.industria.blockentity.util.fluid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

public record FluidStack(FluidVariant variant, long amount) {
    public static final FluidStack EMPTY = new FluidStack(FluidVariant.blank(), 0);

    public static final MapCodec<FluidStack> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    FluidVariant.CODEC.fieldOf("variant").forGetter(FluidStack::variant),
                    Codec.LONG.fieldOf("amount").forGetter(FluidStack::amount)
            ).apply(instance, FluidStack::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidStack> STREAM_CODEC = StreamCodec.composite(
            FluidVariant.PACKET_CODEC, FluidStack::variant,
            ByteBufCodecs.LONG, FluidStack::amount,
            FluidStack::new
    );

    public boolean isEmpty() {
        return this.amount <= 0 || this.variant.isBlank();
    }

    public boolean matches(@Nullable FluidStack other) {
        if(this == EMPTY && other == EMPTY)
            return true;

        return other == null ? isEmpty() : this.variant.equals(other.variant()) && this.amount >= other.amount();
    }

    public FluidStack withAmount(long amount) {
        return amount <= 0 ? EMPTY : new FluidStack(this.variant, amount);
    }

    public boolean testForRecipe(FluidStack other) {
        return this.variant.equals(other.variant()) && this.amount >= other.amount();
    }

    public boolean testForRecipe(SingleFluidStorage fluidStorage) {
        if (fluidStorage == null)
            return false;

        return this.variant.equals(fluidStorage.variant) && this.amount <= fluidStorage.amount;
    }
}
