package dev.turtywurty.industria.multiblock;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.turtywurty.industria.util.ExtraCodecs;
import dev.turtywurty.industria.util.ExtraPacketCodecs;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;

import java.util.ArrayList;
import java.util.List;

public record VariedBlockList(
        List<Block> blockList,
        List<BlockState> stateList,
        List<TagKey<Block>> tagList) {
    public static final VariedBlockList UNMODIFIABLE_EMPTY = new VariedBlockList(List.of(), List.of(), List.of());

    public VariedBlockList() {
        this(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    public static final Codec<VariedBlockList> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExtraCodecs.BLOCK_CODEC.listOf().fieldOf("blocks").forGetter(VariedBlockList::blockList),
            BlockState.CODEC.listOf().fieldOf("states").forGetter(VariedBlockList::stateList),
            TagKey.codec(RegistryKeys.BLOCK).listOf().fieldOf("tags").forGetter(VariedBlockList::tagList)
    ).apply(instance, VariedBlockList::new));

    public static final PacketCodec<RegistryByteBuf, VariedBlockList> PACKET_CODEC = PacketCodec.tuple(
            ExtraPacketCodecs.listOf(PacketCodecs.codec(ExtraCodecs.BLOCK_CODEC)), VariedBlockList::blockList,
            ExtraPacketCodecs.listOf(PacketCodecs.codec(BlockState.CODEC)), VariedBlockList::stateList,
            ExtraPacketCodecs.listOf(TagKey.packetCodec(RegistryKeys.BLOCK)), VariedBlockList::tagList,
            VariedBlockList::new
    );

    public List<BlockState> allStates(RegistryEntryLookup<Block> blockLookup) {
        List<BlockState> allStates = new ArrayList<>(this.stateList);
        for (Block block : this.blockList) {
            allStates.add(block.getDefaultState());
        }

        for (TagKey<Block> tag : this.tagList) {
            RegistryEntryList.Named<Block> entries = blockLookup.getOrThrow(tag);
            for (RegistryEntry<Block> entry : entries) {
                Block block = entry.value();
                allStates.add(block.getDefaultState());
            }
        }

        return allStates;
    }

    public static class Builder {
        private final List<Block> blockList = new ArrayList<>();
        private final List<BlockState> stateList = new ArrayList<>();
        private final List<TagKey<Block>> tagList = new ArrayList<>();

        public static Builder create() {
            return new Builder();
        }

        public Builder addBlock(Block block) {
            this.blockList.add(block);
            return this;
        }

        public Builder addState(BlockState state) {
            this.stateList.add(state);
            return this;
        }

        public Builder addTag(TagKey<Block> tag) {
            this.tagList.add(tag);
            return this;
        }

        public VariedBlockList build() {
            return new VariedBlockList(this.blockList, this.stateList, this.tagList);
        }
    }
}
