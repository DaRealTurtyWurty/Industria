package dev.turtywurty.industria.util;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.SlotDisplay;

import java.util.stream.IntStream;

/**
 * Represents an item stack that can be outputted from a machine.
 *
 * @param item   The item to output.
 * @param count  The count of the item to output.
 * @param chance The chance of the item to output.
 * @see IntProvider
 * @see FloatProvider
 */
public record OutputItemStack(Item item, IntProvider count, FloatProvider chance) {
    private static final ConstantFloat DEFAULT_CHANCE = ConstantFloat.of(1.0F);

    public static final OutputItemStack EMPTY = new OutputItemStack(ItemStack.EMPTY);

    public static final MapCodec<OutputItemStack> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(OutputItemStack::item),
            IntProvider.CODEC.fieldOf("count").forGetter(OutputItemStack::count),
            FloatProvider.CODEC.fieldOf("chance").forGetter(OutputItemStack::chance)
    ).apply(instance, OutputItemStack::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, OutputItemStack> STREAM_CODEC =
            StreamCodec.of(OutputItemStack::encode, OutputItemStack::decode);

    public OutputItemStack {
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null!");
        }

        if (count == null) {
            throw new IllegalArgumentException("Count cannot be null!");
        }

        if (chance == null) {
            throw new IllegalArgumentException("Chance cannot be null!");
        }
    }

    /**
     * Creates a new OutputItemStack with the given item, count, and chance.
     *
     * @param item   The item to output.
     * @param count  The count of the item to output.
     * @param chance The chance of the item to output.
     */
    public OutputItemStack(Item item, int count, float chance) {
        this(item, ConstantInt.of(count), ConstantFloat.of(chance));
    }

    /**
     * Creates a new OutputItemStack with the given itemstack.
     *
     * @param stack The item stack to output.
     * @see ItemStack
     */
    public OutputItemStack(ItemStack stack) {
        this(stack.getItem(), ConstantInt.of(stack.getCount()), DEFAULT_CHANCE);
    }

    /**
     * Creates a new OutputItemStack with the given item and count.
     *
     * @param item   The item to output.
     * @param count  The count of the item to output.
     * @param chance The chance of the item to output.
     * @see IntProvider
     */
    public OutputItemStack(Item item, IntProvider count, float chance) {
        this(item, count, ConstantFloat.of(chance));
    }

    /**
     * Constructs an {@link ItemStack} from this OutputItemStack.
     *
     * @param random The random number generator.
     *               Used to determine the count and chance of the item.
     * @return The constructed ItemStack.
     * @apiNote If the chance of the item is less than the random number generated,
     * then an empty ItemStack will be returned.
     * @see ItemStack
     * @see RandomSource
     */
    public ItemStack createStack(RandomSource random) {
        return this.chance.sample(random) < random.nextFloat() ?
                ItemStack.EMPTY :
                new ItemStack(this.item, this.count.sample(random));
    }

    public SlotDisplay toDisplay() {
        return new SlotDisplay.Composite(
                IntStream.range(this.count.getMinValue(), this.count.getMaxValue() + 1)
                        .mapToObj(count -> new ItemStack(this.item, count))
                        .map(SlotDisplay.ItemStackSlotDisplay::new)
                        .map(SlotDisplay.class::cast)
                        .toList());
    }

    private static void encode(RegistryFriendlyByteBuf buf, OutputItemStack stack) {
        buf.writeResourceKey(BuiltInRegistries.ITEM.getResourceKey(stack.item()).orElseThrow());

        BuiltInRegistries.INT_PROVIDER_TYPE.getResourceKey(stack.count().getType()).ifPresent(buf::writeResourceKey);
        ExtraPacketCodecs.encode(buf, stack.count());

        BuiltInRegistries.FLOAT_PROVIDER_TYPE.getResourceKey(stack.chance().getType()).ifPresent(buf::writeResourceKey);
        ExtraPacketCodecs.encode(buf, stack.chance());
    }

    private static OutputItemStack decode(RegistryFriendlyByteBuf buf) {
        Item item = BuiltInRegistries.ITEM.getValue(buf.readResourceKey(Registries.ITEM));

        ResourceKey<IntProviderType<?>> countType = buf.readResourceKey(Registries.INT_PROVIDER_TYPE);
        IntProviderType<?> countTypeInstance = BuiltInRegistries.INT_PROVIDER_TYPE.getValue(countType);
        IntProvider count = ExtraPacketCodecs.decode(buf, countTypeInstance);

        ResourceKey<FloatProviderType<?>> chanceType = buf.readResourceKey(Registries.FLOAT_PROVIDER_TYPE);
        FloatProviderType<?> chanceTypeInstance = BuiltInRegistries.FLOAT_PROVIDER_TYPE.getValue(chanceType);
        FloatProvider chance = ExtraPacketCodecs.decode(buf, chanceTypeInstance);

        return new OutputItemStack(item, count, chance);
    }
}
