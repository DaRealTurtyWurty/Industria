package dev.turtywurty.industria.init;

import com.mojang.serialization.Codec;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.multiblock.old.MultiblockData;
import dev.turtywurty.industria.util.ExtraCodecs;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.level.material.Fluid;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public class AttachmentTypeInit {
    public static final AttachmentType<Map<BlockPos, MultiblockData>> MULTIBLOCK_ATTACHMENT =
            AttachmentRegistry.create(Industria.id("multiblock"),
                    mapBuilder -> mapBuilder.persistent(Codec.unboundedMap(ExtraCodecs.BLOCK_POS_STRING_CODEC, MultiblockData.CODEC))
                            .syncWith(ByteBufCodecs.map(HashMap::new, BlockPos.STREAM_CODEC, MultiblockData.STREAM_CODEC),
                                    AttachmentSyncPredicate.all()));

    public static final AttachmentType<Map<String, Holder<Fluid>>> FLUID_MAP_ATTACHMENT =
            AttachmentRegistry.create(Industria.id("fluid_map"),
                    mapBuilder -> mapBuilder.persistent(Codec.unboundedMap(Codec.STRING, BuiltInRegistries.FLUID.holderByNameCodec()))
                            .syncWith(ByteBufCodecs.map(HashMap::new,
                                            ByteBufCodecs.STRING_UTF8,
                                            ByteBufCodecs.holderRegistry(Registries.FLUID)),
                                    AttachmentSyncPredicate.all()));

    public static void init() {}
}
