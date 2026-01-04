package dev.turtywurty.industria.util;

import com.mojang.datafixers.util.*;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.util.valueproviders.*;

import java.util.*;
import java.util.function.Function;

@SuppressWarnings("unchecked")
public class ExtraPacketCodecs {
    private static final Map<IntProviderType<?>, StreamCodec<RegistryFriendlyByteBuf, ? extends IntProvider>> INT_PROVIDER_CODECS = new HashMap<>();
    private static final Map<FloatProviderType<?>, StreamCodec<RegistryFriendlyByteBuf, ? extends FloatProvider>> FLOAT_PROVIDER_CODECS = new HashMap<>();

    public static final StreamCodec<ByteBuf, Set<BlockPos>> BLOCK_POS_SET_STREAM_CODEC = setOf(BlockPos.STREAM_CODEC);

    public static final StreamCodec<ByteBuf, BlockPos> BLOCK_POS_STRING_CODEC = ByteBufCodecs.fromCodec(ExtraCodecs.BLOCK_POS_STRING_CODEC);

    public static <B extends ByteBuf, V> StreamCodec<B, Set<V>> setOf(StreamCodec<? super B, V> codec) {
        return ByteBufCodecs.collection(HashSet::new, codec);
    }

    public static <B extends ByteBuf, V> StreamCodec<B, List<V>> listOf(StreamCodec<? super B, V> codec) {
        return ByteBufCodecs.collection(ArrayList::new, codec);
    }

    /**
     * Registers a codec for an {@link IntProviderType}.
     *
     * @param type  The type to register the codec for.
     * @param codec The codec to register.
     * @param <T>   The type of the {@link IntProvider}.
     */
    public static <T extends IntProvider> void registerIntProviderCodec(IntProviderType<T> type, StreamCodec<RegistryFriendlyByteBuf, T> codec) {
        INT_PROVIDER_CODECS.put(type, codec);
    }

    /**
     * Registers a codec for a {@link FloatProviderType}.
     *
     * @param type  The type to register the codec for.
     * @param codec The codec to register.
     * @param <T>   The type of the {@link FloatProvider}.
     */
    public static <T extends FloatProvider> void registerFloatProviderCodec(FloatProviderType<T> type, StreamCodec<RegistryFriendlyByteBuf, T> codec) {
        FLOAT_PROVIDER_CODECS.put(type, codec);
    }

    /**
     * Gets the codec for an {@link IntProviderType}.
     *
     * @param type The type to get the codec for.
     * @return The codec for the {@link IntProviderType}.
     * @see IntProviderType
     */
    public static StreamCodec<RegistryFriendlyByteBuf, ? extends IntProvider> getIntProviderCodec(IntProviderType<?> type) {
        return INT_PROVIDER_CODECS.get(type);
    }

    /**
     * Gets the codec for a {@link FloatProviderType}.
     *
     * @param type The type to get the codec for.
     * @return The codec for the {@link FloatProviderType}.
     * @see FloatProviderType
     */
    public static StreamCodec<RegistryFriendlyByteBuf, ? extends FloatProvider> getFloatProviderCodec(FloatProviderType<?> type) {
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
        StreamCodec<RegistryFriendlyByteBuf, T> codec = (StreamCodec<RegistryFriendlyByteBuf, T>) getIntProviderCodec(intProvider.getType());
        codec.encode((RegistryFriendlyByteBuf) buf, intProvider);
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
        StreamCodec<RegistryFriendlyByteBuf, T> codec = (StreamCodec<RegistryFriendlyByteBuf, T>) getIntProviderCodec(type);
        return codec.decode((RegistryFriendlyByteBuf) buf);
    }

    /**
     * Encodes a {@link FloatProvider} into a {@link ByteBuf}.
     *
     * @param buf           The {@link ByteBuf} to encode the {@link FloatProvider} into.
     * @param floatProvider The {@link FloatProvider} to encode.
     * @param <T>           The type of the {@link FloatProvider}.
     */
    public static <T extends FloatProvider> void encode(ByteBuf buf, T floatProvider) {
        StreamCodec<RegistryFriendlyByteBuf, T> codec = (StreamCodec<RegistryFriendlyByteBuf, T>) getFloatProviderCodec(floatProvider.getType());
        codec.encode((RegistryFriendlyByteBuf) buf, floatProvider);
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
        StreamCodec<RegistryFriendlyByteBuf, T> codec = (StreamCodec<RegistryFriendlyByteBuf, T>) getFloatProviderCodec(type);
        return codec.decode((RegistryFriendlyByteBuf) buf);
    }

    public static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9> StreamCodec<B, C> tuple(
            StreamCodec<? super B, T1> codec1,
            Function<C, T1> from1,
            StreamCodec<? super B, T2> codec2,
            Function<C, T2> from2,
            StreamCodec<? super B, T3> codec3,
            Function<C, T3> from3,
            StreamCodec<? super B, T4> codec4,
            Function<C, T4> from4,
            StreamCodec<? super B, T5> codec5,
            Function<C, T5> from5,
            StreamCodec<? super B, T6> codec6,
            Function<C, T6> from6,
            StreamCodec<? super B, T7> codec7,
            Function<C, T7> from7,
            StreamCodec<? super B, T8> codec8,
            Function<C, T8> from8,
            StreamCodec<? super B, T9> codec9,
            Function<C, T9> from9,
            Function9<T1, T2, T3, T4, T5, T6, T7, T8, T9, C> to
    ) {
        return new StreamCodec<>() {
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

    public static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> StreamCodec<B, C> tuple(
            StreamCodec<? super B, T1> codec1,
            Function<C, T1> from1,
            StreamCodec<? super B, T2> codec2,
            Function<C, T2> from2,
            StreamCodec<? super B, T3> codec3,
            Function<C, T3> from3,
            StreamCodec<? super B, T4> codec4,
            Function<C, T4> from4,
            StreamCodec<? super B, T5> codec5,
            Function<C, T5> from5,
            StreamCodec<? super B, T6> codec6,
            Function<C, T6> from6,
            StreamCodec<? super B, T7> codec7,
            Function<C, T7> from7,
            StreamCodec<? super B, T8> codec8,
            Function<C, T8> from8,
            StreamCodec<? super B, T9> codec9,
            Function<C, T9> from9,
            StreamCodec<? super B, T10> codec10,
            Function<C, T10> from10,
            Function10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, C> to
    ) {
        return new StreamCodec<>() {
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

    public static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> StreamCodec<B, C> tuple(
            StreamCodec<? super B, T1> codec1,
            Function<C, T1> from1,
            StreamCodec<? super B, T2> codec2,
            Function<C, T2> from2,
            StreamCodec<? super B, T3> codec3,
            Function<C, T3> from3,
            StreamCodec<? super B, T4> codec4,
            Function<C, T4> from4,
            StreamCodec<? super B, T5> codec5,
            Function<C, T5> from5,
            StreamCodec<? super B, T6> codec6,
            Function<C, T6> from6,
            StreamCodec<? super B, T7> codec7,
            Function<C, T7> from7,
            StreamCodec<? super B, T8> codec8,
            Function<C, T8> from8,
            StreamCodec<? super B, T9> codec9,
            Function<C, T9> from9,
            StreamCodec<? super B, T10> codec10,
            Function<C, T10> from10,
            StreamCodec<? super B, T11> codec11,
            Function<C, T11> from11,
            Function11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, C> to
    ) {
        return new StreamCodec<>() {
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

    public static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> StreamCodec<B, C> tuple(
            StreamCodec<? super B, T1> codec1, Function<C, T1> from1,
            StreamCodec<? super B, T2> codec2, Function<C, T2> from2,
            StreamCodec<? super B, T3> codec3, Function<C, T3> from3,
            StreamCodec<? super B, T4> codec4, Function<C, T4> from4,
            StreamCodec<? super B, T5> codec5, Function<C, T5> from5,
            StreamCodec<? super B, T6> codec6, Function<C, T6> from6,
            StreamCodec<? super B, T7> codec7, Function<C, T7> from7,
            StreamCodec<? super B, T8> codec8, Function<C, T8> from8,
            StreamCodec<? super B, T9> codec9, Function<C, T9> from9,
            StreamCodec<? super B, T10> codec10, Function<C, T10> from10,
            StreamCodec<? super B, T11> codec11, Function<C, T11> from11,
            StreamCodec<? super B, T12> codec12, Function<C, T12> from12,
            Function12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, C> to
    ) {
        return new StreamCodec<>() {
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

    public static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> StreamCodec<B, C> tuple(
            StreamCodec<? super B, T1> codec1, Function<C, T1> from1,
            StreamCodec<? super B, T2> codec2, Function<C, T2> from2,
            StreamCodec<? super B, T3> codec3, Function<C, T3> from3,
            StreamCodec<? super B, T4> codec4, Function<C, T4> from4,
            StreamCodec<? super B, T5> codec5, Function<C, T5> from5,
            StreamCodec<? super B, T6> codec6, Function<C, T6> from6,
            StreamCodec<? super B, T7> codec7, Function<C, T7> from7,
            StreamCodec<? super B, T8> codec8, Function<C, T8> from8,
            StreamCodec<? super B, T9> codec9, Function<C, T9> from9,
            StreamCodec<? super B, T10> codec10, Function<C, T10> from10,
            StreamCodec<? super B, T11> codec11, Function<C, T11> from11,
            StreamCodec<? super B, T12> codec12, Function<C, T12> from12,
            StreamCodec<? super B, T13> codec13, Function<C, T13> from13,
            Function13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, C> to
    ) {
        return new StreamCodec<>() {
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

    public static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> StreamCodec<B, C> tuple(
            StreamCodec<? super B, T1> codec1, Function<C, T1> from1,
            StreamCodec<? super B, T2> codec2, Function<C, T2> from2,
            StreamCodec<? super B, T3> codec3, Function<C, T3> from3,
            StreamCodec<? super B, T4> codec4, Function<C, T4> from4,
            StreamCodec<? super B, T5> codec5, Function<C, T5> from5,
            StreamCodec<? super B, T6> codec6, Function<C, T6> from6,
            StreamCodec<? super B, T7> codec7, Function<C, T7> from7,
            StreamCodec<? super B, T8> codec8, Function<C, T8> from8,
            StreamCodec<? super B, T9> codec9, Function<C, T9> from9,
            StreamCodec<? super B, T10> codec10, Function<C, T10> from10,
            StreamCodec<? super B, T11> codec11, Function<C, T11> from11,
            StreamCodec<? super B, T12> codec12, Function<C, T12> from12,
            StreamCodec<? super B, T13> codec13, Function<C, T13> from13,
            StreamCodec<? super B, T14> codec14, Function<C, T14> from14,
            Function14<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, C> to
    ) {
        return new StreamCodec<>() {
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

    public static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> StreamCodec<B, C> tuple(
            StreamCodec<? super B, T1> codec1, Function<C, T1> from1,
            StreamCodec<? super B, T2> codec2, Function<C, T2> from2,
            StreamCodec<? super B, T3> codec3, Function<C, T3> from3,
            StreamCodec<? super B, T4> codec4, Function<C, T4> from4,
            StreamCodec<? super B, T5> codec5, Function<C, T5> from5,
            StreamCodec<? super B, T6> codec6, Function<C, T6> from6,
            StreamCodec<? super B, T7> codec7, Function<C, T7> from7,
            StreamCodec<? super B, T8> codec8, Function<C, T8> from8,
            StreamCodec<? super B, T9> codec9, Function<C, T9> from9,
            StreamCodec<? super B, T10> codec10, Function<C, T10> from10,
            StreamCodec<? super B, T11> codec11, Function<C, T11> from11,
            StreamCodec<? super B, T12> codec12, Function<C, T12> from12,
            StreamCodec<? super B, T13> codec13, Function<C, T13> from13,
            StreamCodec<? super B, T14> codec14, Function<C, T14> from14,
            StreamCodec<? super B, T15> codec15, Function<C, T15> from15,
            Function15<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, C> to
    ) {
        return new StreamCodec<>() {
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

    public static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> StreamCodec<B, C> tuple(
            StreamCodec<? super B, T1> codec1, Function<C, T1> from1,
            StreamCodec<? super B, T2> codec2, Function<C, T2> from2,
            StreamCodec<? super B, T3> codec3, Function<C, T3> from3,
            StreamCodec<? super B, T4> codec4, Function<C, T4> from4,
            StreamCodec<? super B, T5> codec5, Function<C, T5> from5,
            StreamCodec<? super B, T6> codec6, Function<C, T6> from6,
            StreamCodec<? super B, T7> codec7, Function<C, T7> from7,
            StreamCodec<? super B, T8> codec8, Function<C, T8> from8,
            StreamCodec<? super B, T9> codec9, Function<C, T9> from9,
            StreamCodec<? super B, T10> codec10, Function<C, T10> from10,
            StreamCodec<? super B, T11> codec11, Function<C, T11> from11,
            StreamCodec<? super B, T12> codec12, Function<C, T12> from12,
            StreamCodec<? super B, T13> codec13, Function<C, T13> from13,
            StreamCodec<? super B, T14> codec14, Function<C, T14> from14,
            StreamCodec<? super B, T15> codec15, Function<C, T15> from15,
            StreamCodec<? super B, T16> codec16, Function<C, T16> from16,
            Function16<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, C> to
    ) {
        return new StreamCodec<>() {
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
        registerIntProviderCodec(IntProviderType.CONSTANT, StreamCodec.of(
                (buf, intProvider) -> buf.writeInt(intProvider.getValue()),
                buf -> ConstantInt.of(buf.readInt())));

        registerIntProviderCodec(IntProviderType.UNIFORM, StreamCodec.of(
                (buf, intProvider) -> {
                    buf.writeInt(intProvider.getMinValue());
                    buf.writeInt(intProvider.getMaxValue());
                },
                buf -> UniformInt.of(buf.readInt(), buf.readInt())));

        registerIntProviderCodec(IntProviderType.BIASED_TO_BOTTOM, StreamCodec.of(
                (buf, intProvider) -> {
                    buf.writeInt(intProvider.getMinValue());
                    buf.writeInt(intProvider.getMaxValue());
                },
                buf -> BiasedToBottomInt.of(buf.readInt(), buf.readInt())));

        registerIntProviderCodec(IntProviderType.CLAMPED, StreamCodec.of(
                (buf, intProvider) -> {
                    IntProviderType<?> type = BuiltInRegistries.INT_PROVIDER_TYPE.getValue(buf.readIdentifier());
                    StreamCodec<RegistryFriendlyByteBuf, IntProvider> codec = (StreamCodec<RegistryFriendlyByteBuf, IntProvider>) getIntProviderCodec(type);
                    codec.encode(buf, intProvider.source);
                    buf.writeInt(intProvider.getMinValue());
                    buf.writeInt(intProvider.getMaxValue());
                },
                buf -> ClampedInt.of(getIntProviderCodec(IntProviderType.CONSTANT).decode(buf), buf.readInt(), buf.readInt())));

        registerIntProviderCodec(IntProviderType.WEIGHTED_LIST, StreamCodec.of(
                (buf, value) -> {
                    StreamCodec<RegistryFriendlyByteBuf, ? extends IntProvider> codec = getIntProviderCodec(value.getType());
                    WeightedList<IntProvider> entries = value.distribution;
                    StreamCodec<RegistryFriendlyByteBuf, WeightedList<IntProvider>> entriesCodec = weightedListCodec((StreamCodec<RegistryFriendlyByteBuf, IntProvider>) codec);
                    entriesCodec.encode(buf, entries);
                },
                buf -> {
                    StreamCodec<RegistryFriendlyByteBuf, ? extends IntProvider> codec = getIntProviderCodec(IntProviderType.CONSTANT);
                    WeightedList<IntProvider> entries = weightedListCodec((StreamCodec<RegistryFriendlyByteBuf, IntProvider>) codec).decode(buf);
                    return new WeightedListInt(entries);
                }));

        registerIntProviderCodec(IntProviderType.CLAMPED_NORMAL, StreamCodec.of(
                (buf, intProvider) -> {
                    buf.writeFloat(intProvider.mean);
                    buf.writeFloat(intProvider.deviation);
                    buf.writeInt(intProvider.getMinValue());
                    buf.writeInt(intProvider.getMaxValue());
                },
                buf -> ClampedNormalInt.of(buf.readFloat(), buf.readFloat(), buf.readInt(), buf.readInt())));

        registerFloatProviderCodec(FloatProviderType.CONSTANT, StreamCodec.of(
                (buf, floatProvider) -> buf.writeFloat(floatProvider.getValue()),
                buf -> ConstantFloat.of(buf.readFloat())));

        registerFloatProviderCodec(FloatProviderType.UNIFORM, StreamCodec.of(
                (buf, floatProvider) -> {
                    buf.writeFloat(floatProvider.getMinValue());
                    buf.writeFloat(floatProvider.getMaxValue());
                },
                buf -> UniformFloat.of(buf.readFloat(), buf.readFloat())));

        registerFloatProviderCodec(FloatProviderType.CLAMPED_NORMAL, StreamCodec.of(
                (buf, floatProvider) -> {
                    buf.writeFloat(floatProvider.mean);
                    buf.writeFloat(floatProvider.deviation);
                    buf.writeFloat(floatProvider.getMinValue());
                    buf.writeFloat(floatProvider.getMaxValue());
                },
                buf -> ClampedNormalFloat.of(buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat())));

        registerFloatProviderCodec(FloatProviderType.TRAPEZOID, StreamCodec.of(
                (buf, floatProvider) -> {
                    buf.writeFloat(floatProvider.getMinValue());
                    buf.writeFloat(floatProvider.getMaxValue());
                    buf.writeFloat(floatProvider.plateau);
                },
                buf -> TrapezoidFloat.of(buf.readFloat(), buf.readFloat(), buf.readFloat())));
    }

    /**
     * Creates a codec for a weighted list of elements.
     *
     * @param elementCodec The codec for the elements.
     * @param <B>          The type of the {@link ByteBuf}.
     * @param <E>          The type of the elements.
     * @return The codec for the weighted list.
     * @see WeightedListInt
     */
    public static <B extends ByteBuf, E> StreamCodec<B, WeightedList<E>> weightedListCodec(StreamCodec<B, E> elementCodec) {
        return StreamCodec.<B, Weighted<E>, E, Integer>composite(
                elementCodec, Weighted::value,
                ByteBufCodecs.VAR_INT, Weighted::weight,
                Weighted::new
        ).apply(ByteBufCodecs.list()).map(WeightedList::new, WeightedList::unwrap);
    }
}
