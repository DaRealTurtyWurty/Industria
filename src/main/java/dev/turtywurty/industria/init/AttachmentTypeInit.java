package dev.turtywurty.industria.init;

import com.mojang.serialization.Codec;
import dev.turtywurty.industria.Industria;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.util.math.BlockPos;

import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public class AttachmentTypeInit {
    public static final AttachmentType<Map<String, BlockPos>> OIL_PUMP_JACK_ATTACHMENT =
            AttachmentRegistry.createPersistent(Industria.id("oil_pump_jack_primary_pos"),
                    Codec.unboundedMap(Codec.STRING, BlockPos.CODEC));

    public static void init() {}
}
