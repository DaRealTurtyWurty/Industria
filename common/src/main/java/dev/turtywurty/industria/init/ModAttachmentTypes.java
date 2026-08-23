package dev.turtywurty.industria.init;

import com.mojang.serialization.Codec;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.turtymultiloader.attachment.AttachmentType;
import dev.turtywurty.turtymultiloader.attachment.Attachments;
import net.minecraft.network.codec.ByteBufCodecs;

public class ModAttachmentTypes {
    public static final AttachmentType<Integer> STOMACH_DESTRUCTION_ATTACHMENT =
            Attachments.register(Industria.id("stomach_destruction"),
                    builder -> builder
                            .persistent(Codec.INT)
                            .syncToOwner(ByteBufCodecs.INT));

    public static void init() {
    }
}
