package dev.turtywurty.industria.util;

import com.mojang.datafixers.util.*;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.util.valueproviders.*;
import org.joml.Vector3d;

import java.util.*;
import java.util.function.Function;

public class ExtraStreamCodecs {
    public static final StreamCodec<ByteBuf, Map<BlockPos, UUID>> BLOCK_POS_TO_UUID_STREAM_CODEC =
            ByteBufCodecs.map(HashMap::new, BlockPos.STREAM_CODEC, UUIDUtil.STREAM_CODEC);
    public static final StreamCodec<RegistryFriendlyByteBuf, IntProvider> INT_PROVIDER_STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(IntProviders.CODEC);
    public static final StreamCodec<RegistryFriendlyByteBuf, FloatProvider> FLOAT_PROVIDER_STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(FloatProviders.CODEC);

    public static final StreamCodec<ByteBuf, Set<BlockPos>> BLOCK_POS_SET_STREAM_CODEC = setOf(BlockPos.STREAM_CODEC);
    public static final StreamCodec<ByteBuf, BlockPos> BLOCK_POS_STRING_CODEC = ByteBufCodecs.fromCodec(ExtraCodecs.BLOCK_POS_STRING_CODEC);
    public static final StreamCodec<ByteBuf, Vector3d> VECTOR_3D_STREAM_CODEC = ByteBufCodecs.fromCodec(ExtraCodecs.VECTOR_3D_CODEC);

    public static <B extends ByteBuf, V> StreamCodec<B, Set<V>> setOf(StreamCodec<? super B, V> codec) {
        return ByteBufCodecs.collection(HashSet::new, codec);
    }

    public static <B extends ByteBuf, V> StreamCodec<B, List<V>> listOf(StreamCodec<? super B, V> codec) {
        return ByteBufCodecs.collection(ArrayList::new, codec);
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
