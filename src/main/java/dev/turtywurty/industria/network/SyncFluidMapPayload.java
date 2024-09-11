package dev.turtywurty.industria.network;

import com.mojang.serialization.Codec;
import dev.turtywurty.industria.Industria;
import net.minecraft.fluid.FluidState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

import java.util.Map;

public record SyncFluidMapPayload(BlockPos pos, Map<String, FluidState> fluidMap) implements CustomPayload {
    public static final Id<SyncFluidMapPayload> ID = new Id<>(Industria.id("sync_fluid_map"));

    public static final PacketCodec<PacketByteBuf, SyncFluidMapPayload> CODEC =
            PacketCodec.tuple(BlockPos.PACKET_CODEC, SyncFluidMapPayload::pos,
                    PacketCodecs.codec(Codec.unboundedMap(Codec.STRING, FluidState.CODEC)),
                    SyncFluidMapPayload::fluidMap, SyncFluidMapPayload::new);

    public SyncFluidMapPayload {
        if (pos == null) {
            throw new IllegalArgumentException("pos cannot be null");
        }

        if (fluidMap == null) {
            throw new IllegalArgumentException("fluidMap cannot be null");
        }
    }

    @Override
    public Id<SyncFluidMapPayload> getId() {
        return ID;
    }
}
