package dev.turtywurty.industria.network.conveyor;

import dev.turtywurty.industria.Industria;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;

public record SetFilterConveyorFilterStackPayload(ItemStack stack) implements CustomPacketPayload {
    public static final Type<SetFilterConveyorFilterStackPayload> ID = new Type<>(Industria.id("set_filter_conveyor_filter_stack"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SetFilterConveyorFilterStackPayload> CODEC =
            StreamCodec.composite(
                    ItemStack.OPTIONAL_STREAM_CODEC, SetFilterConveyorFilterStackPayload::stack,
                    SetFilterConveyorFilterStackPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
