package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.model.*;
import dev.turtywurty.industria.renderer.item.DrillHeadItemRenderer;
import dev.turtywurty.industria.renderer.item.IndustriaBlockEntityItemRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.SpecialBlockRendererRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.render.item.model.special.SpecialModelTypes;
import net.minecraft.item.Item;

import java.util.HashMap;
import java.util.Map;

public class DynamicItemRendererInit {
    private static final Map<Item, IndustriaBlockEntityItemRenderer.Unbaked> BLOCK_ENTITY_ITEM_RENDERERS = new HashMap<>();

    public static void init() {
        SpecialModelTypes.ID_MAPPER.put(Industria.id("drill_head"), DrillHeadItemRenderer.Unbaked.CODEC);
//        ResourceLoader.get(ResourceType.CLIENT_RESOURCES).registerReloader(Industria.id("drill_head_item_renderer"),
//                (store, prepareExecutor, reloadSynchronizer, applyExecutor) ->
//                        CompletableFuture.runAsync(() -> {
//                            DrillHeadItemRenderer.INSTANCE.drillHeadModels.clear();
//                            DrillHeadItemRenderer.INSTANCE.drillHeadTextures.clear();
//                        }, applyExecutor));

        registerItemRenderer(BlockInit.WIND_TURBINE,
                new IndustriaBlockEntityItemRenderer.Unbaked(WindTurbineModel.LAYER_LOCATION, WindTurbineModel.TEXTURE_LOCATION));
        registerItemRenderer(BlockInit.OIL_PUMP_JACK,
                new IndustriaBlockEntityItemRenderer.Unbaked(OilPumpJackModel.LAYER_LOCATION, OilPumpJackModel.TEXTURE_LOCATION));
        registerItemRenderer(BlockInit.DRILL,
                new IndustriaBlockEntityItemRenderer.Unbaked(DrillFrameModel.LAYER_LOCATION, DrillFrameModel.TEXTURE_LOCATION));
        registerItemRenderer(BlockInit.MOTOR,
                new IndustriaBlockEntityItemRenderer.Unbaked(MotorModel.LAYER_LOCATION, MotorModel.TEXTURE_LOCATION));
        registerItemRenderer(BlockInit.UPGRADE_STATION,
                new IndustriaBlockEntityItemRenderer.Unbaked(UpgradeStationModel.LAYER_LOCATION, UpgradeStationModel.TEXTURE_LOCATION));
        registerItemRenderer(BlockInit.MIXER,
                new IndustriaBlockEntityItemRenderer.Unbaked(MixerModel.LAYER_LOCATION, MixerModel.TEXTURE_LOCATION));
        registerItemRenderer(BlockInit.DIGESTER,
                new IndustriaBlockEntityItemRenderer.Unbaked(DigesterModel.LAYER_LOCATION, DigesterModel.TEXTURE_LOCATION));
        registerItemRenderer(BlockInit.CLARIFIER,
                new IndustriaBlockEntityItemRenderer.Unbaked(ClarifierModel.LAYER_LOCATION, ClarifierModel.TEXTURE_LOCATION));
        registerItemRenderer(BlockInit.CRYSTALLIZER,
                new IndustriaBlockEntityItemRenderer.Unbaked(CrystallizerModel.LAYER_LOCATION, CrystallizerModel.TEXTURE_LOCATION));
//        registerItemRenderer(ItemInit.ROTARY_KILN,
//                new IndustriaBlockEntityItemRenderer.Unbaked(RotaryKilnModel.LAYER_LOCATION, RotaryKilnModel.TEXTURE_LOCATION));
    }

    private static void registerItemRenderer(Block block, IndustriaBlockEntityItemRenderer.Unbaked renderer) {
        Item item = block.asItem();
        if (item != null) {
            BLOCK_ENTITY_ITEM_RENDERERS.put(item, renderer);
            SpecialBlockRendererRegistry.register(block, renderer);
            Industria.LOGGER.info("Registered item renderer for block: {}", block);
        } else {
            Industria.LOGGER.warn("Failed to register item renderer for block: {}", block);
        }
    }

    public static IndustriaBlockEntityItemRenderer.Unbaked getItemRenderer(Item item) {
        return BLOCK_ENTITY_ITEM_RENDERERS.get(item);
    }
}
