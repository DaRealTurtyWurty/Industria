package dev.turtywurty.industria.init;

import dev.turtywurty.industria.blockentity.DrillBlockEntity;
import dev.turtywurty.industria.blockentity.MotorBlockEntity;
import dev.turtywurty.industria.conveyor.block.impl.entity.ConveyorFilterAccess;
import dev.turtywurty.industria.network.*;
import dev.turtywurty.industria.network.conveyor.SetConveyorBlacklistModePayload;
import dev.turtywurty.industria.network.conveyor.SetConveyorFilterStackPayload;
import dev.turtywurty.industria.network.conveyor.SetConveyorFilterTagPayload;
import dev.turtywurty.industria.network.conveyor.SetConveyorMatchComponentsPayload;
import dev.turtywurty.industria.network.conveyor.SetConveyorMatchDurabilityPayload;
import dev.turtywurty.industria.network.conveyor.SetConveyorMatchEnchantmentsPayload;
import dev.turtywurty.industria.network.conveyor.SetConveyorTagFilteringPayload;
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

        ServerPlayNetworking.registerGlobalReceiver(SetConveyorFilterStackPayload.ID, (payload, context) -> {
            ConveyorFilterAccess blockEntity = getConveyorFilterAccess(context.player());
            if (blockEntity != null) {
                blockEntity.setFilterStack(payload.stack());
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(SetConveyorBlacklistModePayload.ID, (payload, context) -> {
            ConveyorFilterAccess blockEntity = getConveyorFilterAccess(context.player());
            if (blockEntity != null) {
                blockEntity.setBlacklistMode(payload.blacklistMode());
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(SetConveyorMatchDurabilityPayload.ID, (payload, context) -> {
            ConveyorFilterAccess blockEntity = getConveyorFilterAccess(context.player());
            if (blockEntity != null) {
                blockEntity.setMatchDurability(payload.matchDurability());
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(SetConveyorMatchEnchantmentsPayload.ID, (payload, context) -> {
            ConveyorFilterAccess blockEntity = getConveyorFilterAccess(context.player());
            if (blockEntity != null) {
                blockEntity.setMatchEnchantments(payload.matchEnchantments());
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(SetConveyorMatchComponentsPayload.ID, (payload, context) -> {
            ConveyorFilterAccess blockEntity = getConveyorFilterAccess(context.player());
            if (blockEntity != null) {
                blockEntity.setMatchComponents(payload.matchComponents());
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(SetConveyorFilterTagPayload.ID, (payload, context) -> {
            ConveyorFilterAccess blockEntity = getConveyorFilterAccess(context.player());
            if (blockEntity != null) {
                blockEntity.setFilterTag(payload.filterTag());
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(SetConveyorTagFilteringPayload.ID, (payload, context) -> {
            ConveyorFilterAccess blockEntity = getConveyorFilterAccess(context.player());
            if (blockEntity != null) {
                blockEntity.setTagFiltering(payload.tagFiltering());
            }
        });
    }

    private static ConveyorFilterAccess getConveyorFilterAccess(ServerPlayer player) {
        if (player.containerMenu instanceof FilterConveyorScreenHandler handler)
            return handler.getBlockEntity();

        if (player.containerMenu instanceof DetectorConveyorScreenHandler handler)
            return handler.getBlockEntity();

        return null;
    }
}
