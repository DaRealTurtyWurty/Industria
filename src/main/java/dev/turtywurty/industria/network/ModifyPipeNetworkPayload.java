package dev.turtywurty.industria.network;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.multiblock.TransferType;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.UUID;

public record ModifyPipeNetworkPayload(Operation operation, RegistryKey<World> world, TransferType<?, ?, ?> transferType,
                                       UUID networkId, BlockPos pos) implements CustomPayload {
    public static final Id<ModifyPipeNetworkPayload> ID = new Id<>(Industria.id("modify_pipe_network"));
    public static final PacketCodec<RegistryByteBuf, ModifyPipeNetworkPayload> CODEC =
            PacketCodec.tuple(
                    Operation.PACKET_CODEC, ModifyPipeNetworkPayload::operation,
                    RegistryKey.createPacketCodec(RegistryKeys.WORLD), ModifyPipeNetworkPayload::world,
                    TransferType.PACKET_CODEC, ModifyPipeNetworkPayload::transferType,
                    Uuids.PACKET_CODEC, ModifyPipeNetworkPayload::networkId,
                    BlockPos.PACKET_CODEC, ModifyPipeNetworkPayload::pos,
                    ModifyPipeNetworkPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public enum Operation {
        ADD_PIPE,
        REMOVE_PIPE,
        ADD_CONNECTED_BLOCK,
        REMOVE_CONNECTED_BLOCK;

        public static final PacketCodec<ByteBuf, Operation> PACKET_CODEC =
                PacketCodecs.STRING.xmap(Operation::valueOf, Operation::name);
    }
}
