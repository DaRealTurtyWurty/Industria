package dev.turtywurty.industria.init;

import dev.turtywurty.industria.network.*;
import dev.turtywurty.industria.network.conveyor.AddConveyorNetworkPayload;
import dev.turtywurty.industria.network.conveyor.ModifyConveyorNetworkPayload;
import dev.turtywurty.industria.network.conveyor.RemoveConveyorNetworkPayload;
import dev.turtywurty.industria.network.conveyor.SetFilterConveyorBlacklistModePayload;
import dev.turtywurty.industria.network.conveyor.SetFilterConveyorFilterStackPayload;
import dev.turtywurty.industria.network.conveyor.SetFilterConveyorFilterTagPayload;
import dev.turtywurty.industria.network.conveyor.SetFilterConveyorMatchComponentsPayload;
import dev.turtywurty.industria.network.conveyor.SetFilterConveyorMatchDurabilityPayload;
import dev.turtywurty.industria.network.conveyor.SetFilterConveyorMatchEnchantmentsPayload;
import dev.turtywurty.industria.network.conveyor.SetFilterConveyorTagFilteringPayload;
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
        PayloadTypeRegistry.serverboundPlay().register(SetFilterConveyorFilterStackPayload.ID, SetFilterConveyorFilterStackPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SetFilterConveyorBlacklistModePayload.ID, SetFilterConveyorBlacklistModePayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SetFilterConveyorMatchDurabilityPayload.ID, SetFilterConveyorMatchDurabilityPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SetFilterConveyorMatchEnchantmentsPayload.ID, SetFilterConveyorMatchEnchantmentsPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SetFilterConveyorMatchComponentsPayload.ID, SetFilterConveyorMatchComponentsPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SetFilterConveyorFilterTagPayload.ID, SetFilterConveyorFilterTagPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SetFilterConveyorTagFilteringPayload.ID, SetFilterConveyorTagFilteringPayload.CODEC);
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
