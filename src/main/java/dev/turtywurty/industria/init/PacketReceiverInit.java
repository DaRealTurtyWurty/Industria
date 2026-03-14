package dev.turtywurty.industria.init;

import dev.turtywurty.industria.blockentity.DrillBlockEntity;
import dev.turtywurty.industria.blockentity.MotorBlockEntity;
import dev.turtywurty.industria.network.*;
import dev.turtywurty.industria.network.conveyor.SetFilterConveyorBlacklistModePayload;
import dev.turtywurty.industria.network.conveyor.SetFilterConveyorFilterStackPayload;
import dev.turtywurty.industria.network.conveyor.SetFilterConveyorFilterTagPayload;
import dev.turtywurty.industria.network.conveyor.SetFilterConveyorMatchComponentsPayload;
import dev.turtywurty.industria.network.conveyor.SetFilterConveyorMatchDurabilityPayload;
import dev.turtywurty.industria.network.conveyor.SetFilterConveyorMatchEnchantmentsPayload;
import dev.turtywurty.industria.network.conveyor.SetFilterConveyorTagFilteringPayload;
import dev.turtywurty.industria.screenhandler.*;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class PacketReceiverInit {
    public static void init() {
        ServerPlayNetworking.registerGlobalReceiver(BatteryChargeModePayload.ID, (payload, context) ->
                context.server().execute(() -> {
                    ServerPlayer player = context.player();
                    AbstractContainerMenu handler = player.containerMenu;
                    if (handler instanceof BatteryScreenHandler batteryScreenHandler) {
                        batteryScreenHandler.getBlockEntity().setChargeMode(payload.chargeMode());
                    }
                }));

        ServerPlayNetworking.registerGlobalReceiver(ChangeDrillingPayload.ID, (payload, context) -> {
            ServerPlayer player = context.player();
            if (player.containerMenu instanceof DrillScreenHandler handler) {
                DrillBlockEntity blockEntity = handler.getBlockEntity();
                blockEntity.setDrilling(payload.drilling());
                blockEntity.update();
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(RetractDrillPayload.ID, (payload, context) -> {
            ServerPlayer player = context.player();
            if (player.containerMenu instanceof DrillScreenHandler handler) {
                DrillBlockEntity blockEntity = handler.getBlockEntity();
                blockEntity.setDrilling(false);
                blockEntity.setRetracting(true);
                blockEntity.update();
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(ChangeDrillOverflowModePayload.ID, (payload, context) -> {
            ServerPlayer player = context.player();
            if (player.containerMenu instanceof DrillScreenHandler handler) {
                DrillBlockEntity blockEntity = handler.getBlockEntity();
                blockEntity.setOverflowMethod(payload.overflowMethod());
                blockEntity.update();
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(SetMotorTargetRPMPayload.ID, (payload, context) -> {
            ServerPlayer player = context.player();
            if (player.containerMenu instanceof MotorScreenHandler handler) {
                MotorBlockEntity blockEntity = handler.getBlockEntity();
                blockEntity.setTargetRotationSpeed(payload.targetRPM() / 60f);
                blockEntity.update();
            } else if (player.containerMenu instanceof DrillScreenHandler handler) {
                DrillBlockEntity blockEntity = handler.getBlockEntity();
                blockEntity.setTargetRotationSpeed(payload.targetRPM() / 60f);
                blockEntity.update();
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(FluidTankChangeExtractModePayload.ID, (payload, context) -> {
            ServerPlayer player = context.player();
            if (player.containerMenu instanceof FluidTankScreenHandler handler) {
                handler.getBlockEntity().setExtractMode(payload.extractMode());
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(OilPumpJackSetRunningPayload.ID, (payload, context) -> {
            ServerPlayer player = context.player();
            if(player.containerMenu instanceof OilPumpJackScreenHandler handler) {
                handler.getBlockEntity().setRunning(payload.isRunning());
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(SetFilterConveyorFilterStackPayload.ID, (payload, context) -> {
            ServerPlayer player = context.player();
            if(player.containerMenu instanceof FilterConveyorScreenHandler handler) {
                handler.getBlockEntity().setFilterStack(payload.stack());
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(SetFilterConveyorBlacklistModePayload.ID, (payload, context) -> {
            ServerPlayer player = context.player();
            if (player.containerMenu instanceof FilterConveyorScreenHandler handler) {
                handler.getBlockEntity().setBlacklistMode(payload.blacklistMode());
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(SetFilterConveyorMatchDurabilityPayload.ID, (payload, context) -> {
            ServerPlayer player = context.player();
            if (player.containerMenu instanceof FilterConveyorScreenHandler handler) {
                handler.getBlockEntity().setMatchDurability(payload.matchDurability());
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(SetFilterConveyorMatchEnchantmentsPayload.ID, (payload, context) -> {
            ServerPlayer player = context.player();
            if (player.containerMenu instanceof FilterConveyorScreenHandler handler) {
                handler.getBlockEntity().setMatchEnchantments(payload.matchEnchantments());
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(SetFilterConveyorMatchComponentsPayload.ID, (payload, context) -> {
            ServerPlayer player = context.player();
            if (player.containerMenu instanceof FilterConveyorScreenHandler handler) {
                handler.getBlockEntity().setMatchComponents(payload.matchComponents());
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(SetFilterConveyorFilterTagPayload.ID, (payload, context) -> {
            ServerPlayer player = context.player();
            if (player.containerMenu instanceof FilterConveyorScreenHandler handler) {
                handler.getBlockEntity().setFilterTag(payload.filterTag());
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(SetFilterConveyorTagFilteringPayload.ID, (payload, context) -> {
            ServerPlayer player = context.player();
            if (player.containerMenu instanceof FilterConveyorScreenHandler handler) {
                handler.getBlockEntity().setTagFiltering(payload.tagFiltering());
            }
        });
    }
}
