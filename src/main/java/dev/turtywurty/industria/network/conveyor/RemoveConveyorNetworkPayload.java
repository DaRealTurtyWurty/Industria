package dev.turtywurty.industria.network.conveyor;

import dev.turtywurty.industria.Industria;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.UUID;

public record RemoveConveyorNetworkPayload(ResourceKey<Level> level, UUID networkId) implements CustomPacketPayload {
    public static final Type<RemoveConveyorNetworkPayload> ID = new Type<>(Industria.id("remove_conveyor_network"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RemoveConveyorNetworkPayload> CODEC =
            StreamCodec.composite(
                    ResourceKey.streamCodec(Registries.DIMENSION), RemoveConveyorNetworkPayload::level,
                    UUIDUtil.STREAM_CODEC, RemoveConveyorNetworkPayload::networkId,
                    RemoveConveyorNetworkPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
