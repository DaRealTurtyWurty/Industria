package dev.turtywurty.industria.network;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.blockentity.BatteryBlockEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record BatteryChargeModePayload(BatteryBlockEntity.ChargeMode chargeMode) implements CustomPayload {
    public static final Id<BatteryChargeModePayload> ID = new Id<>(Industria.id("battery_charge_mode"));
    public static final PacketCodec<RegistryByteBuf, BatteryChargeModePayload> CODEC = PacketCodec.tuple(
            BatteryBlockEntity.ChargeMode.PACKET_CODEC, BatteryChargeModePayload::chargeMode,
            BatteryChargeModePayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
