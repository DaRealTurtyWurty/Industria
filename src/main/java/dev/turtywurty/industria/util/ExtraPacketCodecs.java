package dev.turtywurty.industria.util;

import com.mojang.datafixers.util.*;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.util.collection.Pool;
import net.minecraft.util.collection.Weighted;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.floatprovider.*;
import net.minecraft.util.math.intprovider.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

@SuppressWarnings("unchecked")
public class ExtraPacketCodecs {
    public static final PacketCodec<ByteBuf, Set<BlockPos>> BLOCK_POS_SET_PACKET_CODEC = setOf(BlockPos.PACKET_CODEC);
    public static final PacketCodec<ByteBuf, BlockPos> BLOCK_POS_STRING_CODEC = PacketCodecs.codec(ExtraCodecs.BLOCK_POS_STRING_CODEC);
    private static final Map<IntProviderType<?>, PacketCodec<RegistryByteBuf, ? extends IntProvider>> INT_PROVIDER_CODECS = new HashMap<>();
    private static final Map<FloatProviderType<?>, PacketCodec<RegistryByteBuf, ? extends FloatProvider>> FLOAT_PROVIDER_CODECS = new HashMap<>();

    public static <B extends ByteBuf, V> PacketCodec<B, Set<V>> setOf(PacketCodec<? super B, V> codec) {
        return PacketCodecs.collection(HashSet::new, codec);
    }

    /**
     * Registers a codec for an {@link IntProviderType}.
     *
     * @param type  The type to register the codec for.
     * @param codec The codec to register.
     * @param <T>   The type of the {@link IntProvider}.
     */
    public static <T extends IntProvider> void registerIntProviderCodec(IntProviderType<T> type, PacketCodec<RegistryByteBuf, T> codec) {
        INT_PROVIDER_CODECS.put(type, codec);
    }

    /**
     * Registers a codec for a {@link FloatProviderType}.
     *
     * @param type  The type to register the codec for.
     * @param codec The codec to register.
     * @param <T>   The type of the {@link FloatProvider}.
     */
    public static <T extends FloatProvider> void registerFloatProviderCodec(FloatProviderType<T> type, PacketCodec<RegistryByteBuf, T> codec) {
        FLOAT_PROVIDER_CODECS.put(type, codec);
    }

    /**
     * Gets the codec for an {@link IntProviderType}.
     *
     * @param type The type to get the codec for.
     * @return The codec for the {@link IntProviderType}.
     * @see IntProviderType
     */
    public static PacketCodec<RegistryByteBuf, ? extends IntProvider> getIntProviderCodec(IntProviderType<?> type) {
        return INT_PROVIDER_CODECS.get(type);
    }

    /**
     * Gets the codec for a {@link FloatProviderType}.
     *
     * @param type The type to get the codec for.
     * @return The codec for the {@link FloatProviderType}.
     * @see FloatProviderType
     */
    public static PacketCodec<RegistryByteBuf, ? extends FloatProvider> getFloatProviderCodec(FloatProviderType<?> type) {
        return FLOAT_PROVIDER_CODECS.get(type);
    }

    /**
     * Encodes an {@link IntProvider} into a {@link ByteBuf}.
     *
     * @param buf         The {@link ByteBuf} to encode the {@link IntProvider} into.
     * @param intProvider The {@link IntProvider} to encode.
     * @param <T>         The type of the {@link IntProvider}.
     */
    public static <T extends IntProvider> void encode(ByteBuf buf, T intProvider) {
        PacketCodec<RegistryByteBuf, T> codec = (PacketCodec<RegistryByteBuf, T>) getIntProviderCodec(intProvider.getType());
        codec.encode((RegistryByteBuf) buf, intProvider);
    }

    /**
     * Decodes an {@link IntProvider} from a {@link ByteBuf}.
     *
     * @param buf  The {@link ByteBuf} to decode the {@link IntProvider} from.
     * @param type The type of the {@link IntProvider}.
     * @param <T>  The type of the {@link IntProvider}.
     * @return The decoded {@link IntProvider}.
     */
    public static <T extends IntProvider> T decode(ByteBuf buf, IntProviderType<T> type) {
        PacketCodec<RegistryByteBuf, T> codec = (PacketCodec<RegistryByteBuf, T>) getIntProviderCodec(type);
        return codec.decode((RegistryByteBuf) buf);
    }

    /**
     * Encodes a {@link FloatProvider} into a {@link ByteBuf}.
     *
     * @param buf           The {@link ByteBuf} to encode the {@link FloatProvider} into.
     * @param floatProvider The {@link FloatProvider} to encode.
     * @param <T>           The type of the {@link FloatProvider}.
     */
    public static <T extends FloatProvider> void encode(ByteBuf buf, T floatProvider) {
        PacketCodec<RegistryByteBuf, T> codec = (PacketCodec<RegistryByteBuf, T>) getFloatProviderCodec(floatProvider.getType());
        codec.encode((RegistryByteBuf) buf, floatProvider);
    }

    /**
     * Decodes a {@link FloatProvider} from a {@link ByteBuf}.
     *
     * @param buf  The {@link ByteBuf} to decode the {@link FloatProvider} from.
     * @param type The type of the {@link FloatProvider}.
     * @param <T>  The type of the {@link FloatProvider}.
     * @return The decoded {@link FloatProvider}.
     */
    public static <T extends FloatProvider> T decode(ByteBuf buf, FloatProviderType<T> type) {
        PacketCodec<RegistryByteBuf, T> codec = (PacketCodec<RegistryByteBuf, T>) getFloatProviderCodec(type);
        return codec.decode((RegistryByteBuf) buf);
    }

    public static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9> PacketCodec<B, C> tuple(
            PacketCodec<? super B, T1> codec1,
            Function<C, T1> from1,
            PacketCodec<? super B, T2> codec2,
            Function<C, T2> from2,
            PacketCodec<? super B, T3> codec3,
            Function<C, T3> from3,
            PacketCodec<? super B, T4> codec4,
            Function<C, T4> from4,
            PacketCodec<? super B, T5> codec5,
            Function<C, T5> from5,
            PacketCodec<? super B, T6> codec6,
            Function<C, T6> from6,
            PacketCodec<? super B, T7> codec7,
            Function<C, T7> from7,
            PacketCodec<? super B, T8> codec8,
            Function<C, T8> from8,
            PacketCodec<? super B, T9> codec9,
            Function<C, T9> from9,
            Function9<T1, T2, T3, T4, T5, T6, T7, T8, T9, C> to
    ) {
        return new PacketCodec<>() {
            @Override
            public C decode(B object) {
                T1 object2 = codec1.decode(object);
                T2 object3 = codec2.decode(object);
                T3 object4 = codec3.decode(object);
                T4 object5 = codec4.decode(object);
                T5 object6 = codec5.decode(object);
                T6 object7 = codec6.decode(object);
                T7 object8 = codec7.decode(object);
                T8 object9 = codec8.decode(object);
                T9 object10 = codec9.decode(object);
                return to.apply(object2, object3, object4, object5, object6, object7, object8, object9, object10);
            }

            @Override
            public void encode(B object, C object2) {
                codec1.encode(object, from1.apply(object2));
                codec2.encode(object, from2.apply(object2));
                codec3.encode(object, from3.apply(object2));
                codec4.encode(object, from4.apply(object2));
                codec5.encode(object, from5.apply(object2));
                codec6.encode(object, from6.apply(object2));
                codec7.encode(object, from7.apply(object2));
                codec8.encode(object, from8.apply(object2));
                codec9.encode(object, from9.apply(object2));
            }
        };
    }

    public static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> PacketCodec<B, C> tuple(
            PacketCodec<? super B, T1> codec1,
            Function<C, T1> from1,
            PacketCodec<? super B, T2> codec2,
            Function<C, T2> from2,
            PacketCodec<? super B, T3> codec3,
            Function<C, T3> from3,
            PacketCodec<? super B, T4> codec4,
            Function<C, T4> from4,
            PacketCodec<? super B, T5> codec5,
            Function<C, T5> from5,
            PacketCodec<? super B, T6> codec6,
            Function<C, T6> from6,
            PacketCodec<? super B, T7> codec7,
            Function<C, T7> from7,
            PacketCodec<? super B, T8> codec8,
            Function<C, T8> from8,
            PacketCodec<? super B, T9> codec9,
            Function<C, T9> from9,
            PacketCodec<? super B, T10> codec10,
            Function<C, T10> from10,
            Function10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, C> to
    ) {
        return new PacketCodec<>() {
            @Override
            public C decode(B object) {
                T1 object2 = codec1.decode(object);
                T2 object3 = codec2.decode(object);
                T3 object4 = codec3.decode(object);
                T4 object5 = codec4.decode(object);
                T5 object6 = codec5.decode(object);
                T6 object7 = codec6.decode(object);
                T7 object8 = codec7.decode(object);
                T8 object9 = codec8.decode(object);
                T9 object10 = codec9.decode(object);
                T10 object11 = codec10.decode(object);
                return to.apply(object2, object3, object4, object5, object6, object7, object8, object9, object10, object11);
            }

            @Override
            public void encode(B object, C object2) {
                codec1.encode(object, from1.apply(object2));
                codec2.encode(object, from2.apply(object2));
                codec3.encode(object, from3.apply(object2));
                codec4.encode(object, from4.apply(object2));
                codec5.encode(object, from5.apply(object2));
                codec6.encode(object, from6.apply(object2));
                codec7.encode(object, from7.apply(object2));
                codec8.encode(object, from8.apply(object2));
                codec9.encode(object, from9.apply(object2));
                codec10.encode(object, from10.apply(object2));
            }
        };
    }

    public static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> PacketCodec<B, C> tuple(
            PacketCodec<? super B, T1> codec1,
            Function<C, T1> from1,
            PacketCodec<? super B, T2> codec2,
            Function<C, T2> from2,
            PacketCodec<? super B, T3> codec3,
            Function<C, T3> from3,
            PacketCodec<? super B, T4> codec4,
            Function<C, T4> from4,
            PacketCodec<? super B, T5> codec5,
            Function<C, T5> from5,
            PacketCodec<? super B, T6> codec6,
            Function<C, T6> from6,
            PacketCodec<? super B, T7> codec7,
            Function<C, T7> from7,
            PacketCodec<? super B, T8> codec8,
            Function<C, T8> from8,
            PacketCodec<? super B, T9> codec9,
            Function<C, T9> from9,
            PacketCodec<? super B, T10> codec10,
            Function<C, T10> from10,
            PacketCodec<? super B, T11> codec11,
            Function<C, T11> from11,
            Function11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, C> to
    ) {
        return new PacketCodec<>() {
            @Override
            public C decode(B object) {
                T1 object2 = codec1.decode(object);
                T2 object3 = codec2.decode(object);
                T3 object4 = codec3.decode(object);
                T4 object5 = codec4.decode(object);
                T5 object6 = codec5.decode(object);
                T6 object7 = codec6.decode(object);
                T7 object8 = codec7.decode(object);
                T8 object9 = codec8.decode(object);
                T9 object10 = codec9.decode(object);
                T10 object11 = codec10.decode(object);
                T11 object12 = codec11.decode(object);
                return to.apply(object2, object3, object4, object5, object6,
                        object7, object8, object9, object10, object11, object12);
            }

            @Override
            public void encode(B object, C object2) {
                codec1.encode(object, from1.apply(object2));
                codec2.encode(object, from2.apply(object2));
                codec3.encode(object, from3.apply(object2));
                codec4.encode(object, from4.apply(object2));
                codec5.encode(object, from5.apply(object2));
                codec6.encode(object, from6.apply(object2));
                codec7.encode(object, from7.apply(object2));
                codec8.encode(object, from8.apply(object2));
                codec9.encode(object, from9.apply(object2));
                codec10.encode(object, from10.apply(object2));
                codec11.encode(object, from11.apply(object2));
            }
        };
    }

    public static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> PacketCodec<B, C> tuple(
            PacketCodec<? super B, T1> codec1, Function<C, T1> from1,
            PacketCodec<? super B, T2> codec2, Function<C, T2> from2,
            PacketCodec<? super B, T3> codec3, Function<C, T3> from3,
            PacketCodec<? super B, T4> codec4, Function<C, T4> from4,
            PacketCodec<? super B, T5> codec5, Function<C, T5> from5,
            PacketCodec<? super B, T6> codec6, Function<C, T6> from6,
            PacketCodec<? super B, T7> codec7, Function<C, T7> from7,
            PacketCodec<? super B, T8> codec8, Function<C, T8> from8,
            PacketCodec<? super B, T9> codec9, Function<C, T9> from9,
            PacketCodec<? super B, T10> codec10, Function<C, T10> from10,
            PacketCodec<? super B, T11> codec11, Function<C, T11> from11,
            PacketCodec<? super B, T12> codec12, Function<C, T12> from12,
            Function12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, C> to
    ) {
        return new PacketCodec<>() {
            @Override
            public C decode(B object) {
                T1 object2 = codec1.decode(object);
                T2 object3 = codec2.decode(object);
                T3 object4 = codec3.decode(object);
                T4 object5 = codec4.decode(object);
                T5 object6 = codec5.decode(object);
                T6 object7 = codec6.decode(object);
                T7 object8 = codec7.decode(object);
                T8 object9 = codec8.decode(object);
                T9 object10 = codec9.decode(object);
                T10 object11 = codec10.decode(object);
                T11 object12 = codec11.decode(object);
                T12 object13 = codec12.decode(object);
                return to.apply(object2, object3, object4, object5, object6,
                        object7, object8, object9, object10, object11, object12, object13);
            }

            @Override
            public void encode(B object, C object2) {
                codec1.encode(object, from1.apply(object2));
                codec2.encode(object, from2.apply(object2));
                codec3.encode(object, from3.apply(object2));
                codec4.encode(object, from4.apply(object2));
                codec5.encode(object, from5.apply(object2));
                codec6.encode(object, from6.apply(object2));
                codec7.encode(object, from7.apply(object2));
                codec8.encode(object, from8.apply(object2));
                codec9.encode(object, from9.apply(object2));
                codec10.encode(object, from10.apply(object2));
                codec11.encode(object, from11.apply(object2));
                codec12.encode(object, from12.apply(object2));
            }
        };
    }

    public static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> PacketCodec<B, C> tuple(
            PacketCodec<? super B, T1> codec1, Function<C, T1> from1,
            PacketCodec<? super B, T2> codec2, Function<C, T2> from2,
            PacketCodec<? super B, T3> codec3, Function<C, T3> from3,
            PacketCodec<? super B, T4> codec4, Function<C, T4> from4,
            PacketCodec<? super B, T5> codec5, Function<C, T5> from5,
            PacketCodec<? super B, T6> codec6, Function<C, T6> from6,
            PacketCodec<? super B, T7> codec7, Function<C, T7> from7,
            PacketCodec<? super B, T8> codec8, Function<C, T8> from8,
            PacketCodec<? super B, T9> codec9, Function<C, T9> from9,
            PacketCodec<? super B, T10> codec10, Function<C, T10> from10,
            PacketCodec<? super B, T11> codec11, Function<C, T11> from11,
            PacketCodec<? super B, T12> codec12, Function<C, T12> from12,
            PacketCodec<? super B, T13> codec13, Function<C, T13> from13,
            Function13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, C> to
    ) {
        return new PacketCodec<>() {
            @Override
            public C decode(B object) {
                T1 object2 = codec1.decode(object);
                T2 object3 = codec2.decode(object);
                T3 object4 = codec3.decode(object);
                T4 object5 = codec4.decode(object);
                T5 object6 = codec5.decode(object);
                T6 object7 = codec6.decode(object);
                T7 object8 = codec7.decode(object);
                T8 object9 = codec8.decode(object);
                T9 object10 = codec9.decode(object);
                T10 object11 = codec10.decode(object);
                T11 object12 = codec11.decode(object);
                T12 object13 = codec12.decode(object);
                T13 object14 = codec13.decode(object);
                return to.apply(object2, object3, object4, object5, object6,
                        object7, object8, object9, object10, object11, object12, object13, object14);
            }

            @Override
            public void encode(B object, C object2) {
                codec1.encode(object, from1.apply(object2));
                codec2.encode(object, from2.apply(object2));
                codec3.encode(object, from3.apply(object2));
                codec4.encode(object, from4.apply(object2));
                codec5.encode(object, from5.apply(object2));
                codec6.encode(object, from6.apply(object2));
                codec7.encode(object, from7.apply(object2));
                codec8.encode(object, from8.apply(object2));
                codec9.encode(object, from9.apply(object2));
                codec10.encode(object, from10.apply(object2));
                codec11.encode(object, from11.apply(object2));
                codec12.encode(object, from12.apply(object2));
                codec13.encode(object, from13.apply(object2));
            }
        };
    }

    public static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> PacketCodec<B, C> tuple(
            PacketCodec<? super B, T1> codec1, Function<C, T1> from1,
            PacketCodec<? super B, T2> codec2, Function<C, T2> from2,
            PacketCodec<? super B, T3> codec3, Function<C, T3> from3,
            PacketCodec<? super B, T4> codec4, Function<C, T4> from4,
            PacketCodec<? super B, T5> codec5, Function<C, T5> from5,
            PacketCodec<? super B, T6> codec6, Function<C, T6> from6,
            PacketCodec<? super B, T7> codec7, Function<C, T7> from7,
            PacketCodec<? super B, T8> codec8, Function<C, T8> from8,
            PacketCodec<? super B, T9> codec9, Function<C, T9> from9,
            PacketCodec<? super B, T10> codec10, Function<C, T10> from10,
            PacketCodec<? super B, T11> codec11, Function<C, T11> from11,
            PacketCodec<? super B, T12> codec12, Function<C, T12> from12,
            PacketCodec<? super B, T13> codec13, Function<C, T13> from13,
            PacketCodec<? super B, T14> codec14, Function<C, T14> from14,
            Function14<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, C> to
    ) {
        return new PacketCodec<>() {
            @Override
            public C decode(B object) {
                T1 object2 = codec1.decode(object);
                T2 object3 = codec2.decode(object);
                T3 object4 = codec3.decode(object);
                T4 object5 = codec4.decode(object);
                T5 object6 = codec5.decode(object);
                T6 object7 = codec6.decode(object);
                T7 object8 = codec7.decode(object);
                T8 object9 = codec8.decode(object);
                T9 object10 = codec9.decode(object);
                T10 object11 = codec10.decode(object);
                T11 object12 = codec11.decode(object);
                T12 object13 = codec12.decode(object);
                T13 object14 = codec13.decode(object);
                T14 object15 = codec14.decode(object);
                return to.apply(object2, object3, object4, object5, object6,
                        object7, object8, object9, object10, object11, object12, object13, object14, object15);
            }

            @Override
            public void encode(B object, C object2) {
                codec1.encode(object, from1.apply(object2));
                codec2.encode(object, from2.apply(object2));
                codec3.encode(object, from3.apply(object2));
                codec4.encode(object, from4.apply(object2));
                codec5.encode(object, from5.apply(object2));
                codec6.encode(object, from6.apply(object2));
                codec7.encode(object, from7.apply(object2));
                codec8.encode(object, from8.apply(object2));
                codec9.encode(object, from9.apply(object2));
                codec10.encode(object, from10.apply(object2));
                codec11.encode(object, from11.apply(object2));
                codec12.encode(object, from12.apply(object2));
                codec13.encode(object, from13.apply(object2));
                codec14.encode(object, from14.apply(object2));
            }
        };
    }

    public static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> PacketCodec<B, C> tuple(
            PacketCodec<? super B, T1> codec1, Function<C, T1> from1,
            PacketCodec<? super B, T2> codec2, Function<C, T2> from2,
            PacketCodec<? super B, T3> codec3, Function<C, T3> from3,
            PacketCodec<? super B, T4> codec4, Function<C, T4> from4,
            PacketCodec<? super B, T5> codec5, Function<C, T5> from5,
            PacketCodec<? super B, T6> codec6, Function<C, T6> from6,
            PacketCodec<? super B, T7> codec7, Function<C, T7> from7,
            PacketCodec<? super B, T8> codec8, Function<C, T8> from8,
            PacketCodec<? super B, T9> codec9, Function<C, T9> from9,
            PacketCodec<? super B, T10> codec10, Function<C, T10> from10,
            PacketCodec<? super B, T11> codec11, Function<C, T11> from11,
            PacketCodec<? super B, T12> codec12, Function<C, T12> from12,
            PacketCodec<? super B, T13> codec13, Function<C, T13> from13,
            PacketCodec<? super B, T14> codec14, Function<C, T14> from14,
            PacketCodec<? super B, T15> codec15, Function<C, T15> from15,
            Function15<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, C> to
    ) {
        return new PacketCodec<>() {
            @Override
            public C decode(B object) {
                T1 object2 = codec1.decode(object);
                T2 object3 = codec2.decode(object);
                T3 object4 = codec3.decode(object);
                T4 object5 = codec4.decode(object);
                T5 object6 = codec5.decode(object);
                T6 object7 = codec6.decode(object);
                T7 object8 = codec7.decode(object);
                T8 object9 = codec8.decode(object);
                T9 object10 = codec9.decode(object);
                T10 object11 = codec10.decode(object);
                T11 object12 = codec11.decode(object);
                T12 object13 = codec12.decode(object);
                T13 object14 = codec13.decode(object);
                T14 object15 = codec14.decode(object);
                T15 object16 = codec15.decode(object);
                return to.apply(object2, object3, object4, object5, object6,
                        object7, object8, object9, object10, object11, object12, object13, object14, object15, object16);
            }

            @Override
            public void encode(B object, C object2) {
                codec1.encode(object, from1.apply(object2));
                codec2.encode(object, from2.apply(object2));
                codec3.encode(object, from3.apply(object2));
                codec4.encode(object, from4.apply(object2));
                codec5.encode(object, from5.apply(object2));
                codec6.encode(object, from6.apply(object2));
                codec7.encode(object, from7.apply(object2));
                codec8.encode(object, from8.apply(object2));
                codec9.encode(object, from9.apply(object2));
                codec10.encode(object, from10.apply(object2));
                codec11.encode(object, from11.apply(object2));
                codec12.encode(object, from12.apply(object2));
                codec13.encode(object, from13.apply(object2));
                codec14.encode(object, from14.apply(object2));
                codec15.encode(object, from15.apply(object2));
            }
        };
    }

    public static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> PacketCodec<B, C> tuple(
            PacketCodec<? super B, T1> codec1, Function<C, T1> from1,
            PacketCodec<? super B, T2> codec2, Function<C, T2> from2,
            PacketCodec<? super B, T3> codec3, Function<C, T3> from3,
            PacketCodec<? super B, T4> codec4, Function<C, T4> from4,
            PacketCodec<? super B, T5> codec5, Function<C, T5> from5,
            PacketCodec<? super B, T6> codec6, Function<C, T6> from6,
            PacketCodec<? super B, T7> codec7, Function<C, T7> from7,
            PacketCodec<? super B, T8> codec8, Function<C, T8> from8,
            PacketCodec<? super B, T9> codec9, Function<C, T9> from9,
            PacketCodec<? super B, T10> codec10, Function<C, T10> from10,
            PacketCodec<? super B, T11> codec11, Function<C, T11> from11,
            PacketCodec<? super B, T12> codec12, Function<C, T12> from12,
            PacketCodec<? super B, T13> codec13, Function<C, T13> from13,
            PacketCodec<? super B, T14> codec14, Function<C, T14> from14,
            PacketCodec<? super B, T15> codec15, Function<C, T15> from15,
            PacketCodec<? super B, T16> codec16, Function<C, T16> from16,
            Function16<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, C> to
    ) {
        return new PacketCodec<>() {
            @Override
            public C decode(B object) {
                T1 object2 = codec1.decode(object);
                T2 object3 = codec2.decode(object);
                T3 object4 = codec3.decode(object);
                T4 object5 = codec4.decode(object);
                T5 object6 = codec5.decode(object);
                T6 object7 = codec6.decode(object);
                T7 object8 = codec7.decode(object);
                T8 object9 = codec8.decode(object);
                T9 object10 = codec9.decode(object);
                T10 object11 = codec10.decode(object);
                T11 object12 = codec11.decode(object);
                T12 object13 = codec12.decode(object);
                T13 object14 = codec13.decode(object);
                T14 object15 = codec14.decode(object);
                T15 object16 = codec15.decode(object);
                T16 object17 = codec16.decode(object);
                return to.apply(object2, object3, object4, object5, object6,
                        object7, object8, object9, object10, object11, object12, object13, object14, object15, object16, object17);
            }

            @Override
            public void encode(B object, C object2) {
                codec1.encode(object, from1.apply(object2));
                codec2.encode(object, from2.apply(object2));
                codec3.encode(object, from3.apply(object2));
                codec4.encode(object, from4.apply(object2));
                codec5.encode(object, from5.apply(object2));
                codec6.encode(object, from6.apply(object2));
                codec7.encode(object, from7.apply(object2));
                codec8.encode(object, from8.apply(object2));
                codec9.encode(object, from9.apply(object2));
                codec10.encode(object, from10.apply(object2));
                codec11.encode(object, from11.apply(object2));
                codec12.encode(object, from12.apply(object2));
                codec13.encode(object, from13.apply(object2));
                codec14.encode(object, from14.apply(object2));
                codec15.encode(object, from15.apply(object2));
                codec16.encode(object, from16.apply(object2));
            }
        };
    }

    /**
     * Registers the default codecs for all {@link IntProviderType}s and {@link FloatProviderType}s.
     */
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
                    Pool<IntProvider> entries = value.weightedList;
                    PacketCodec<RegistryByteBuf, Pool<IntProvider>> entriesCodec = weightedListCodec((PacketCodec<RegistryByteBuf, IntProvider>) codec);
                    entriesCodec.encode(buf, entries);
                },
                buf -> {
                    PacketCodec<RegistryByteBuf, ? extends IntProvider> codec = getIntProviderCodec(IntProviderType.CONSTANT);
                    Pool<IntProvider> entries = weightedListCodec((PacketCodec<RegistryByteBuf, IntProvider>) codec).decode(buf);
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

    /**
     * Creates a codec for a weighted list of elements.
     *
     * @param elementCodec The codec for the elements.
     * @param <B>          The type of the {@link ByteBuf}.
     * @param <E>          The type of the elements.
     * @return The codec for the weighted list.
     * @see WeightedListIntProvider
     */
    public static <B extends ByteBuf, E> PacketCodec<B, Pool<E>> weightedListCodec(PacketCodec<B, E> elementCodec) {
        return PacketCodec.<B, Weighted<E>, E, Integer>tuple(
                elementCodec, Weighted::value,
                PacketCodecs.VAR_INT, Weighted::weight,
                Weighted::new
        ).collect(PacketCodecs.toList()).xmap(Pool::new, Pool::getEntries);
    }
}
