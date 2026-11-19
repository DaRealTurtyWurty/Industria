package dev.turtywurty.industria.network.mechanical;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.mechanical.ShaftRotation;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record ShaftRotationPayload(ResourceKey<Level> level, long networkId, ShaftRotation rotation)
    implements CustomPacketPayload {
    public static final Type<ShaftRotationPayload> ID = new Type<>(Industria.id("shaft_rotation"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ShaftRotationPayload> CODEC = StreamCodec.composite(
        ResourceKey.streamCodec(Registries.DIMENSION), ShaftRotationPayload::level,
        ByteBufCodecs.VAR_LONG, ShaftRotationPayload::networkId,
        ShaftRotation.STREAM_CODEC, ShaftRotationPayload::rotation,
        ShaftRotationPayload::new);

    @Override
    public Type<ShaftRotationPayload> type() {
        return ID;
    }
}
