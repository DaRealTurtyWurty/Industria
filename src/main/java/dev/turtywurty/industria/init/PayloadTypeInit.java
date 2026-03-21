package dev.turtywurty.industria.init;

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
        PayloadTypeRegistry.serverboundPlay().register(SetConveyorFilterStackPayload.ID, SetConveyorFilterStackPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SetConveyorBlacklistModePayload.ID, SetConveyorBlacklistModePayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SetConveyorMatchDurabilityPayload.ID, SetConveyorMatchDurabilityPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SetConveyorMatchEnchantmentsPayload.ID, SetConveyorMatchEnchantmentsPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SetConveyorMatchComponentsPayload.ID, SetConveyorMatchComponentsPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SetConveyorFilterTagPayload.ID, SetConveyorFilterTagPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SetConveyorTagFilteringPayload.ID, SetConveyorTagFilteringPayload.CODEC);
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
        PayloadTypeRegistry.clientboundPlay().register(AddConveyorNetworkPayload.ID, AddConveyorNetworkPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ModifyConveyorNetworkPayload.ID, ModifyConveyorNetworkPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(RemoveConveyorNetworkPayload.ID, RemoveConveyorNetworkPayload.CODEC);
    }
}
