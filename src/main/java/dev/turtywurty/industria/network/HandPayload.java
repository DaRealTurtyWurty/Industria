package dev.turtywurty.industria.network;

import dev.turtywurty.industria.Industria;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Hand;

public record HandPayload(Hand hand) implements CustomPayload {
    public static final CustomPayload.Id<HandPayload> ID = new CustomPayload.Id<>(Industria.id("hand"));
    public static final PacketCodec<RegistryByteBuf, HandPayload> CODEC =
            PacketCodec.tuple(PacketCodecs.STRING,
                    payload -> payload.hand().name(),
                    handStr -> new HandPayload(Hand.valueOf(handStr)));


    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
