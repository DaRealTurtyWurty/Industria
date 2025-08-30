package dev.turtywurty.industria.multiblock.old;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.turtywurty.industria.init.IndustriaRegistries;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
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

    public static final PacketCodec<RegistryByteBuf, MultiblockData> PACKET_CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, MultiblockData::primaryPos,
            PacketCodecs.registryEntry(IndustriaRegistries.MULTIBLOCK_TYPES_KEY), data -> IndustriaRegistries.MULTIBLOCK_TYPES.getEntry(data.type()),
            (blockPos, multiblockTypeRegistryEntry) -> new MultiblockData(blockPos, multiblockTypeRegistryEntry.value())
    );
}