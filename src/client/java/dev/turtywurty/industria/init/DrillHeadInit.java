package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.model.SimpleDrillHeadModel;
import dev.turtywurty.industria.registry.DrillHeadRegistry;

public class DrillHeadInit {
    public static void init() {
        DrillHeadRegistry.register(ItemInit.SIMPLE_DRILL_HEAD, DrillHeadRegistry.DrillHeadClientData.create(
                either -> new SimpleDrillHeadModel(either.map(ctx ->
                                ctx.bakeLayer(SimpleDrillHeadModel.LAYER_LOCATION),
                        loader -> loader.bakeLayer(SimpleDrillHeadModel.LAYER_LOCATION))),
                SimpleDrillHeadModel::onRender,
                Industria.id("textures/block/simple_drill_head.png")));

        DrillHeadRegistry.register(ItemInit.BLOCK_BUILDER_DRILL_HEAD, DrillHeadRegistry.DrillHeadClientData.create(
                either -> new SimpleDrillHeadModel(either.map(ctx ->
                                ctx.bakeLayer(SimpleDrillHeadModel.LAYER_LOCATION),
                        loader -> loader.bakeLayer(SimpleDrillHeadModel.LAYER_LOCATION))),
                SimpleDrillHeadModel::onRender,
                Industria.id("textures/block/simple_drill_head.png")));
    }
}
