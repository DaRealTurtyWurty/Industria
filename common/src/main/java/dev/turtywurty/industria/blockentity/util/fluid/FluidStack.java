package dev.turtywurty.industria.blockentity.util.fluid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceTypes;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariantCodecs;
import dev.turtywurty.turtymultiloader.transfer.storage.ResourceStorage;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

public record FluidStack(ResourceVariant<Fluid> variant, long amount) {
    public static final FluidStack EMPTY = new FluidStack(ResourceTypes.FLUID.empty(), 0);

    public static final MapCodec<FluidStack> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ResourceVariantCodecs.FLUID.fieldOf("variant").forGetter(FluidStack::variant),
                    Codec.LONG.fieldOf("amount").forGetter(FluidStack::amount)
            ).apply(instance, FluidStack::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidStack> STREAM_CODEC = StreamCodec.composite(
            ResourceVariantCodecs.FLUID_STREAM, FluidStack::variant,
            ByteBufCodecs.LONG, FluidStack::amount,
            FluidStack::new
    );

    public boolean isEmpty() {
        return this.amount <= 0 || this.variant.isBlank();
    }

    public boolean matches(@Nullable FluidStack other) {
        if (this == EMPTY && other == EMPTY)
            return true;

        return other == null ? isEmpty() : this.variant.equals(other.variant()) && this.amount >= other.amount();
    }

    public FluidStack withAmount(long amount) {
        return amount <= 0 ? EMPTY : new FluidStack(this.variant, amount);
    }

    public boolean testForRecipe(FluidStack other) {
        return this.variant.equals(other.variant()) && this.amount >= other.amount();
    }

    public boolean testForRecipe(ResourceStorage<ResourceVariant<Fluid>> fluidStorage) {
        if (fluidStorage == null)
            return false;

        return fluidStorage.hasStableIndices() && fluidStorage.size() == 1
                && this.variant.equals(fluidStorage.resource(0)) && this.amount <= fluidStorage.amount(0);
    }
}
