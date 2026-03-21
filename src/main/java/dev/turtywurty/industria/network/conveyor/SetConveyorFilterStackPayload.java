package dev.turtywurty.industria.network.conveyor;

import dev.turtywurty.industria.Industria;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;

public record SetConveyorFilterStackPayload(ItemStack stack) implements CustomPacketPayload {
    public static final Type<SetConveyorFilterStackPayload> ID = new Type<>(Industria.id("set_conveyor_filter_stack"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SetConveyorFilterStackPayload> CODEC =
            StreamCodec.composite(
                    ItemStack.OPTIONAL_STREAM_CODEC, SetConveyorFilterStackPayload::stack,
                    SetConveyorFilterStackPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
