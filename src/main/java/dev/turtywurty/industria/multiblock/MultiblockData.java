package dev.turtywurty.industria.multiblock;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.turtywurty.industria.init.IndustriaRegistries;
import net.minecraft.util.math.BlockPos;

/**
 * Represents the data of a multiblock
 *
 * @param primaryPos The primary position of the multiblock
 *                   (The position of the block that the multiblock is centered around)
 * @param type       The type of multiblock
 * @see MultiblockType
 */
public record MultiblockData(BlockPos primaryPos, MultiblockType<?> type) {
    public static final Codec<MultiblockData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("primary_pos").forGetter(MultiblockData::primaryPos),
            IndustriaRegistries.MULTIBLOCK_TYPES.getEntryCodec().fieldOf("type").forGetter(data -> IndustriaRegistries.MULTIBLOCK_TYPES.getEntry(data.type()))
    ).apply(instance, (primaryPos, type) -> new MultiblockData(primaryPos, type.value())));
}