package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.network.*;
import dev.turtywurty.industria.network.conveyor.AddConveyorNetworkPayload;
import dev.turtywurty.industria.network.conveyor.ModifyConveyorNetworkPayload;
import dev.turtywurty.industria.network.conveyor.RemoveConveyorNetworkPayload;
import dev.turtywurty.industria.network.conveyor.SetConveyorBlacklistModePayload;
import dev.turtywurty.industria.network.conveyor.SetConveyorFilterStackPayload;
import dev.turtywurty.industria.network.conveyor.SetConveyorFilterTagPayload;
import dev.turtywurty.industria.network.conveyor.SetConveyorMatchComponentsPayload;
import dev.turtywurty.industria.network.conveyor.SetConveyorMatchDurabilityPayload;
import dev.turtywurty.industria.network.conveyor.SetConveyorMatchEnchantmentsPayload;
import dev.turtywurty.industria.network.conveyor.SetConveyorTagFilteringPayload;
import dev.turtywurty.industria.network.pipe.AddPipeNetworkPayload;
import dev.turtywurty.industria.network.pipe.ModifyPipeNetworkPayload;
import dev.turtywurty.industria.network.pipe.RemovePipeNetworkPayload;
import dev.turtywurty.industria.network.pipe.SyncPipeNetworkManagerPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class PayloadTypeInit {
    public static void init() {
        registerC2S();
        registerS2C();
    }

    private static void registerC2S() {
        registerServerbound(BatteryChargeModePayload.ID, BatteryChargeModePayload.CODEC);
        registerServerbound(ChangeDrillingPayload.ID, ChangeDrillingPayload.CODEC);
        registerServerbound(RetractDrillPayload.ID, RetractDrillPayload.CODEC);
        registerServerbound(ChangeDrillOverflowModePayload.ID, ChangeDrillOverflowModePayload.CODEC);
        registerServerbound(SetMotorTargetRPMPayload.ID, SetMotorTargetRPMPayload.CODEC);
        registerServerbound(FluidTankChangeExtractModePayload.ID, FluidTankChangeExtractModePayload.CODEC);
        registerServerbound(OilPumpJackSetRunningPayload.ID, OilPumpJackSetRunningPayload.CODEC);
        registerServerbound(SetConveyorFilterStackPayload.ID, SetConveyorFilterStackPayload.CODEC);
        registerServerbound(SetConveyorBlacklistModePayload.ID, SetConveyorBlacklistModePayload.CODEC);
        registerServerbound(SetConveyorMatchDurabilityPayload.ID, SetConveyorMatchDurabilityPayload.CODEC);
        registerServerbound(SetConveyorMatchEnchantmentsPayload.ID, SetConveyorMatchEnchantmentsPayload.CODEC);
        registerServerbound(SetConveyorMatchComponentsPayload.ID, SetConveyorMatchComponentsPayload.CODEC);
        registerServerbound(SetConveyorFilterTagPayload.ID, SetConveyorFilterTagPayload.CODEC);
        registerServerbound(SetConveyorTagFilteringPayload.ID, SetConveyorTagFilteringPayload.CODEC);
    }

    private static void registerS2C() {
        registerClientbound(OpenSeismicScannerPayload.ID, OpenSeismicScannerPayload.CODEC);
        registerClientbound(SyncFluidPocketsPayload.ID, SyncFluidPocketsPayload.CODEC);
        registerClientbound(UpgradeStationUpdateRecipesPayload.ID, UpgradeStationUpdateRecipesPayload.CODEC);
        registerClientbound(SyncPipeNetworkManagerPayload.ID, SyncPipeNetworkManagerPayload.CODEC);
        registerClientbound(AddPipeNetworkPayload.ID, AddPipeNetworkPayload.CODEC);
        registerClientbound(RemovePipeNetworkPayload.ID, RemovePipeNetworkPayload.CODEC);
        registerClientbound(ModifyPipeNetworkPayload.ID, ModifyPipeNetworkPayload.CODEC);
        registerClientbound(RotaryKilnControllerRemovedPayload.ID, RotaryKilnControllerRemovedPayload.CODEC);
        registerClientbound(AddConveyorNetworkPayload.ID, AddConveyorNetworkPayload.CODEC);
        registerClientbound(ModifyConveyorNetworkPayload.ID, ModifyConveyorNetworkPayload.CODEC);
        registerClientbound(RemoveConveyorNetworkPayload.ID, RemoveConveyorNetworkPayload.CODEC);
    }

    private static <T extends CustomPacketPayload> void registerServerbound(CustomPacketPayload.Type<T> id, StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
        PayloadTypeRegistry.serverboundPlay().register(id, debugCodec(id, codec, "serverbound"));
    }

    private static <T extends CustomPacketPayload> void registerClientbound(CustomPacketPayload.Type<T> id, StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
        PayloadTypeRegistry.clientboundPlay().register(id, debugCodec(id, codec, "clientbound"));
    }

    private static <T extends CustomPacketPayload> StreamCodec<RegistryFriendlyByteBuf, T> debugCodec(CustomPacketPayload.Type<T> id,
                                                                                                       StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
                                                                                                       String direction) {
        return StreamCodec.of(
                (output, value) -> {
                    try {
                        codec.encode(output, value);
                    } catch (Exception exception) {
                        Industria.LOGGER.error("Failed to encode {} custom payload '{}'", direction, id.id(), exception);
                        throw exception;
                    }
                },
                input -> {
                    int readerIndex = input.readerIndex();
                    int readableBytes = input.readableBytes();
                    try {
                        return codec.decode(input);
                    } catch (Exception exception) {
                        Industria.LOGGER.error(
                                "Failed to decode {} custom payload '{}' at readerIndex={} readableBytes={}",
                                direction,
                                id.id(),
                                readerIndex,
                                readableBytes,
                                exception
                        );
                        throw exception;
                    }
                }
        );
    }
}
