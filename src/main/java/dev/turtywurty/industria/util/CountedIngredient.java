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
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * A custom ingredient that represents an ingredient with a specific count.
 *
 * @param stacks The stacks that make up the ingredient
 * @param count  The count of the ingredient
 * @see CustomIngredient
 */
public record CountedIngredient(List<ItemStack> stacks, int count) implements CustomIngredient {
    private static final PacketCodec<RegistryByteBuf, List<ItemStack>> STACKS_CODEC =
            PacketCodecs.collection(ArrayList::new, ItemStack.PACKET_CODEC);

    public static final MapCodec<CountedIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemStack.OPTIONAL_CODEC.listOf().fieldOf("stack").forGetter(CountedIngredient::stacks),
            Codec.INT.fieldOf("count").forGetter(CountedIngredient::count)
    ).apply(instance, CountedIngredient::new));

    public static final PacketCodec<RegistryByteBuf, CountedIngredient> PACKET_CODEC =
            PacketCodec.ofStatic((buf, countedIngredient) -> {
                STACKS_CODEC.encode(buf, countedIngredient.stacks());
                buf.writeVarInt(countedIngredient.count());
            }, buf -> new CountedIngredient(STACKS_CODEC.decode(buf), buf.readVarInt()));

    public static final Serializer SERIALIZER = new Serializer();
    public static final CountedIngredient EMPTY = new CountedIngredient(
            Util.make(new ArrayList<>(), itemStacks -> itemStacks.add(ItemStack.EMPTY)), 0);

    public static CountedIngredient ofItems(int count, ItemConvertible... items) {
        return new CountedIngredient(Util.make(new ArrayList<>(), itemStacks -> {
            for (ItemConvertible item : items) {
                itemStacks.add(new ItemStack(item));
            }
        }), count);
    }

    public static CountedIngredient fromTag(int count, RegistryEntryList<Item> tagKey) {
        return new CountedIngredient(Util.make(new ArrayList<>(), itemStacks ->
                tagKey.forEach(item -> itemStacks.add(new ItemStack(item)))), count);
    }

    @Override
    public boolean test(ItemStack stack) {
        return this.stacks.stream().anyMatch(itemStack -> ItemStack.areItemsAndComponentsEqual(itemStack, stack));
    }

    @Override
    public List<RegistryEntry<Item>> getMatchingItems() {
        return this.stacks.stream().map(ItemStack::getItem).map(RegistryEntry::of).toList();
    }

//    @Override
//    public List<ItemStack> getMatchingStacks() {
//        return List.of(Arrays.stream(this.ingredient.getMatchingStacks()).map(stack -> {
//            ItemStack copy = stack.copy();
//            copy.setCount(this.count);
//            return copy;
//        }).toArray(ItemStack[]::new));
//    }

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
        public MapCodec<CountedIngredient> getCodec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, CountedIngredient> getPacketCodec() {
            return PACKET_CODEC;
        }
    }
}
