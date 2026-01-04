package dev.turtywurty.industria.network;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.recipe.UpgradeStationRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.ArrayList;
import java.util.List;

public record UpgradeStationOpenPayload(BlockPos pos, List<UpgradeStationRecipe> recipes) implements HasPositionPayload {
    public static final Type<UpgradeStationOpenPayload> ID = new Type<>(Industria.id("upgrade_station_open"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UpgradeStationOpenPayload> CODEC =
            StreamCodec.composite(BlockPos.STREAM_CODEC, UpgradeStationOpenPayload::pos,
                    ByteBufCodecs.collection(ArrayList::new, UpgradeStationRecipe.Serializer.STREAM_CODEC), UpgradeStationOpenPayload::recipes,
                    UpgradeStationOpenPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
