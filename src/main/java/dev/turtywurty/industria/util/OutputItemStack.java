package dev.turtywurty.industria.util;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.math.floatprovider.ConstantFloatProvider;
import net.minecraft.util.math.floatprovider.FloatProvider;
import net.minecraft.util.math.floatprovider.FloatProviderType;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.intprovider.IntProviderType;
import net.minecraft.util.math.random.Random;

public record OutputItemStack(Item item, IntProvider count, FloatProvider chance) {
    private static final ConstantFloatProvider DEFAULT_CHANCE = ConstantFloatProvider.create(1.0F);

    public static final OutputItemStack EMPTY = new OutputItemStack(ItemStack.EMPTY);

    public static final MapCodec<OutputItemStack> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Registries.ITEM.getCodec().fieldOf("item").forGetter(OutputItemStack::item),
            IntProvider.VALUE_CODEC.fieldOf("count").forGetter(OutputItemStack::count),
            FloatProvider.VALUE_CODEC.fieldOf("chance").forGetter(OutputItemStack::chance)
    ).apply(instance, OutputItemStack::new));

    public static final PacketCodec<RegistryByteBuf, OutputItemStack> PACKET_CODEC =
            PacketCodec.ofStatic(OutputItemStack::encode, OutputItemStack::decode);

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

    public OutputItemStack(Item item, int count, float chance) {
        this(item, ConstantIntProvider.create(count), ConstantFloatProvider.create(chance));
    }

    public OutputItemStack(ItemStack stack) {
        this(stack.getItem(), ConstantIntProvider.create(stack.getCount()), DEFAULT_CHANCE);
    }

    public OutputItemStack(Item item, IntProvider count, float chance) {
        this(item, count, ConstantFloatProvider.create(chance));
    }

    public ItemStack createStack(Random random) {
        return this.chance.get(random) < random.nextFloat() ?
                ItemStack.EMPTY :
                new ItemStack(this.item, this.count.get(random));
    }

    public static void encode(RegistryByteBuf buf, OutputItemStack stack) {
        buf.writeRegistryKey(Registries.ITEM.getKey(stack.item()).orElseThrow());

        Registries.INT_PROVIDER_TYPE.getKey(stack.count().getType()).ifPresent(buf::writeRegistryKey);
        ExtraPacketCodecs.encode(buf, stack.count());

        Registries.FLOAT_PROVIDER_TYPE.getKey(stack.chance().getType()).ifPresent(buf::writeRegistryKey);
        ExtraPacketCodecs.encode(buf, stack.chance());
    }

    public static OutputItemStack decode(RegistryByteBuf buf) {
        Item item = Registries.ITEM.get(buf.readRegistryKey(RegistryKeys.ITEM));

        RegistryKey<IntProviderType<?>> countType = buf.readRegistryKey(RegistryKeys.INT_PROVIDER_TYPE);
        IntProviderType<?> countTypeInstance = Registries.INT_PROVIDER_TYPE.get(countType);
        IntProvider count = ExtraPacketCodecs.decode(buf, countTypeInstance);

        RegistryKey<FloatProviderType<?>> chanceType = buf.readRegistryKey(RegistryKeys.FLOAT_PROVIDER_TYPE);
        FloatProviderType<?> chanceTypeInstance = Registries.FLOAT_PROVIDER_TYPE.get(chanceType);
        FloatProvider chance = ExtraPacketCodecs.decode(buf, chanceTypeInstance);

        return new OutputItemStack(item, count, chance);
    }
}
