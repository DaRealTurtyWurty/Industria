package dev.turtywurty.industria.init;

import com.mojang.serialization.Codec;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.multiblock.old.MultiblockData;
import dev.turtywurty.industria.util.ExtraCodecs;
import dev.turtywurty.turtymultiloader.attachment.AttachmentType;
import dev.turtywurty.turtymultiloader.attachment.Attachments;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;

import java.util.HashMap;
import java.util.Map;

public class ModAttachmentTypes {
    public static final AttachmentType<Map<BlockPos, MultiblockData>> MULTIBLOCK_ATTACHMENT =
            Attachments.register(Industria.id("multiblock"),
                    mapBuilder -> mapBuilder
                            .persistent(Codec.unboundedMap(ExtraCodecs.BLOCK_POS_STRING_CODEC, MultiblockData.CODEC))
                            .syncToTrackers(ByteBufCodecs.map(HashMap::new, BlockPos.STREAM_CODEC, MultiblockData.STREAM_CODEC)));

    public static final AttachmentType<Integer> STOMACH_DESTRUCTION_ATTACHMENT =
            Attachments.register(Industria.id("stomach_destruction"),
                    builder -> builder
                            .persistent(Codec.INT)
                            .syncToOwner(ByteBufCodecs.INT));

    public static void init() {
    }
}
