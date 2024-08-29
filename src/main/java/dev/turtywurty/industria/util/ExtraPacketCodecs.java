package dev.turtywurty.industria.util;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.util.collection.DataPool;
import net.minecraft.util.collection.Weight;
import net.minecraft.util.collection.Weighted;
import net.minecraft.util.math.floatprovider.*;
import net.minecraft.util.math.intprovider.*;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public class ExtraPacketCodecs {
    private static final Map<IntProviderType<?>, PacketCodec<RegistryByteBuf, ? extends IntProvider>> INT_PROVIDER_CODECS = new HashMap<>();
    private static final Map<FloatProviderType<?>, PacketCodec<RegistryByteBuf, ? extends FloatProvider>> FLOAT_PROVIDER_CODECS = new HashMap<>();

    public static <T extends IntProvider> void registerIntProviderCodec(IntProviderType<T> type, PacketCodec<RegistryByteBuf, T> codec) {
        INT_PROVIDER_CODECS.put(type, codec);
    }

    public static <T extends FloatProvider> void registerFloatProviderCodec(FloatProviderType<T> type, PacketCodec<RegistryByteBuf, T> codec) {
        FLOAT_PROVIDER_CODECS.put(type, codec);
    }

    public static PacketCodec<RegistryByteBuf, ? extends IntProvider> getIntProviderCodec(IntProviderType<?> type) {
        return INT_PROVIDER_CODECS.get(type);
    }

    public static PacketCodec<RegistryByteBuf, ? extends FloatProvider> getFloatProviderCodec(FloatProviderType<?> type) {
        return FLOAT_PROVIDER_CODECS.get(type);
    }

    public static <T extends IntProvider> void encode(ByteBuf buf, T intProvider) {
        PacketCodec<RegistryByteBuf, T> codec = (PacketCodec<RegistryByteBuf, T>) getIntProviderCodec(intProvider.getType());
        codec.encode((RegistryByteBuf) buf, intProvider);
    }

    public static <T extends IntProvider> T decode(ByteBuf buf, IntProviderType<T> type) {
        PacketCodec<RegistryByteBuf, T> codec = (PacketCodec<RegistryByteBuf, T>) getIntProviderCodec(type);
        return codec.decode((RegistryByteBuf) buf);
    }

    public static <T extends FloatProvider> void encode(ByteBuf buf, T floatProvider) {
        PacketCodec<RegistryByteBuf, T> codec = (PacketCodec<RegistryByteBuf, T>) getFloatProviderCodec(floatProvider.getType());
        codec.encode((RegistryByteBuf) buf, floatProvider);
    }

    public static <T extends FloatProvider> T decode(ByteBuf buf, FloatProviderType<T> type) {
        PacketCodec<RegistryByteBuf, T> codec = (PacketCodec<RegistryByteBuf, T>) getFloatProviderCodec(type);
        return codec.decode((RegistryByteBuf) buf);
    }

    public static void registerDefaults() {
        registerIntProviderCodec(IntProviderType.CONSTANT, PacketCodec.ofStatic(
                (buf, intProvider) -> buf.writeInt(intProvider.getValue()),
                buf -> ConstantIntProvider.create(buf.readInt())));

        registerIntProviderCodec(IntProviderType.UNIFORM, PacketCodec.ofStatic(
                (buf, intProvider) -> {
                    buf.writeInt(intProvider.getMin());
                    buf.writeInt(intProvider.getMax());
                },
                buf -> UniformIntProvider.create(buf.readInt(), buf.readInt())));

        registerIntProviderCodec(IntProviderType.BIASED_TO_BOTTOM, PacketCodec.ofStatic(
                (buf, intProvider) -> {
                    buf.writeInt(intProvider.getMin());
                    buf.writeInt(intProvider.getMax());
                },
                buf -> BiasedToBottomIntProvider.create(buf.readInt(), buf.readInt())));

        registerIntProviderCodec(IntProviderType.CLAMPED, PacketCodec.ofStatic(
                (buf, intProvider) -> {
                    IntProviderType<?> type = Registries.INT_PROVIDER_TYPE.get(buf.readIdentifier());
                    PacketCodec<RegistryByteBuf, IntProvider> codec = (PacketCodec<RegistryByteBuf, IntProvider>) getIntProviderCodec(type);
                    codec.encode(buf, intProvider.source);
                    buf.writeInt(intProvider.getMin());
                    buf.writeInt(intProvider.getMax());
                },
                buf -> ClampedIntProvider.create(getIntProviderCodec(IntProviderType.CONSTANT).decode(buf), buf.readInt(), buf.readInt())));

        registerIntProviderCodec(IntProviderType.WEIGHTED_LIST, PacketCodec.ofStatic(
                (buf, value) -> {
                    PacketCodec<RegistryByteBuf, ? extends IntProvider> codec = getIntProviderCodec(value.getType());
                    DataPool<IntProvider> entries = value.weightedList;
                    PacketCodec<RegistryByteBuf, DataPool<IntProvider>> entriesCodec = weightedListCodec((PacketCodec<RegistryByteBuf, IntProvider>) codec);
                    entriesCodec.encode(buf, entries);
                },
                buf -> {
                    PacketCodec<RegistryByteBuf, ? extends IntProvider> codec = getIntProviderCodec(IntProviderType.CONSTANT);
                    DataPool<IntProvider> entries = weightedListCodec((PacketCodec<RegistryByteBuf, IntProvider>) codec).decode(buf);
                    return new WeightedListIntProvider(entries);
                }));

        registerIntProviderCodec(IntProviderType.CLAMPED_NORMAL, PacketCodec.ofStatic(
                (buf, intProvider) -> {
                    buf.writeFloat(intProvider.mean);
                    buf.writeFloat(intProvider.deviation);
                    buf.writeInt(intProvider.getMin());
                    buf.writeInt(intProvider.getMax());
                },
                buf -> ClampedNormalIntProvider.of(buf.readFloat(), buf.readFloat(), buf.readInt(), buf.readInt())));

        registerFloatProviderCodec(FloatProviderType.CONSTANT, PacketCodec.ofStatic(
                (buf, floatProvider) -> buf.writeFloat(floatProvider.getValue()),
                buf -> ConstantFloatProvider.create(buf.readFloat())));

        registerFloatProviderCodec(FloatProviderType.UNIFORM, PacketCodec.ofStatic(
                (buf, floatProvider) -> {
                    buf.writeFloat(floatProvider.getMin());
                    buf.writeFloat(floatProvider.getMax());
                },
                buf -> UniformFloatProvider.create(buf.readFloat(), buf.readFloat())));

        registerFloatProviderCodec(FloatProviderType.CLAMPED_NORMAL, PacketCodec.ofStatic(
                (buf, floatProvider) -> {
                    buf.writeFloat(floatProvider.mean);
                    buf.writeFloat(floatProvider.deviation);
                    buf.writeFloat(floatProvider.getMin());
                    buf.writeFloat(floatProvider.getMax());
                },
                buf -> ClampedNormalFloatProvider.create(buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat())));

        registerFloatProviderCodec(FloatProviderType.TRAPEZOID, PacketCodec.ofStatic(
                (buf, floatProvider) -> {
                    buf.writeFloat(floatProvider.getMin());
                    buf.writeFloat(floatProvider.getMax());
                    buf.writeFloat(floatProvider.plateau);
                },
                buf -> TrapezoidFloatProvider.create(buf.readFloat(), buf.readFloat(), buf.readFloat())));
    }

    public static <B extends ByteBuf, E> PacketCodec<B, DataPool<E>> weightedListCodec(PacketCodec<B, E> elementCodec) {
        return PacketCodec.<B, Weighted.Present<E>, E, Weight>tuple(
                elementCodec, Weighted.Present::data,
                PacketCodecs.VAR_INT.xmap(Weight::of, Weight::getValue), Weighted.Present::weight,
                Weighted.Present::new
        ).collect(PacketCodecs.toList()).xmap(DataPool::new, DataPool::getEntries);
    }
}
