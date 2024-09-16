package dev.turtywurty.industria.network;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.persistent.WorldFluidPocketsState;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

import java.util.ArrayList;
import java.util.List;

public record SyncFluidPocketsPayload(List<WorldFluidPocketsState.FluidPocket> fluidPockets) implements CustomPayload {
    public static final Id<SyncFluidPocketsPayload> ID = new Id<>(Industria.id("sync_fluid_pockets"));

    public static final PacketCodec<RegistryByteBuf, SyncFluidPocketsPayload> CODEC =
            PacketCodec.tuple(PacketCodecs.collection(ArrayList::new, WorldFluidPocketsState.FluidPocket.PACKET_CODEC),
                    SyncFluidPocketsPayload::fluidPockets,
                    SyncFluidPocketsPayload::new);

    public SyncFluidPocketsPayload {
        if (fluidPockets == null) {
            throw new IllegalArgumentException("Fluid pockets cannot be null!");
        }
    }

    @Override
    public Id<SyncFluidPocketsPayload> getId() {
        return ID;
    }
}
