package dev.turtywurty.industria.multiblock;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.turtywurty.industria.util.ExtraCodecs;
import dev.turtywurty.industria.util.ExtraPacketCodecs;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

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
            TagKey.hashedCodec(Registries.BLOCK).listOf().fieldOf("tags").forGetter(VariedBlockList::tagList)
    ).apply(instance, VariedBlockList::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, VariedBlockList> STREAM_CODEC = StreamCodec.composite(
            ExtraPacketCodecs.listOf(ByteBufCodecs.fromCodec(ExtraCodecs.BLOCK_CODEC)), VariedBlockList::blockList,
            ExtraPacketCodecs.listOf(ByteBufCodecs.fromCodec(BlockState.CODEC)), VariedBlockList::stateList,
            ExtraPacketCodecs.listOf(TagKey.streamCodec(Registries.BLOCK)), VariedBlockList::tagList,
            VariedBlockList::new
    );

    public List<BlockState> allStates(HolderGetter<Block> blockLookup) {
        List<BlockState> allStates = new ArrayList<>(this.stateList);
        for (Block block : this.blockList) {
            allStates.add(block.defaultBlockState());
        }

        for (TagKey<Block> tag : this.tagList) {
            HolderSet.Named<Block> entries = blockLookup.getOrThrow(tag);
            for (Holder<Block> entry : entries) {
                Block block = entry.value();
                allStates.add(block.defaultBlockState());
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
