package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.model.*;
import dev.turtywurty.industria.renderer.item.DrillHeadItemRenderer;
import dev.turtywurty.industria.renderer.item.IndustriaBlockEntityItemRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.SpecialBlockRendererRegistry;
import net.minecraft.client.render.item.model.special.SpecialModelTypes;

public class DynamicItemRendererInit {
    public static void init() {
        SpecialModelTypes.ID_MAPPER.put(Industria.id("drill_head"), DrillHeadItemRenderer.Unbaked.CODEC);

        SpecialBlockRendererRegistry.register(BlockInit.WIND_TURBINE,
                new IndustriaBlockEntityItemRenderer.Unbaked(WindTurbineModel.LAYER_LOCATION, WindTurbineModel.TEXTURE_LOCATION));
        SpecialBlockRendererRegistry.register(BlockInit.OIL_PUMP_JACK,
                new IndustriaBlockEntityItemRenderer.Unbaked(OilPumpJackModel.LAYER_LOCATION, OilPumpJackModel.TEXTURE_LOCATION));
        SpecialBlockRendererRegistry.register(BlockInit.DRILL,
                new IndustriaBlockEntityItemRenderer.Unbaked(DrillFrameModel.LAYER_LOCATION, DrillFrameModel.TEXTURE_LOCATION));
        SpecialBlockRendererRegistry.register(BlockInit.MOTOR,
                new IndustriaBlockEntityItemRenderer.Unbaked(MotorModel.LAYER_LOCATION, MotorModel.TEXTURE_LOCATION));
        SpecialBlockRendererRegistry.register(BlockInit.UPGRADE_STATION,
                new IndustriaBlockEntityItemRenderer.Unbaked(UpgradeStationModel.LAYER_LOCATION, UpgradeStationModel.TEXTURE_LOCATION));
        SpecialBlockRendererRegistry.register(BlockInit.MIXER,
                new IndustriaBlockEntityItemRenderer.Unbaked(MixerModel.LAYER_LOCATION, MixerModel.TEXTURE_LOCATION));
        SpecialBlockRendererRegistry.register(BlockInit.DIGESTER,
                new IndustriaBlockEntityItemRenderer.Unbaked(DigesterModel.LAYER_LOCATION, DigesterModel.TEXTURE_LOCATION));
        SpecialBlockRendererRegistry.register(BlockInit.CLARIFIER,
                new IndustriaBlockEntityItemRenderer.Unbaked(ClarifierModel.LAYER_LOCATION, ClarifierModel.TEXTURE_LOCATION));
        SpecialBlockRendererRegistry.register(BlockInit.CRYSTALLIZER,
                new IndustriaBlockEntityItemRenderer.Unbaked(CrystallizerModel.LAYER_LOCATION, CrystallizerModel.TEXTURE_LOCATION));
//        SpecialBlockRendererRegistry.register(ItemInit.ROTARY_KILN,
//                new IndustriaBlockEntityItemRenderer.Unbaked(RotaryKilnModel.LAYER_LOCATION, RotaryKilnModel.TEXTURE_LOCATION));
    }
}
