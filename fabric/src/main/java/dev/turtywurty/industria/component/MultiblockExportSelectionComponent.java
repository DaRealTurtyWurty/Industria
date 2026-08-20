package dev.turtywurty.industria.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;

public record MultiblockExportSelectionComponent(List<BlockPos> positions) {
    public static final MultiblockExportSelectionComponent EMPTY = new MultiblockExportSelectionComponent(List.of());

    public static final Codec<MultiblockExportSelectionComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.listOf().fieldOf("positions").forGetter(MultiblockExportSelectionComponent::positions)
    ).apply(instance, MultiblockExportSelectionComponent::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MultiblockExportSelectionComponent> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.collection(ArrayList::new, BlockPos.STREAM_CODEC),
                    MultiblockExportSelectionComponent::positions,
                    MultiblockExportSelectionComponent::new
            );

    public MultiblockExportSelectionComponent {
        positions = List.copyOf(positions);
    }
}
