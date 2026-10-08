package dev.turtywurty.industria.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.turtywurty.industria.molding.MoldPiece;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public record MoldComponent(Identifier moldId, MoldPiece piece) {
    public static final Codec<MoldComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("mold_id").forGetter(MoldComponent::moldId),
            MoldPiece.CODEC.fieldOf("piece").forGetter(MoldComponent::piece)
    ).apply(instance, MoldComponent::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MoldComponent> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, MoldComponent::moldId,
            ByteBufCodecs.fromCodec(MoldPiece.CODEC), MoldComponent::piece,
            MoldComponent::new
    );
}
