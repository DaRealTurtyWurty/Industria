package dev.turtywurty.industria.init;

import dev.turtywurty.industria.network.*;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class PayloadTypeInit {
    public static void init() {
        registerC2S();
        registerS2C();
    }

    private static void registerC2S() {
        PayloadTypeRegistry.serverboundPlay().register(BatteryChargeModePayload.ID, BatteryChargeModePayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ChangeDrillingPayload.ID, ChangeDrillingPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(RetractDrillPayload.ID, RetractDrillPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ChangeDrillOverflowModePayload.ID, ChangeDrillOverflowModePayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SetMotorTargetRPMPayload.ID, SetMotorTargetRPMPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(FluidTankChangeExtractModePayload.ID, FluidTankChangeExtractModePayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(OilPumpJackSetRunningPayload.ID, OilPumpJackSetRunningPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SetMultiblockPieceCharPayload.ID, SetMultiblockPieceCharPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(UpdatePaletteEntryNamePayload.ID, UpdatePaletteEntryNamePayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(DeletePaletteEntryPayload.ID, DeletePaletteEntryPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(UpdatePaletteEntryVariedBlockListPayload.ID, UpdatePaletteEntryVariedBlockListPayload.CODEC);
    }

    private static void registerS2C() {
        PayloadTypeRegistry.clientboundPlay().register(OpenSeismicScannerPayload.ID, OpenSeismicScannerPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SyncFluidPocketsPayload.ID, SyncFluidPocketsPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(UpgradeStationUpdateRecipesPayload.ID, UpgradeStationUpdateRecipesPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SyncPipeNetworkManagerPayload.ID, SyncPipeNetworkManagerPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(AddPipeNetworkPayload.ID, AddPipeNetworkPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(RemovePipeNetworkPayload.ID, RemovePipeNetworkPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ModifyPipeNetworkPayload.ID, ModifyPipeNetworkPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(RotaryKilnControllerRemovedPayload.ID, RotaryKilnControllerRemovedPayload.CODEC);
    }
}
