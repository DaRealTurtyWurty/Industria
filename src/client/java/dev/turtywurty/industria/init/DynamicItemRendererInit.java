package dev.turtywurty.industria.init;

import dev.turtywurty.industria.renderer.item.IndustriaDynamicItemRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;

public class DynamicItemRendererInit {
    public static void init() {
        BuiltinItemRendererRegistry.INSTANCE.register(BlockInit.WIND_TURBINE, IndustriaDynamicItemRenderer.INSTANCE);
        BuiltinItemRendererRegistry.INSTANCE.register(BlockInit.OIL_PUMP_JACK, IndustriaDynamicItemRenderer.INSTANCE);
        BuiltinItemRendererRegistry.INSTANCE.register(BlockInit.DRILL, IndustriaDynamicItemRenderer.INSTANCE);
        BuiltinItemRendererRegistry.INSTANCE.register(ItemInit.SEISMIC_SCANNER, IndustriaDynamicItemRenderer.INSTANCE);
        BuiltinItemRendererRegistry.INSTANCE.register(BlockInit.MOTOR, IndustriaDynamicItemRenderer.INSTANCE);
        BuiltinItemRendererRegistry.INSTANCE.register(ItemInit.SIMPLE_DRILL_HEAD, IndustriaDynamicItemRenderer.INSTANCE);
        BuiltinItemRendererRegistry.INSTANCE.register(ItemInit.BLOCK_BUILDER_DRILL_HEAD, IndustriaDynamicItemRenderer.INSTANCE);
        BuiltinItemRendererRegistry.INSTANCE.register(BlockInit.UPGRADE_STATION, IndustriaDynamicItemRenderer.INSTANCE);
        BuiltinItemRendererRegistry.INSTANCE.register(BlockInit.MIXER, IndustriaDynamicItemRenderer.INSTANCE);
        BuiltinItemRendererRegistry.INSTANCE.register(BlockInit.DIGESTER, IndustriaDynamicItemRenderer.INSTANCE);
        BuiltinItemRendererRegistry.INSTANCE.register(BlockInit.CLARIFIER, IndustriaDynamicItemRenderer.INSTANCE);
        BuiltinItemRendererRegistry.INSTANCE.register(BlockInit.CRYSTALLIZER, IndustriaDynamicItemRenderer.INSTANCE);
    }
}
