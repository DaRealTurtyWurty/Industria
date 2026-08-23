package dev.turtywurty.industria.network.conveyor;

import dev.turtywurty.industria.Industria;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SetConveyorMatchEnchantmentsPayload(boolean matchEnchantments) implements CustomPacketPayload {
    public static final Type<SetConveyorMatchEnchantmentsPayload> ID =
            new Type<>(Industria.id("set_conveyor_match_enchantments"));

    public static final StreamCodec<ByteBuf, SetConveyorMatchEnchantmentsPayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, SetConveyorMatchEnchantmentsPayload::matchEnchantments,
                    SetConveyorMatchEnchantmentsPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
