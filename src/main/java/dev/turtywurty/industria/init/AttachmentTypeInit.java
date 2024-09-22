package dev.turtywurty.industria.init;

import com.mojang.serialization.Codec;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.multiblock.MultiblockData;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.fluid.FluidState;

import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public class AttachmentTypeInit {
    public static final AttachmentType<Map<String, MultiblockData>> MULTIBLOCK_ATTACHMENT =
            AttachmentRegistry.createPersistent(Industria.id("multiblock"),
                    Codec.unboundedMap(Codec.STRING, MultiblockData.CODEC));

    public static final AttachmentType<Map<String, FluidState>> FLUID_MAP_ATTACHMENT =
            AttachmentRegistry.createPersistent(Industria.id("fluid_map"),
                    Codec.unboundedMap(Codec.STRING, FluidState.CODEC));

    public static void init() {}
}
