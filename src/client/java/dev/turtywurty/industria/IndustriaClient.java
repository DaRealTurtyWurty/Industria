package dev.turtywurty.industria;

import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.FluidInit;
import dev.turtywurty.industria.init.ScreenHandlerTypeInit;
import dev.turtywurty.industria.model.CrusherModel;
import dev.turtywurty.industria.model.OilPumpJackModel;
import dev.turtywurty.industria.model.WindTurbineModel;
import dev.turtywurty.industria.renderer.CrusherBlockEntityRenderer;
import dev.turtywurty.industria.renderer.OilPumpJackBlockEntityRenderer;
import dev.turtywurty.industria.renderer.WindTurbineBlockEntityRenderer;
import dev.turtywurty.industria.screen.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

public class IndustriaClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// Registering Screens
		HandledScreens.register(ScreenHandlerTypeInit.ALLOY_FURNACE, AlloyFurnaceScreen::new);
		HandledScreens.register(ScreenHandlerTypeInit.THERMAL_GENERATOR, ThermalGeneratorScreen::new);
		HandledScreens.register(ScreenHandlerTypeInit.BATTERY, BatteryScreen::new);
		HandledScreens.register(ScreenHandlerTypeInit.COMBUSTION_GENERATOR, CombustionGeneratorScreen::new);
		HandledScreens.register(ScreenHandlerTypeInit.SOLAR_PANEL, SolarPanelScreen::new);
		HandledScreens.register(ScreenHandlerTypeInit.CRUSHER, CrusherScreen::new);
		HandledScreens.register(ScreenHandlerTypeInit.WIND_TURBINE, WindTurbineScreen::new);
		HandledScreens.register(ScreenHandlerTypeInit.OIL_PUMP_JACK, OilPumpJackScreen::new);

		// Registering Models
		EntityModelLayerRegistry.registerModelLayer(CrusherModel.LAYER_LOCATION, CrusherModel::createMainLayer);
		EntityModelLayerRegistry.registerModelLayer(WindTurbineModel.LAYER_LOCATION, WindTurbineModel::createMainLayer);
		EntityModelLayerRegistry.registerModelLayer(OilPumpJackModel.LAYER_LOCATION, OilPumpJackModel::createMainLayer);

		// Registering Block Entity Renderers
		BlockEntityRendererFactories.register(BlockEntityTypeInit.CRUSHER, CrusherBlockEntityRenderer::new);
		BlockEntityRendererFactories.register(BlockEntityTypeInit.WIND_TURBINE, WindTurbineBlockEntityRenderer::new);
		BlockEntityRendererFactories.register(BlockEntityTypeInit.OIL_PUMP_JACK, OilPumpJackBlockEntityRenderer::new);

		// Register Fluid Renderers
		FluidRenderHandlerRegistry.INSTANCE.register(FluidInit.CRUDE_OIL, FluidInit.CRUDE_OIL_FLOWING,
				new SimpleFluidRenderHandler(Industria.id("block/crude_oil_still"), Industria.id("block/crude_oil_flow")));

		// Add to render layer map
		BlockRenderLayerMap.INSTANCE.putFluids(RenderLayer.getTranslucent(), FluidInit.CRUDE_OIL, FluidInit.CRUDE_OIL_FLOWING);
	}
}