package dev.turtywurty.industria.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.List;
import java.util.Set;

public class ExtraCodecs {
    public static final Codec<Set<BlockPos>> BLOCK_POS_SET_CODEC = setOf(BlockPos.CODEC);
    public static final Codec<Box> BOX_CODEC = Codec.DOUBLE
            .listOf()
            .xmap(
                    list -> new Box(
                            list.get(0),
                            list.get(1),
                            list.get(2),
                            list.get(3),
                            list.get(4),
                            list.get(5)
                    ),
                    box -> List.of(
                            box.minX,
                            box.minY,
                            box.minZ,
                            box.maxX,
                            box.maxY,
                            box.maxZ
                    )
            );

    public static final Codec<BlockPos> BLOCK_POS_STRING_CODEC = Codec.STRING.comapFlatMap(
            str -> {
                String[] parts = str.split(" ");
                if (parts.length != 3) {
                    return DataResult.error(() -> "Invalid BlockPos format: " + str);
                }

                try {
                    int x = Integer.parseInt(parts[0]);
                    int y = Integer.parseInt(parts[1]);
                    int z = Integer.parseInt(parts[2]);
                    return DataResult.success(new BlockPos(x, y, z));
                } catch (NumberFormatException e) {
                    return DataResult.error(() -> "Invalid BlockPos format: " + str);
                }
            },
            blockPos -> blockPos.getX() + " " + blockPos.getY() + " " + blockPos.getZ());
    public static final Codec<Character> CHAR_CODEC = Codec.STRING.xmap(s -> s.charAt(0), String::valueOf);

    public static <T> Codec<Set<T>> setOf(Codec<T> codec) {
        return Codec.list(codec).xmap(Sets::newHashSet, Lists::newArrayList);
    }

    public static <T> Codec<Set<T>> setOf(MapCodec<T> codec) {
        return setOf(codec.codec());
    }

    public static <T> Codec<List<T>> listOf(MapCodec<T> codec) {
        return Codec.list(codec.codec());
    }
}
