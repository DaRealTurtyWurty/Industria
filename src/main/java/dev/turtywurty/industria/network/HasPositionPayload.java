package dev.turtywurty.industria.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface HasPositionPayload extends CustomPacketPayload {
    BlockPos pos();
}
