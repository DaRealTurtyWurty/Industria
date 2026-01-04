package dev.turtywurty.industria.multiblock.old;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.turtywurty.industria.init.IndustriaRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

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
            IndustriaRegistries.MULTIBLOCK_TYPES.holderByNameCodec().fieldOf("type").forGetter(data -> IndustriaRegistries.MULTIBLOCK_TYPES.wrapAsHolder(data.type()))
    ).apply(instance, (primaryPos, type) -> new MultiblockData(primaryPos, type.value())));

    public static final StreamCodec<RegistryFriendlyByteBuf, MultiblockData> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, MultiblockData::primaryPos,
            ByteBufCodecs.holderRegistry(IndustriaRegistries.MULTIBLOCK_TYPES_KEY), data -> IndustriaRegistries.MULTIBLOCK_TYPES.wrapAsHolder(data.type()),
            (blockPos, multiblockTypeRegistryEntry) -> new MultiblockData(blockPos, multiblockTypeRegistryEntry.value())
    );
}