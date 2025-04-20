package dev.turtywurty.industria.util;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

public final class NbtUtils {
    private NbtUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static NbtCompound toNbt(BlockPos pos) {
        var nbt = new NbtCompound();
        nbt.put("Pos", BlockPos.CODEC, pos);
        return nbt;
    }

    public static Optional<BlockPos> fromNbt(NbtCompound nbt) {
        return nbt.get("Pos", BlockPos.CODEC);
    }
}
