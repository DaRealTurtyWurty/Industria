package dev.turtywurty.industria.network;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.persistent.WorldFluidPocketsState;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.ArrayList;
import java.util.List;

public record SyncFluidPocketsPayload(List<WorldFluidPocketsState.FluidPocket> fluidPockets) implements CustomPacketPayload {
    public static final Type<SyncFluidPocketsPayload> ID = new Type<>(Industria.id("sync_fluid_pockets"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncFluidPocketsPayload> CODEC =
            StreamCodec.composite(ByteBufCodecs.collection(ArrayList::new, WorldFluidPocketsState.FluidPocket.STREAM_CODEC),
                    SyncFluidPocketsPayload::fluidPockets,
                    SyncFluidPocketsPayload::new);

    public SyncFluidPocketsPayload {
        if (fluidPockets == null) {
            throw new IllegalArgumentException("Fluid pockets cannot be null!");
        }
    }

    @Override
    public Type<SyncFluidPocketsPayload> type() {
        return ID;
    }
}
