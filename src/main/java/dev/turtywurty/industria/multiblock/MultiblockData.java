package dev.turtywurty.industria.multiblock;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;

public record MultiblockData(BlockPos primaryPos, MultiblockType type) {
    public static final Codec<MultiblockData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("primary_pos").forGetter(MultiblockData::primaryPos),
            Codec.STRING.fieldOf("type").forGetter(data -> MultiblockType.toString(data.type))
    ).apply(instance, (primaryPos, type) -> new MultiblockData(primaryPos, MultiblockType.fromString(type))));
}