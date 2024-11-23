package dev.turtywurty.industria.init;

import com.mojang.serialization.Codec;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.multiblock.MultiblockData;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.fluid.Fluid;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public class AttachmentTypeInit {
    public static final AttachmentType<Map<String, MultiblockData>> MULTIBLOCK_ATTACHMENT =
            AttachmentRegistry.create(Industria.id("multiblock"),
                    mapBuilder -> mapBuilder.persistent(Codec.unboundedMap(Codec.STRING, MultiblockData.CODEC))
                            .syncWith(PacketCodecs.map(HashMap::new, PacketCodecs.STRING, MultiblockData.PACKET_CODEC),
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
