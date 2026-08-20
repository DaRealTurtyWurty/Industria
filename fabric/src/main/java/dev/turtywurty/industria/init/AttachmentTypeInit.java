package dev.turtywurty.industria.init;

import com.mojang.serialization.Codec;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.multiblock.old.MultiblockData;
import dev.turtywurty.industria.util.ExtraCodecs;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;

import java.util.HashMap;
import java.util.Map;

public class AttachmentTypeInit {
    public static final AttachmentType<Map<BlockPos, MultiblockData>> MULTIBLOCK_ATTACHMENT =
            AttachmentRegistry.create(Industria.id("multiblock"),
                    mapBuilder -> mapBuilder.persistent(Codec.unboundedMap(ExtraCodecs.BLOCK_POS_STRING_CODEC, MultiblockData.CODEC))
                            .syncWith(ByteBufCodecs.map(HashMap::new, BlockPos.STREAM_CODEC, MultiblockData.STREAM_CODEC),
                                    AttachmentSyncPredicate.all()));

    public static final AttachmentType<Integer> STOMACH_DESTRUCTION_ATTACHMENT =
            AttachmentRegistry.create(Industria.id("stomach_destruction"),
                    builder -> builder.persistent(Codec.INT)
                            .syncWith(ByteBufCodecs.INT, AttachmentSyncPredicate.targetOnly()));

    public static void init() {
    }
}
