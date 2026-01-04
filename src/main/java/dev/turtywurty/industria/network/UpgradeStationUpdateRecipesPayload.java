package dev.turtywurty.industria.network;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.recipe.UpgradeStationRecipe;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.ArrayList;
import java.util.List;

public record UpgradeStationUpdateRecipesPayload(List<UpgradeStationRecipe> recipes) implements CustomPacketPayload {
    public static final Type<UpgradeStationUpdateRecipesPayload> ID = new Type<>(Industria.id("upgrade_station_update_recipes"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UpgradeStationUpdateRecipesPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(ArrayList::new, UpgradeStationRecipe.Serializer.STREAM_CODEC), UpgradeStationUpdateRecipesPayload::recipes,
            UpgradeStationUpdateRecipesPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
