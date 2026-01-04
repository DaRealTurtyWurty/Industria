package dev.turtywurty.industria.network;

import dev.turtywurty.industria.Industria;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;

public record HandPayload(InteractionHand hand) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<HandPayload> ID = new CustomPacketPayload.Type<>(Industria.id("hand"));
    public static final StreamCodec<RegistryFriendlyByteBuf, HandPayload> CODEC =
            StreamCodec.composite(ByteBufCodecs.STRING_UTF8,
                    payload -> payload.hand().name(),
                    handStr -> new HandPayload(InteractionHand.valueOf(handStr)));


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
