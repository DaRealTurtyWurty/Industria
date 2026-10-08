package dev.turtywurty.industria.network;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.molding.MoldTableVisualState;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record UpdateMoldVisualPayload(int containerId, MoldTableVisualState visual) implements CustomPacketPayload {
    public static final Type<UpdateMoldVisualPayload> ID = new Type<>(Industria.id("update_mold_visual"));
    public static final StreamCodec<ByteBuf, UpdateMoldVisualPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, UpdateMoldVisualPayload::containerId,
            ByteBufCodecs.fromCodec(MoldTableVisualState.CODEC), UpdateMoldVisualPayload::visual,
            UpdateMoldVisualPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
