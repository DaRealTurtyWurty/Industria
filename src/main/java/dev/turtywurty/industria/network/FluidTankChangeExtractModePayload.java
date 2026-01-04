package dev.turtywurty.industria.network;

import dev.turtywurty.industria.Industria;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record FluidTankChangeExtractModePayload(boolean extractMode) implements CustomPacketPayload {
    public static final Type<FluidTankChangeExtractModePayload> ID = new Type<>(Industria.id("fluid_tank_change_extract_mode"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidTankChangeExtractModePayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, FluidTankChangeExtractModePayload::extractMode, FluidTankChangeExtractModePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
