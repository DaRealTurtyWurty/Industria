package dev.turtywurty.industria.network;

import dev.turtywurty.industria.Industria;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ChargingHatchOpenPayload(BlockPos pos, int slotCount) implements HasPositionPayload {
    public static final Type<ChargingHatchOpenPayload> ID = new Type<>(Industria.id("charging_hatch_open"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ChargingHatchOpenPayload> CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, ChargingHatchOpenPayload::pos,
                    ByteBufCodecs.VAR_INT, ChargingHatchOpenPayload::slotCount,
                    ChargingHatchOpenPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
