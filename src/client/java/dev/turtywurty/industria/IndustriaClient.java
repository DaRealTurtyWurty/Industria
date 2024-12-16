package dev.turtywurty.industria;

import dev.turtywurty.industria.block.MultiblockBlock;
import dev.turtywurty.industria.init.*;
import dev.turtywurty.industria.model.*;
import dev.turtywurty.industria.network.OpenSeismicScannerPayload;
import dev.turtywurty.industria.network.SyncFluidPocketsPayload;
import dev.turtywurty.industria.network.UpgradeStationUpdateRecipesPayload;
import dev.turtywurty.industria.persistent.WorldFluidPocketsState;
import dev.turtywurty.industria.registry.ArmPositionRegistry;
import dev.turtywurty.industria.registry.DrillHeadRegistry;
import dev.turtywurty.industria.renderer.block.*;
import dev.turtywurty.industria.renderer.item.IndustriaDynamicItemRenderer;
import dev.turtywurty.industria.screen.*;
import dev.turtywurty.industria.screenhandler.UpgradeStationScreenHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.ParticleUtil;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IndustriaClient implements ClientModInitializer {
    private static final Map<RegistryKey<World>, List<WorldFluidPocketsState.FluidPocket>> FLUID_POCKETS = new HashMap<>();

    public static final Identifier SEISMIC_SCANNER = Industria.id("item/seismic_scanner_model");

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
        HandledScreens.register(ScreenHandlerTypeInit.DRILL, DrillScreen::new);
        HandledScreens.register(ScreenHandlerTypeInit.MOTOR, MotorScreen::new);
        HandledScreens.register(ScreenHandlerTypeInit.UPGRADE_STATION, UpgradeStationScreen::new);
        HandledScreens.register(ScreenHandlerTypeInit.ELECTRIC_FURNACE, ElectricFurnaceScreen::new);
        HandledScreens.register(ScreenHandlerTypeInit.FRACTIONAL_DISTILLATION_CONTROLLER, FractionalDistillationControllerScreen::new);
        HandledScreens.register(ScreenHandlerTypeInit.INDUCTION_HEATER, InductionHeaterScreen::new);
        HandledScreens.register(ScreenHandlerTypeInit.FLUID_PUMP, FluidPumpScreen::new);

        // Registering Models
        EntityModelLayerRegistry.registerModelLayer(CrusherModel.LAYER_LOCATION, CrusherModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(WindTurbineModel.LAYER_LOCATION, WindTurbineModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(OilPumpJackModel.LAYER_LOCATION, OilPumpJackModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(DrillFrameModel.LAYER_LOCATION, DrillFrameModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(SimpleDrillHeadModel.LAYER_LOCATION, SimpleDrillHeadModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(MotorModel.LAYER_LOCATION, MotorModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(DrillMotorModel.LAYER_LOCATION, DrillMotorModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(DrillCableModel.LAYER_LOCATION, DrillCableModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(UpgradeStationModel.LAYER_LOCATION, UpgradeStationModel::getTexturedModelData);

        // Registering Block Entity Renderers
        BlockEntityRendererFactories.register(BlockEntityTypeInit.CRUSHER, CrusherBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(BlockEntityTypeInit.WIND_TURBINE, WindTurbineBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(BlockEntityTypeInit.OIL_PUMP_JACK, OilPumpJackBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(BlockEntityTypeInit.DRILL, DrillBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(BlockEntityTypeInit.MOTOR, MotorBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(BlockEntityTypeInit.UPGRADE_STATION, UpgradeStationBlockEntityRenderer::new);

        // Registering BuiltinModelItemRenderers
        BuiltinItemRendererRegistry.INSTANCE.register(BlockInit.WIND_TURBINE, IndustriaDynamicItemRenderer.INSTANCE);
        BuiltinItemRendererRegistry.INSTANCE.register(BlockInit.OIL_PUMP_JACK, IndustriaDynamicItemRenderer.INSTANCE);
        BuiltinItemRendererRegistry.INSTANCE.register(BlockInit.DRILL, IndustriaDynamicItemRenderer.INSTANCE);
        BuiltinItemRendererRegistry.INSTANCE.register(ItemInit.SEISMIC_SCANNER, IndustriaDynamicItemRenderer.INSTANCE);
        BuiltinItemRendererRegistry.INSTANCE.register(BlockInit.MOTOR, IndustriaDynamicItemRenderer.INSTANCE);
        BuiltinItemRendererRegistry.INSTANCE.register(ItemInit.SIMPLE_DRILL_HEAD, IndustriaDynamicItemRenderer.INSTANCE);
        BuiltinItemRendererRegistry.INSTANCE.register(ItemInit.BLOCK_BUILDER_DRILL_HEAD, IndustriaDynamicItemRenderer.INSTANCE);
        BuiltinItemRendererRegistry.INSTANCE.register(BlockInit.UPGRADE_STATION, IndustriaDynamicItemRenderer.INSTANCE);

        // Register Fluid Renderers
        FluidRenderHandlerRegistry.INSTANCE.register(FluidInit.CRUDE_OIL, FluidInit.CRUDE_OIL_FLOWING,
                new SimpleFluidRenderHandler(Industria.id("block/crude_oil_still"), Industria.id("block/crude_oil_flow")));

        // Add to render layer map
        BlockRenderLayerMap.INSTANCE.putFluids(RenderLayer.getTranslucent(), FluidInit.CRUDE_OIL, FluidInit.CRUDE_OIL_FLOWING);

        // Packets
        handlePackets();
        handleEvents();
        ModelLoadingPlugin.register(IndustriaClient::registerModels);
        registerArmPositions();
        registerDrillHeads();

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("resource_reload").executes(context -> {
                MinecraftClient.getInstance().reloadResourcesConcurrently();

                return 1;
            }));
        });
    }

    private static void handlePackets() {
        ClientPlayNetworking.registerGlobalReceiver(OpenSeismicScannerPayload.ID, (payload, context) ->
                context.client().execute(() ->
                        context.client().setScreen(new SeismicScannerScreen(payload.stack()))));

        ClientPlayNetworking.registerGlobalReceiver(SyncFluidPocketsPayload.ID, (payload, context) -> {
            RegistryKey<World> worldKey = context.player().getEntityWorld().getRegistryKey();
            FLUID_POCKETS.put(worldKey, payload.fluidPockets());
        });

        ClientPlayNetworking.registerGlobalReceiver(UpgradeStationUpdateRecipesPayload.ID, (payload, context) -> {
            if (context.player().currentScreenHandler instanceof UpgradeStationScreenHandler handler) {
                handler.setAvailableRecipes(payload.recipes());
            }
        });
    }

    private static void handleEvents() {
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            PlayerEntity player = MinecraftClient.getInstance().player;
            if (player == null)
                return;

            if (!FLUID_POCKETS.containsKey(player.getEntityWorld().getRegistryKey()))
                return;

            List<WorldFluidPocketsState.FluidPocket> nearbyFluidPockets = FLUID_POCKETS.get(player.getEntityWorld().getRegistryKey())
                    .stream()
                    .filter(fluidPocket -> fluidPocket.isWithinDistance(player.getBlockPos(), 64))
                    .toList();

            MatrixStack matrixStack = context.matrixStack();
            if (matrixStack == null)
                return;

            VertexConsumerProvider provider = context.consumers();
            if (provider == null)
                return;

            World world = player.getWorld();
            if (world == null)
                return;

            for (WorldFluidPocketsState.FluidPocket pocket : nearbyFluidPockets) {
                // TODO: Draw different colored particles based on fluid

                for (BlockPos pos : pocket.fluidPositions()) {
                    ParticleUtil.spawnParticlesAround(world, pos, 1, ParticleTypes.DRIPPING_WATER);
                }
            }
        });

        WorldRenderEvents.BLOCK_OUTLINE.register((worldRenderContext, blockOutlineContext) -> {
            World world = worldRenderContext.world();
            BlockPos blockPos = blockOutlineContext.blockPos();

            BlockState state = blockOutlineContext.blockState();
            if (state.getBlock() instanceof MultiblockBlock multiblockBlock) {

            }

            return true;
        });
    }

    private static void registerModels(ModelLoadingPlugin.Context context) {
        context.addModels(SEISMIC_SCANNER);
    }

    private static void registerArmPositions() {
        ArmPositionRegistry.register(stack -> stack.isOf(ItemInit.SEISMIC_SCANNER),
                (state, leftArm, rightArm) -> {
                    leftArm.hidden = false;
                    rightArm.hidden = false;

                    leftArm.pitch = (float) Math.toRadians(-30F);
                    leftArm.yaw = (float) Math.toRadians(20F);

                    rightArm.pitch = (float) Math.toRadians(-30F);
                    rightArm.yaw = (float) Math.toRadians(-20F);
                });
    }

    private static void registerDrillHeads() {
        DrillHeadRegistry.register(ItemInit.SIMPLE_DRILL_HEAD, DrillHeadRegistry.DrillHeadClientData.create(
                either -> new SimpleDrillHeadModel(either.map(ctx ->
                                ctx.getLayerModelPart(SimpleDrillHeadModel.LAYER_LOCATION),
                        loader -> loader.getModelPart(SimpleDrillHeadModel.LAYER_LOCATION))),
                SimpleDrillHeadModel::onRender,
                Industria.id("textures/block/simple_drill_head.png")));

        DrillHeadRegistry.register(ItemInit.BLOCK_BUILDER_DRILL_HEAD, DrillHeadRegistry.DrillHeadClientData.create(
                either -> new SimpleDrillHeadModel(either.map(ctx ->
                                ctx.getLayerModelPart(SimpleDrillHeadModel.LAYER_LOCATION),
                        loader -> loader.getModelPart(SimpleDrillHeadModel.LAYER_LOCATION))),
                SimpleDrillHeadModel::onRender,
                Industria.id("textures/block/simple_drill_head.png")));
    }
}