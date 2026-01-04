package dev.turtywurty.industria.network;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.blockentity.BatteryBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record BatteryChargeModePayload(BatteryBlockEntity.ChargeMode chargeMode) implements CustomPacketPayload {
    public static final Type<BatteryChargeModePayload> ID = new Type<>(Industria.id("battery_charge_mode"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BatteryChargeModePayload> CODEC = StreamCodec.composite(
            BatteryBlockEntity.ChargeMode.STREAM_CODEC, BatteryChargeModePayload::chargeMode,
            BatteryChargeModePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
