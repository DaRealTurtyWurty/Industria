package dev.turtywurty.industria.network;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.recipe.UpgradeStationRecipe;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

import java.util.ArrayList;
import java.util.List;

public record UpgradeStationUpdateRecipesPayload(List<UpgradeStationRecipe> recipes) implements CustomPayload {
    public static final Id<UpgradeStationUpdateRecipesPayload> ID = new Id<>(Industria.id("upgrade_station_update_recipes"));
    public static final PacketCodec<RegistryByteBuf, UpgradeStationUpdateRecipesPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.collection(ArrayList::new, UpgradeStationRecipe.Serializer.PACKET_CODEC), UpgradeStationUpdateRecipesPayload::recipes,
            UpgradeStationUpdateRecipesPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
