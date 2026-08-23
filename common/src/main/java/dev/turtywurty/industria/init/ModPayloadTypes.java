package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.blockentity.AgitatorBlockEntity;
import dev.turtywurty.industria.blockentity.DrillBlockEntity;
import dev.turtywurty.industria.blockentity.MotorBlockEntity;
import dev.turtywurty.industria.conveyor.block.impl.entity.ConveyorFilterAccess;
import dev.turtywurty.industria.menu.*;
import dev.turtywurty.industria.network.*;
import dev.turtywurty.industria.network.conveyor.*;
import dev.turtywurty.industria.network.pipe.AddPipeNetworkPayload;
import dev.turtywurty.industria.network.pipe.ModifyPipeNetworkPayload;
import dev.turtywurty.industria.network.pipe.RemovePipeNetworkPayload;
import dev.turtywurty.industria.network.pipe.SyncPipeNetworkManagerPayload;
import dev.turtywurty.turtymultiloader.network.NetworkService;
import dev.turtywurty.turtymultiloader.network.PayloadHandler;
import dev.turtywurty.turtymultiloader.network.PayloadRegistrationOptions;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class ModPayloadTypes {
    private static final NetworkService NETWORK = NetworkService.get();

    public static void init() {
        registerC2S();
        registerS2C();
    }

    private static void registerC2S() {
        registerServerbound(BatteryChargeModePayload.ID, BatteryChargeModePayload.CODEC, (payload, context) ->
                context.sender().ifPresent(player -> {
                    AbstractContainerMenu handler = player.containerMenu;
                    if (handler instanceof BatteryScreenHandler batteryScreenHandler) {
                        batteryScreenHandler.getBlockEntity().setChargeMode(payload.chargeMode());
                    }
                }));

        registerServerbound(ArcFurnaceSetModePayload.ID, ArcFurnaceSetModePayload.CODEC, (payload, context) ->
                context.sender().ifPresent(player -> {
                    AbstractContainerMenu handler = player.containerMenu;
                    if (handler instanceof ArcFurnaceScreenHandler arcFurnaceScreenHandler) {
                        arcFurnaceScreenHandler.getBlockEntity().setMode(payload.mode());
                    }
                }));

        registerServerbound(ChangeDrillingPayload.ID, ChangeDrillingPayload.CODEC, (payload, context) ->
                context.sender().ifPresent(player -> {
                    if (player.containerMenu instanceof DrillScreenHandler handler) {
                        DrillBlockEntity blockEntity = handler.getBlockEntity();
                        blockEntity.setDrilling(payload.drilling());
                        blockEntity.update();
                    }
                }));

        registerServerbound(RetractDrillPayload.ID, RetractDrillPayload.CODEC, (_, context) ->
                context.sender().ifPresent(player -> {
                    if (player.containerMenu instanceof DrillScreenHandler handler) {
                        DrillBlockEntity blockEntity = handler.getBlockEntity();
                        blockEntity.setDrilling(false);
                        blockEntity.setRetracting(true);
                        blockEntity.update();
                    }
                }));

        registerServerbound(ChangeDrillOverflowModePayload.ID, ChangeDrillOverflowModePayload.CODEC, (payload, context) ->
                context.sender().ifPresent(player -> {
                    if (player.containerMenu instanceof DrillScreenHandler handler) {
                        DrillBlockEntity blockEntity = handler.getBlockEntity();
                        blockEntity.setOverflowMethod(payload.overflowMethod());
                        blockEntity.update();
                    }
                }));

        registerServerbound(SetMotorTargetRPMPayload.ID, SetMotorTargetRPMPayload.CODEC, (payload, context) ->
                context.sender().ifPresent(player -> {
                    if (player.containerMenu instanceof MotorScreenHandler handler) {
                        MotorBlockEntity blockEntity = handler.getBlockEntity();
                        blockEntity.setTargetRotationSpeed(payload.targetRPM() / 60f);
                        blockEntity.update();
                    } else if (player.containerMenu instanceof DrillScreenHandler handler) {
                        DrillBlockEntity blockEntity = handler.getBlockEntity();
                        blockEntity.setTargetRotationSpeed(payload.targetRPM() / 60f);
                        blockEntity.update();
                    }
                }));

        registerServerbound(FluidTankChangeExtractModePayload.ID, FluidTankChangeExtractModePayload.CODEC, (payload, context) ->
                context.sender().ifPresent(player -> {
                    if (player.containerMenu instanceof FluidTankScreenHandler handler) {
                        handler.getBlockEntity().setExtractMode(payload.extractMode());
                    }
                }));

        registerServerbound(OilPumpJackSetRunningPayload.ID, OilPumpJackSetRunningPayload.CODEC, (payload, context) ->
                context.sender().ifPresent(player -> {
                    if (player.containerMenu instanceof OilPumpJackScreenHandler handler) {
                        handler.getBlockEntity().setRunning(payload.isRunning());
                    }
                }));

        registerServerbound(SetConveyorFilterStackPayload.ID, SetConveyorFilterStackPayload.CODEC, (payload, context) ->
                context.sender().ifPresent(player -> {
                    ConveyorFilterAccess blockEntity = getConveyorFilterAccess(player);
                    if (blockEntity != null) {
                        blockEntity.setFilterStack(payload.stack());
                    }
                }));

        registerServerbound(SetConveyorBlacklistModePayload.ID, SetConveyorBlacklistModePayload.CODEC, (payload, context) ->
                context.sender().ifPresent(player -> {
                    ConveyorFilterAccess blockEntity = getConveyorFilterAccess(player);
                    if (blockEntity != null) {
                        blockEntity.setBlacklistMode(payload.blacklistMode());
                    }
                }));

        registerServerbound(SetConveyorMatchDurabilityPayload.ID, SetConveyorMatchDurabilityPayload.CODEC, (payload, context) ->
                context.sender().ifPresent(player -> {
                    ConveyorFilterAccess blockEntity = getConveyorFilterAccess(player);
                    if (blockEntity != null) {
                        blockEntity.setMatchDurability(payload.matchDurability());
                    }
                }));

        registerServerbound(SetConveyorMatchEnchantmentsPayload.ID, SetConveyorMatchEnchantmentsPayload.CODEC, (payload, context) ->
                context.sender().ifPresent(player -> {
                    ConveyorFilterAccess blockEntity = getConveyorFilterAccess(player);
                    if (blockEntity != null) {
                        blockEntity.setMatchEnchantments(payload.matchEnchantments());
                    }
                }));

        registerServerbound(SetConveyorMatchComponentsPayload.ID, SetConveyorMatchComponentsPayload.CODEC, (payload, context) ->
                context.sender().ifPresent(player -> {
                    ConveyorFilterAccess blockEntity = getConveyorFilterAccess(player);
                    if (blockEntity != null) {
                        blockEntity.setMatchComponents(payload.matchComponents());
                    }
                }));

        registerServerbound(SetConveyorFilterTagPayload.ID, SetConveyorFilterTagPayload.CODEC, (payload, context) ->
                context.sender().ifPresent(player -> {
                    ConveyorFilterAccess blockEntity = getConveyorFilterAccess(player);
                    if (blockEntity != null) {
                        blockEntity.setFilterTag(payload.filterTag());
                    }
                }));

        registerServerbound(SetConveyorTagFilteringPayload.ID, SetConveyorTagFilteringPayload.CODEC, (payload, context) ->
                context.sender().ifPresent(player -> {
                    ConveyorFilterAccess blockEntity = getConveyorFilterAccess(player);
                    if (blockEntity != null) {
                        blockEntity.setTagFiltering(payload.tagFiltering());
                    }
                }));

        registerServerbound(AgitatorSetPortModePayload.ID, AgitatorSetPortModePayload.CODEC, (payload, context) ->
                context.sender().ifPresent(player -> {
                    if (player.containerMenu instanceof AgitatorScreenHandler handler) {
                        AgitatorBlockEntity blockEntity = handler.getBlockEntity();
                        if (payload.output()) {
                            blockEntity.setOutputMode(payload.index(), payload.portType());
                        } else {
                            blockEntity.setInputMode(payload.index(), payload.portType());
                        }
                    }
                }));
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

    private static <T extends CustomPacketPayload> void registerServerbound(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec, PayloadHandler<T> handler) {
        NETWORK.registerPlayServerbound(type, debugCodec(type, codec, "serverbound"), PayloadRegistrationOptions.DEFAULT, handler);
    }

    private static <T extends CustomPacketPayload> void registerClientbound(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
        NETWORK.registerPlayClientbound(type, debugCodec(type, codec, "clientbound"), PayloadRegistrationOptions.DEFAULT);
    }

    private static ConveyorFilterAccess getConveyorFilterAccess(ServerPlayer player) {
        if (player.containerMenu instanceof FilterConveyorScreenHandler handler)
            return handler.getBlockEntity();

        if (player.containerMenu instanceof DetectorConveyorScreenHandler handler)
            return handler.getBlockEntity();

        return null;
    }

    private static <T extends CustomPacketPayload> StreamCodec<RegistryFriendlyByteBuf, T> debugCodec(
            CustomPacketPayload.Type<T> id,
            StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
            String direction
    ) {
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
