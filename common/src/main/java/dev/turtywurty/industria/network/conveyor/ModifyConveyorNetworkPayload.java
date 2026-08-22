package dev.turtywurty.industria.network.conveyor;

import dev.turtywurty.industria.Industria;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.UUID;

public record ModifyConveyorNetworkPayload(Operation operation, ResourceKey<Level> level, UUID networkId, BlockPos pos) implements CustomPacketPayload {
    public static final Type<ModifyConveyorNetworkPayload> ID = new Type<>(Industria.id("modify_conveyor_network"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ModifyConveyorNetworkPayload> CODEC =
            StreamCodec.composite(
                    Operation.STREAM_CODEC, ModifyConveyorNetworkPayload::operation,
                    ResourceKey.streamCodec(Registries.DIMENSION), ModifyConveyorNetworkPayload::level,
                    UUIDUtil.STREAM_CODEC, ModifyConveyorNetworkPayload::networkId,
                    BlockPos.STREAM_CODEC, ModifyConveyorNetworkPayload::pos,
                    ModifyConveyorNetworkPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public enum Operation {
        ADD_CONVEYOR,
        REMOVE_CONVEYOR,
        ADD_CONNECTED_BLOCK,
        REMOVE_CONNECTED_BLOCK;

        public static final StreamCodec<ByteBuf, Operation> STREAM_CODEC =
                ByteBufCodecs.STRING_UTF8.map(Operation::valueOf, Operation::name);
    }
}
