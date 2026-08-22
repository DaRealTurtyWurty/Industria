package dev.turtywurty.industria.pipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record PipeStorageContent<V extends ResourceVariant<?>>(V variant, long amount) {
    public static <V extends ResourceVariant<?>> Codec<PipeStorageContent<V>> codec(Codec<V> variantCodec) {
        return RecordCodecBuilder.create(instance -> instance.group(
                variantCodec.fieldOf("variant").forGetter(PipeStorageContent::variant),
                Codec.LONG.fieldOf("amount").forGetter(PipeStorageContent::amount)
        ).apply(instance, PipeStorageContent::new));
    }

    public static <V extends ResourceVariant<?>> StreamCodec<RegistryFriendlyByteBuf, PipeStorageContent<V>> streamCodec(
            StreamCodec<? super RegistryFriendlyByteBuf, V> variantCodec) {
        return StreamCodec.composite(
                variantCodec, PipeStorageContent::variant,
                ByteBufCodecs.LONG, PipeStorageContent::amount,
                PipeStorageContent::new);
    }
}
