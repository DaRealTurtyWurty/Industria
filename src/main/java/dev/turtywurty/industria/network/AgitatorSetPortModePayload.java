package dev.turtywurty.industria.network;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.util.AgitatorPortType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record AgitatorSetPortModePayload(boolean output, int index, AgitatorPortType portType) implements CustomPacketPayload {
    public static final Type<AgitatorSetPortModePayload> ID = new Type<>(Industria.id("agitator_set_port_mode"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AgitatorSetPortModePayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, AgitatorSetPortModePayload::output,
            ByteBufCodecs.INT, AgitatorSetPortModePayload::index,
            AgitatorPortType.STREAM_CODEC, AgitatorSetPortModePayload::portType,
            AgitatorSetPortModePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
