package dev.turtywurty.industria.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.turtywurty.industria.Industria;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public record CountedIngredient(Ingredient ingredient, int count) implements CustomIngredient {
    public static final MapCodec<CountedIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.ALLOW_EMPTY_CODEC.fieldOf("ingredient").forGetter(CountedIngredient::ingredient),
            Codec.INT.fieldOf("count").forGetter(CountedIngredient::count)
    ).apply(instance, CountedIngredient::new));

    public static final PacketCodec<RegistryByteBuf, CountedIngredient> PACKET_CODEC =
            PacketCodec.ofStatic((buf, countedIngredient) -> {
                Ingredient.PACKET_CODEC.encode(buf, countedIngredient.ingredient());
                buf.writeVarInt(countedIngredient.count());
            }, buf -> new CountedIngredient(Ingredient.PACKET_CODEC.decode(buf), buf.readVarInt()));

    public static final Serializer SERIALIZER = new Serializer();
    public static final CountedIngredient EMPTY = new CountedIngredient(Ingredient.EMPTY, 0);

    public static CountedIngredient ofItems(int count, ItemConvertible... items) {
        return new CountedIngredient(Ingredient.ofItems(items), count);
    }

    public static CountedIngredient ofStacks(int count, ItemStack... items) {
        return new CountedIngredient(Ingredient.ofStacks(items), count);
    }

    public static CountedIngredient ofStacks(int count, Stream<ItemStack> items) {
        return new CountedIngredient(Ingredient.ofStacks(items), count);
    }

    public static CountedIngredient fromTag(int count, TagKey<Item> tagKey) {
        return new CountedIngredient(Ingredient.fromTag(tagKey), count);
    }

    @Override
    public boolean test(ItemStack stack) {
        return this.ingredient.test(stack) && stack.getCount() >= this.count;
    }

    @Override
    public List<ItemStack> getMatchingStacks() {
        return List.of(Arrays.stream(this.ingredient.getMatchingStacks()).map(stack -> {
            ItemStack copy = stack.copy();
            copy.setCount(this.count);
            return copy;
        }).toArray(ItemStack[]::new));
    }

    @Override
    public boolean requiresTesting() {
        return false;
    }

    @Override
    public CustomIngredientSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    public interface CountedIngredientRecipe {
        List<CountedIngredient> getCountedIngredients();

        default int getRealSlotIndex(int index) {
            return index;
        }
    }

    public static class Serializer implements CustomIngredientSerializer<CountedIngredient> {
        public static final Identifier ID = Industria.id("counted_ingredient");

        @Override
        public Identifier getIdentifier() {
            return ID;
        }

        @Override
        public MapCodec<CountedIngredient> getCodec(boolean allowEmpty) {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, CountedIngredient> getPacketCodec() {
            return PACKET_CODEC;
        }
    }
}
