package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.screenhandler.*;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;

public class ScreenHandlerTypeInit {
    public static final ScreenHandlerType<AlloyFurnaceScreenHandler> ALLOY_FURNACE =
            register("alloy_furnace", AlloyFurnaceScreenHandler::new, BlockPosPayload.CODEC);

    public static final ScreenHandlerType<ThermalGeneratorScreenHandler> THERMAL_GENERATOR =
            register("thermal_generator", ThermalGeneratorScreenHandler::new, BlockPosPayload.CODEC);

    public static final ScreenHandlerType<BatteryScreenHandler> BATTERY =
            register("battery", BatteryScreenHandler::new, BlockPosPayload.CODEC);

    public static final ScreenHandlerType<CombustionGeneratorScreenHandler> COMBUSTION_GENERATOR =
            register("combustion_generator", CombustionGeneratorScreenHandler::new, BlockPosPayload.CODEC);

    public static final ScreenHandlerType<SolarPanelScreenHandler> SOLAR_PANEL =
            register("solar_panel", SolarPanelScreenHandler::new, BlockPosPayload.CODEC);

    public static final ScreenHandlerType<CrusherScreenHandler> CRUSHER =
            register("crusher", CrusherScreenHandler::new, BlockPosPayload.CODEC);

    public static final ScreenHandlerType<WindTurbineScreenHandler> WIND_TURBINE =
            register("wind_turbine", WindTurbineScreenHandler::new, BlockPosPayload.CODEC);

    public static <T extends ScreenHandler, D extends CustomPayload> ExtendedScreenHandlerType<T, D> register(String name, ExtendedScreenHandlerType.ExtendedFactory<T, D> factory, PacketCodec<? super RegistryByteBuf, D> codec) {
        return Registry.register(Registries.SCREEN_HANDLER, Industria.id(name), new ExtendedScreenHandlerType<>(factory, codec));
    }

    public static void init() {}
}
