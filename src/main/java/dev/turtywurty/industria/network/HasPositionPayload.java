package dev.turtywurty.industria.network;

import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public interface HasPositionPayload extends CustomPayload {
    BlockPos pos();
}
