package dev.turtywurty.industria;

import dev.turtywurty.industria.blockentity.*;
import dev.turtywurty.industria.init.*;
import dev.turtywurty.industria.init.worldgen.BiomeModificationInit;
import dev.turtywurty.industria.network.BatteryChargeModePayload;
import dev.turtywurty.industria.screenhandler.BatteryScreenHandler;
import dev.turtywurty.industria.util.ExtraPacketCodecs;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import team.reborn.energy.api.EnergyStorage;

public class Industria implements ModInitializer {
    public static final String MOD_ID = "industria";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    public static Text containerTitle(String name) {
        return Text.translatable("container." + MOD_ID + "." + name);
    }

    @Override
    public void onInitialize() {
        LOGGER.info("Loading Industria...");

        ItemInit.init();
        BlockInit.init();
        BlockEntityTypeInit.init();
        ScreenHandlerTypeInit.init();
        RecipeTypeInit.init();
        RecipeSerializerInit.init();
        ItemGroupInit.init();
        BiomeModificationInit.init();

        ExtraPacketCodecs.registerDefaults();

        ItemStorage.SIDED.registerForBlockEntity(AlloyFurnaceBlockEntity::getInventoryProvider, BlockEntityTypeInit.ALLOY_FURNACE);

        EnergyStorage.SIDED.registerForBlockEntity(ThermalGeneratorBlockEntity::getEnergyProvider, BlockEntityTypeInit.THERMAL_GENERATOR);
        FluidStorage.SIDED.registerForBlockEntity(ThermalGeneratorBlockEntity::getFluidProvider, BlockEntityTypeInit.THERMAL_GENERATOR);
        ItemStorage.SIDED.registerForBlockEntity(ThermalGeneratorBlockEntity::getInventoryProvider, BlockEntityTypeInit.THERMAL_GENERATOR);

        EnergyStorage.SIDED.registerForBlockEntity(BatteryBlockEntity::getEnergyProvider, BlockEntityTypeInit.BATTERY);
        ItemStorage.SIDED.registerForBlockEntity(BatteryBlockEntity::getInventoryProvider, BlockEntityTypeInit.BATTERY);

        EnergyStorage.SIDED.registerForBlockEntity(CombustionGeneratorBlockEntity::getEnergyProvider, BlockEntityTypeInit.COMBUSTION_GENERATOR);
        ItemStorage.SIDED.registerForBlockEntity(CombustionGeneratorBlockEntity::getInventoryProvider, BlockEntityTypeInit.COMBUSTION_GENERATOR);

        EnergyStorage.SIDED.registerForBlockEntity(SolarPanelBlockEntity::getEnergyProvider, BlockEntityTypeInit.SOLAR_PANEL);

        EnergyStorage.SIDED.registerForBlockEntity(CrusherBlockEntity::getEnergyProvider, BlockEntityTypeInit.CRUSHER);
        ItemStorage.SIDED.registerForBlockEntity(CrusherBlockEntity::getInventoryProvider, BlockEntityTypeInit.CRUSHER);

        EnergyStorage.SIDED.registerForBlockEntity(CableBlockEntity::getEnergyProvider, BlockEntityTypeInit.CABLE);

        EnergyStorage.SIDED.registerForBlockEntity(WindTurbineBlockEntity::getEnergyProvider, BlockEntityTypeInit.WIND_TURBINE);

        PayloadTypeRegistry.playC2S().register(BatteryChargeModePayload.ID, BatteryChargeModePayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(BatteryChargeModePayload.ID, (payload, context) ->
                context.server().execute(() -> {
                    ServerPlayerEntity player = context.player();
                    ScreenHandler handler = player.currentScreenHandler;
                    if (handler instanceof BatteryScreenHandler batteryScreenHandler) {
                        batteryScreenHandler.getBlockEntity().setChargeMode(payload.chargeMode());
                    }
                }));

        LOGGER.info("Industria has finished loading!");
    }
}