package dev.turtywurty.industria.util;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class WireDrawplateClientCache {
    private static volatile List<Entry> entries = List.of();

    private WireDrawplateClientCache() {
    }

    public static void update(List<Entry> recipes) {
        entries = List.copyOf(recipes);
    }

    public static void clear() {
        entries = List.of();
    }

    public static int findDuration(ItemStack input) {
        if (input.isEmpty())
            return 0;

        for (Entry entry : entries) {
            if (entry.input().testForRecipe(input))
                return entry.duration();
        }

        return 0;
    }

    public record Entry(IndustriaIngredient input, int duration) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Entry> STREAM_CODEC = StreamCodec.composite(
                IndustriaIngredient.STREAM_CODEC, Entry::input,
                ByteBufCodecs.VAR_INT, Entry::duration,
                Entry::new);
    }
}
