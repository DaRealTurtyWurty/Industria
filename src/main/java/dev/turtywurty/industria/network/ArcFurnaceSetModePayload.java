package dev.turtywurty.industria.network;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.blockentity.ArcFurnaceBlockEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ArcFurnaceSetModePayload(ArcFurnaceBlockEntity.Mode mode) implements CustomPacketPayload {
    public static final Type<ArcFurnaceSetModePayload> ID = new Type<>(Industria.id("arc_furnace_set_mode"));

    public static final StreamCodec<ByteBuf, ArcFurnaceSetModePayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.map(ArcFurnaceBlockEntity.Mode::valueOf, ArcFurnaceBlockEntity.Mode::name),
            ArcFurnaceSetModePayload::mode,
            ArcFurnaceSetModePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
