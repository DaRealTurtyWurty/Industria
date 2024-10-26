package dev.turtywurty.industria.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.component.ComponentChanges;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.display.SlotDisplay;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public record IndustriaIngredient(RegistryEntryList<Item> entries, StackData stackData) {
    public static final Codec<IndustriaIngredient> CODEC = Codec.lazyInitialized(() -> RecordCodecBuilder.create(
            instance -> instance.group(
                    Ingredient.ENTRIES_CODEC.fieldOf("entries").forGetter(IndustriaIngredient::entries),
                    StackData.CODEC.optionalFieldOf("stack_data", StackData.EMPTY).forGetter(IndustriaIngredient::stackData)
            ).apply(instance, IndustriaIngredient::new)
    ));

    public static final PacketCodec<RegistryByteBuf, IndustriaIngredient> PACKET_CODEC =
            PacketCodec.tuple(PacketCodecs.registryEntryList(RegistryKeys.ITEM), IndustriaIngredient::entries,
                    StackData.PACKET_CODEC, IndustriaIngredient::stackData,
                    IndustriaIngredient::new);

    public static final IndustriaIngredient EMPTY = new IndustriaIngredient(RegistryEntryList.of(), StackData.EMPTY);

    public IndustriaIngredient(RegistryEntryList<Item> entries, int count) {
        this(entries, StackData.create(count));
    }

    @SuppressWarnings("deprecation")
    public IndustriaIngredient(int count, Item... items) {
        this(RegistryEntryList.of(Arrays.stream(items).map(Item::getRegistryEntry).toList()), StackData.create(count));
    }

    public IndustriaIngredient(RegistryEntryList<Item> entries, int count, ComponentChanges components) {
        this(entries, StackData.create(count, components));
    }

    public List<ItemStack> getMatchingStacks() {
        return this.entries.stream().map(item -> new ItemStack(item, this.stackData.count(), this.stackData.components())).toList();
    }

    public boolean testForRecipe(ItemStack stack) {
        return test(stack, countLessThanOrEquals(stack.getCount()));
    }

    public boolean test(ItemStack stack, boolean matchCount, boolean matchComponents) {
        return this.entries.stream().anyMatch(item ->
                stack.getItem() == item &&
                        (!matchCount || stack.getCount() == this.stackData.count()) &&
                        (!matchComponents || this.stackData.components().equals(stack.getComponentChanges())));
    }

    public boolean test(ItemStack stack) {
        return test(stack, true, true);
    }

    public boolean test(ItemStack stack, Predicate<Integer> countPredicate) {
        return this.entries.stream().anyMatch(item ->
                stack.getItem() == item.value() &&
                        countPredicate.test(stackData().count()) &&
                        this.stackData.components().equals(stack.getComponentChanges()));
    }

    public static Predicate<Integer> countEquals(int count) {
        return value -> value == count;
    }

    public static Predicate<Integer> countLessThan(int count) {
        return value -> value < count;
    }

    public static Predicate<Integer> countLessThanOrEquals(int count) {
        return value -> value <= count;
    }

    public static Predicate<Integer> countGreaterThan(int count) {
        return value -> value > count;
    }

    public static Predicate<Integer> countGreaterThanOrEquals(int count) {
        return value -> value >= count;
    }

    public SlotDisplay toDisplay() {
        return new SlotDisplay.CompositeSlotDisplay(
                getMatchingStacks().stream()
                        .map(SlotDisplay.StackSlotDisplay::new)
                        .map(SlotDisplay.class::cast)
                        .toList());
    }

    public record StackData(int count, @NotNull ComponentChanges components) {
        public static final Codec<StackData> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                        Codec.INT.fieldOf("count").orElse(1).forGetter(StackData::count),
                        ComponentChanges.CODEC.optionalFieldOf("components", ComponentChanges.EMPTY).forGetter(StackData::components)
                ).apply(instance, StackData::new)
        );

        public static final PacketCodec<RegistryByteBuf, StackData> PACKET_CODEC =
                PacketCodec.tuple(PacketCodecs.INTEGER, StackData::count,
                        ComponentChanges.PACKET_CODEC, StackData::components,
                        StackData::new);

        public static final StackData EMPTY = new StackData(1, ComponentChanges.EMPTY);

        public static StackData create(int count) {
            return new StackData(count, ComponentChanges.EMPTY);
        }

        public static StackData create(int count, ComponentChanges components) {
            return new StackData(count, components);
        }
    }
}
