package dev.turtywurty.industria.network;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.recipe.UpgradeStationRecipe;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public record UpgradeStationOpenPayload(BlockPos pos, List<UpgradeStationRecipe> recipes) implements HasPositionPayload {
    public static final Id<UpgradeStationOpenPayload> ID = new Id<>(Industria.id("upgrade_station_open"));
    public static final PacketCodec<RegistryByteBuf, UpgradeStationOpenPayload> CODEC =
            PacketCodec.tuple(BlockPos.PACKET_CODEC, UpgradeStationOpenPayload::pos,
                    PacketCodecs.collection(ArrayList::new, UpgradeStationRecipe.Serializer.PACKET_CODEC), UpgradeStationOpenPayload::recipes,
                    UpgradeStationOpenPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
