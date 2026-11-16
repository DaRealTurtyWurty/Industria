package dev.turtywurty.industria.network;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.recipe.WireDrawplateRecipe;
import dev.turtywurty.industria.util.WireDrawplateClientCache;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.ArrayList;
import java.util.List;

public record SyncWireDrawplateRecipesPayload(List<WireDrawplateClientCache.Entry> recipes)
    implements CustomPacketPayload {
    public static final Type<SyncWireDrawplateRecipesPayload> ID = new Type<>(
        Industria.id("sync_wire_drawplate_recipes"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncWireDrawplateRecipesPayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.collection(ArrayList::new, WireDrawplateClientCache.Entry.STREAM_CODEC),
        SyncWireDrawplateRecipesPayload::recipes,
        SyncWireDrawplateRecipesPayload::new);

    public SyncWireDrawplateRecipesPayload {
        recipes = List.copyOf(recipes);
    }

    public static SyncWireDrawplateRecipesPayload fromRecipes(RecipeManager recipeManager) {
        return new SyncWireDrawplateRecipesPayload(recipeManager.getRecipes().stream()
            .map(RecipeHolder::value)
            .filter(WireDrawplateRecipe.class::isInstance)
            .map(WireDrawplateRecipe.class::cast)
            .map(recipe -> new WireDrawplateClientCache.Entry(recipe.input(), recipe.duration()))
            .toList());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
