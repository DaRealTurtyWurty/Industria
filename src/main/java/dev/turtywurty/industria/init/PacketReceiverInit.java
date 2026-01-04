package dev.turtywurty.industria.init;

import dev.turtywurty.industria.blockentity.DrillBlockEntity;
import dev.turtywurty.industria.blockentity.MotorBlockEntity;
import dev.turtywurty.industria.blockentity.MultiblockDesignerBlockEntity;
import dev.turtywurty.industria.network.*;
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

        ServerPlayNetworking.registerGlobalReceiver(SetMultiblockPieceCharPayload.ID, (payload, context) -> {
            ServerPlayer player = context.player();
            if (player.containerMenu instanceof MultiblockDesignerScreenHandler handler) {
                MultiblockDesignerBlockEntity blockEntity = handler.getBlockEntity();
                blockEntity.setPaletteChar(payload.piecePos(), payload.paletteChar());
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(UpdatePaletteEntryNamePayload.ID, (payload, context) -> {
            ServerPlayer player = context.player();
            if (player.containerMenu instanceof MultiblockDesignerScreenHandler handler) {
                handler.getBlockEntity().setPaletteName(payload.paletteChar(), payload.name());
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(DeletePaletteEntryPayload.ID, (payload, context) -> {
            ServerPlayer player = context.player();
            if (player.containerMenu instanceof MultiblockDesignerScreenHandler handler) {
                handler.getBlockEntity().removePiecesWithChar(payload.paletteChar());
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(UpdatePaletteEntryVariedBlockListPayload.ID, (payload, context) -> {
            ServerPlayer player = context.player();
            if (player.containerMenu instanceof MultiblockDesignerScreenHandler handler) {
                handler.getBlockEntity().setPaletteVariedBlockList(payload.paletteChar(), payload.variedBlockList());
            }
        });
    }
}
