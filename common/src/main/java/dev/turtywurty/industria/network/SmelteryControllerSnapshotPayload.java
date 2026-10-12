package dev.turtywurty.industria.network;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.menu.SmelteryControllerMenu;
import dev.turtywurty.industria.util.ExtraStreamCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;

public record SmelteryControllerSnapshotPayload(
    int syncId,
    BlockPos controllerPos,
    float temperature,
    boolean structureReady,
    boolean keepMolten,
    List<SmelteryControllerMenu.SmelteryChamberInfo> chambers
) implements CustomPacketPayload {
    public static final Type<SmelteryControllerSnapshotPayload> ID = new Type<>(
        Industria.id("smeltery_controller_snapshot"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SmelteryControllerSnapshotPayload> CODEC = StreamCodec
        .composite(
            ByteBufCodecs.VAR_INT, SmelteryControllerSnapshotPayload::syncId,
            BlockPos.STREAM_CODEC, SmelteryControllerSnapshotPayload::controllerPos,
            ByteBufCodecs.FLOAT, SmelteryControllerSnapshotPayload::temperature,
            ByteBufCodecs.BOOL, SmelteryControllerSnapshotPayload::structureReady,
            ByteBufCodecs.BOOL, SmelteryControllerSnapshotPayload::keepMolten,
            ExtraStreamCodecs.listOf(SmelteryControllerMenu.SmelteryChamberInfo.STREAM_CODEC),
            SmelteryControllerSnapshotPayload::chambers,
            SmelteryControllerSnapshotPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
