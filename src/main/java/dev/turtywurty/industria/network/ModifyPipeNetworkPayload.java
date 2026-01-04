package dev.turtywurty.industria.network;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.multiblock.TransferType;
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

public record ModifyPipeNetworkPayload(Operation operation, ResourceKey<Level> world, TransferType<?, ?, ?> transferType,
                                       UUID networkId, BlockPos pos) implements CustomPacketPayload {
    public static final Type<ModifyPipeNetworkPayload> ID = new Type<>(Industria.id("modify_pipe_network"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ModifyPipeNetworkPayload> CODEC =
            StreamCodec.composite(
                    Operation.STREAM_CODEC, ModifyPipeNetworkPayload::operation,
                    ResourceKey.streamCodec(Registries.DIMENSION), ModifyPipeNetworkPayload::world,
                    TransferType.STREAM_CODEC, ModifyPipeNetworkPayload::transferType,
                    UUIDUtil.STREAM_CODEC, ModifyPipeNetworkPayload::networkId,
                    BlockPos.STREAM_CODEC, ModifyPipeNetworkPayload::pos,
                    ModifyPipeNetworkPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public enum Operation {
        ADD_PIPE,
        REMOVE_PIPE,
        ADD_CONNECTED_BLOCK,
        REMOVE_CONNECTED_BLOCK;

        public static final StreamCodec<ByteBuf, Operation> STREAM_CODEC =
                ByteBufCodecs.STRING_UTF8.map(Operation::valueOf, Operation::name);
    }
}
