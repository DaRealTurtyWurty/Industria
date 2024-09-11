package dev.turtywurty.industria;

import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.FluidInit;
import dev.turtywurty.industria.init.ScreenHandlerTypeInit;
import dev.turtywurty.industria.model.CrusherModel;
import dev.turtywurty.industria.model.OilPumpJackModel;
import dev.turtywurty.industria.model.WindTurbineModel;
import dev.turtywurty.industria.network.OpenSeismicScannerPayload;
import dev.turtywurty.industria.network.SyncFluidMapPayload;
import dev.turtywurty.industria.renderer.CrusherBlockEntityRenderer;
import dev.turtywurty.industria.renderer.IndustriaDynamicItemRenderer;
import dev.turtywurty.industria.renderer.OilPumpJackBlockEntityRenderer;
import dev.turtywurty.industria.renderer.WindTurbineBlockEntityRenderer;
import dev.turtywurty.industria.screen.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.ParticleUtil;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.ResultHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IndustriaClient implements ClientModInitializer {
    private static final Map<ChunkPos, Map<BlockPos, FluidState>> FLUID_MAP = new HashMap<>();

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

        // Registering BuiltinModelItemRenderers
        BuiltinItemRendererRegistry.INSTANCE.register(BlockInit.WIND_TURBINE, IndustriaDynamicItemRenderer.INSTANCE);
        BuiltinItemRendererRegistry.INSTANCE.register(BlockInit.OIL_PUMP_JACK, IndustriaDynamicItemRenderer.INSTANCE);

        // Register Fluid Renderers
        FluidRenderHandlerRegistry.INSTANCE.register(FluidInit.CRUDE_OIL, FluidInit.CRUDE_OIL_FLOWING,
                new SimpleFluidRenderHandler(Industria.id("block/crude_oil_still"), Industria.id("block/crude_oil_flow")));

        // Add to render layer map
        BlockRenderLayerMap.INSTANCE.putFluids(RenderLayer.getTranslucent(), FluidInit.CRUDE_OIL, FluidInit.CRUDE_OIL_FLOWING);

        // Packets
        ClientPlayNetworking.registerGlobalReceiver(OpenSeismicScannerPayload.ID, (payload, context) ->
                context.client().execute(() ->
                        context.client().setScreen(new SeismicScannerScreen(payload.stack()))));

        ClientPlayNetworking.registerGlobalReceiver(SyncFluidMapPayload.ID, (payload, context) -> {
            var chunkPos = new ChunkPos(payload.pos());
            var fluidMap = new HashMap<BlockPos, FluidState>();
            payload.fluidMap().forEach((posStr, state) -> {
                String[] posArr = posStr.split(",");
                var pos = new BlockPos(Integer.parseInt(posArr[0].strip()),
                        Integer.parseInt(posArr[1].strip()),
                        Integer.parseInt(posArr[2].strip()));

                fluidMap.put(pos, state);
            });

            FLUID_MAP.put(chunkPos, fluidMap);
        });

        // Client Commands
        // registerDevCommands();

        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            PlayerEntity player = MinecraftClient.getInstance().player;
            if (player == null)
                return;

            List<ChunkPos> nearbyChunks = new ArrayList<>();
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    nearbyChunks.add(new ChunkPos(player.getBlockPos().add(x * 16, 0, z * 16)));
                }
            }

            MatrixStack matrixStack = context.matrixStack();
            if (matrixStack == null)
                return;

            VertexConsumerProvider provider = context.consumers();
            if (provider == null)
                return;

            World world = player.getWorld();
            if (world == null)
                return;

            nearbyChunks.stream()
                    .filter(FLUID_MAP::containsKey)
                    .map(FLUID_MAP::get)
                    .filter(map -> map != null && !map.isEmpty())
                    .forEach(fluidMap -> {
                        for (Map.Entry<BlockPos, FluidState> entry : fluidMap.entrySet()) {
                            FluidState state = entry.getValue();
                            if (state.isEmpty())
                                continue;

                            BlockPos pos = entry.getKey();
                            ParticleUtil.spawnParticlesAround(world, pos, 1, ParticleTypes.DRIPPING_WATER);
                        }
                    });
        });
    }

    private static void registerDevCommands() {
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
                    ClientCommandManager.literal(Industria.MOD_ID).then(ClientCommandManager.literal("reload").executes(context -> {
                        Industria.LOGGER.info("Reloading Industria...");

                        new Thread(() -> {
                            var connector = GradleConnector.newConnector();
                            connector.forProjectDirectory(FabricLoader.getInstance().getGameDir().getParent().toFile());
                            try (ProjectConnection connection = connector.connect()) {
                                connection.newBuild()
                                        .forTasks("runDatagen", "build")
                                        .run(new ResultHandler<>() {
                                            @Override
                                            public void onComplete(Void result) {
                                                MinecraftClient.getInstance().execute(() ->
                                                        MinecraftClient.getInstance().reloadResources().thenAccept(ignored ->
                                                                context.getSource().sendFeedback(Text.literal("Reloaded Industria!"))));
                                            }

                                            @Override
                                            public void onFailure(GradleConnectionException failure) {
                                                MinecraftClient.getInstance().execute(() ->
                                                        context.getSource().sendError(Text.literal("Failed to reload Industria!")));
                                            }
                                        });
                            }
                        }).start();

                        return 1;
                    }))));
        }
    }
}