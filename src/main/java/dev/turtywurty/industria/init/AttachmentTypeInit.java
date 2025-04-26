package dev.turtywurty.industria.init;

import com.mojang.serialization.Codec;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.multiblock.MultiblockData;
import dev.turtywurty.industria.util.ExtraCodecs;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.fluid.Fluid;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public class AttachmentTypeInit {
    public static final AttachmentType<Map<BlockPos, MultiblockData>> MULTIBLOCK_ATTACHMENT =
            AttachmentRegistry.create(Industria.id("multiblock"),
                    mapBuilder -> mapBuilder.persistent(Codec.unboundedMap(ExtraCodecs.BLOCK_POS_STRING_CODEC, MultiblockData.CODEC))
                            .syncWith(PacketCodecs.map(HashMap::new, BlockPos.PACKET_CODEC, MultiblockData.PACKET_CODEC),
                                    AttachmentSyncPredicate.all()));

    public static final AttachmentType<Map<String, RegistryEntry<Fluid>>> FLUID_MAP_ATTACHMENT =
            AttachmentRegistry.create(Industria.id("fluid_map"),
                    mapBuilder -> mapBuilder.persistent(Codec.unboundedMap(Codec.STRING, Registries.FLUID.getEntryCodec()))
                            .syncWith(PacketCodecs.map(HashMap::new,
                                            PacketCodecs.STRING,
                                            PacketCodecs.registryEntry(RegistryKeys.FLUID)),
                                    AttachmentSyncPredicate.all()));

    public static void init() {}
}
