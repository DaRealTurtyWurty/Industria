package dev.turtywurty.industria.init;

import dev.turtywurty.industria.network.*;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class PayloadTypeInit {
    public static void init() {
        registerC2S();
        registerS2C();
    }

    private static void registerC2S() {
        PayloadTypeRegistry.playC2S().register(BatteryChargeModePayload.ID, BatteryChargeModePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ChangeDrillingPayload.ID, ChangeDrillingPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(RetractDrillPayload.ID, RetractDrillPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ChangeDrillOverflowModePayload.ID, ChangeDrillOverflowModePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SetMotorTargetRPMPayload.ID, SetMotorTargetRPMPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(FluidTankChangeExtractModePayload.ID, FluidTankChangeExtractModePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(OilPumpJackSetRunningPayload.ID, OilPumpJackSetRunningPayload.CODEC);
    }

    private static void registerS2C() {
        PayloadTypeRegistry.playS2C().register(OpenSeismicScannerPayload.ID, OpenSeismicScannerPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SyncFluidPocketsPayload.ID, SyncFluidPocketsPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(UpgradeStationUpdateRecipesPayload.ID, UpgradeStationUpdateRecipesPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SyncPipeNetworkManagerPayload.ID, SyncPipeNetworkManagerPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(AddPipeNetworkPayload.ID, AddPipeNetworkPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(RemovePipeNetworkPayload.ID, RemovePipeNetworkPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ModifyPipeNetworkPayload.ID, ModifyPipeNetworkPayload.CODEC);
    }
}
