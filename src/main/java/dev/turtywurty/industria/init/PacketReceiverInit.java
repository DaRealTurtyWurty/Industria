package dev.turtywurty.industria.init;

import dev.turtywurty.industria.blockentity.DrillBlockEntity;
import dev.turtywurty.industria.blockentity.MotorBlockEntity;
import dev.turtywurty.industria.blockentity.MultiblockDesignerBlockEntity;
import dev.turtywurty.industria.network.*;
import dev.turtywurty.industria.screenhandler.*;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public class PacketReceiverInit {
    public static void init() {
        ServerPlayNetworking.registerGlobalReceiver(BatteryChargeModePayload.ID, (payload, context) ->
                context.server().execute(() -> {
                    ServerPlayerEntity player = context.player();
                    ScreenHandler handler = player.currentScreenHandler;
                    if (handler instanceof BatteryScreenHandler batteryScreenHandler) {
                        batteryScreenHandler.getBlockEntity().setChargeMode(payload.chargeMode());
                    }
                }));

        ServerPlayNetworking.registerGlobalReceiver(ChangeDrillingPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            if (player.currentScreenHandler instanceof DrillScreenHandler handler) {
                DrillBlockEntity blockEntity = handler.getBlockEntity();
                blockEntity.setDrilling(payload.drilling());
                blockEntity.update();
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(RetractDrillPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            if (player.currentScreenHandler instanceof DrillScreenHandler handler) {
                DrillBlockEntity blockEntity = handler.getBlockEntity();
                blockEntity.setDrilling(false);
                blockEntity.setRetracting(true);
                blockEntity.update();
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(ChangeDrillOverflowModePayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            if (player.currentScreenHandler instanceof DrillScreenHandler handler) {
                DrillBlockEntity blockEntity = handler.getBlockEntity();
                blockEntity.setOverflowMethod(payload.overflowMethod());
                blockEntity.update();
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(SetMotorTargetRPMPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            if (player.currentScreenHandler instanceof MotorScreenHandler handler) {
                MotorBlockEntity blockEntity = handler.getBlockEntity();
                blockEntity.setTargetRotationSpeed(payload.targetRPM() / 60f);
                blockEntity.update();
            } else if (player.currentScreenHandler instanceof DrillScreenHandler handler) {
                DrillBlockEntity blockEntity = handler.getBlockEntity();
                blockEntity.setTargetRotationSpeed(payload.targetRPM() / 60f);
                blockEntity.update();
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(FluidTankChangeExtractModePayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            if (player.currentScreenHandler instanceof FluidTankScreenHandler handler) {
                handler.getBlockEntity().setExtractMode(payload.extractMode());
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(OilPumpJackSetRunningPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            if(player.currentScreenHandler instanceof OilPumpJackScreenHandler handler) {
                handler.getBlockEntity().setRunning(payload.isRunning());
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(SetMultiblockPieceCharPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            if (player.currentScreenHandler instanceof MultiblockDesignerScreenHandler handler) {
                MultiblockDesignerBlockEntity blockEntity = handler.getBlockEntity();
                blockEntity.setPaletteChar(payload.piecePos(), payload.paletteChar());
            }
        });
    }
}
