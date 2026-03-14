package dev.turtywurty.industria.network.conveyor;

import dev.turtywurty.industria.Industria;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SetFilterConveyorMatchEnchantmentsPayload(boolean matchEnchantments) implements CustomPacketPayload {
    public static final Type<SetFilterConveyorMatchEnchantmentsPayload> ID =
            new Type<>(Industria.id("set_filter_conveyor_match_enchantments"));

    public static final StreamCodec<ByteBuf, SetFilterConveyorMatchEnchantmentsPayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, SetFilterConveyorMatchEnchantmentsPayload::matchEnchantments,
                    SetFilterConveyorMatchEnchantmentsPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
