package dev.turtywurty.industria.network.conveyor;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.conveyor.ConveyorNetwork;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record AddConveyorNetworkPayload(ResourceKey<Level> level,
                                        ConveyorNetwork network) implements CustomPacketPayload {
    public static final Type<AddConveyorNetworkPayload> ID = new Type<>(Industria.id("add_conveyor_network"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AddConveyorNetworkPayload> CODEC =
            StreamCodec.composite(
                    ResourceKey.streamCodec(Registries.DIMENSION), AddConveyorNetworkPayload::level,
                    ConveyorNetwork.STREAM_CODEC, AddConveyorNetworkPayload::network,
                    AddConveyorNetworkPayload::new);

    public AddConveyorNetworkPayload {
        network = network.copy();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
