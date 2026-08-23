package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.model.SimpleDrillHeadModel;
import dev.turtywurty.industria.registry.DrillHeadRegistry;

public class ModDrillHeads {
    public static void init() {
        DrillHeadRegistry.register(ModItems.SIMPLE_DRILL_HEAD.get(), DrillHeadRegistry.DrillHeadClientData.create(
                either -> new SimpleDrillHeadModel(either.map(ctx ->
                                ctx.bakeLayer(SimpleDrillHeadModel.LAYER_LOCATION),
                        loader -> loader.bakeLayer(SimpleDrillHeadModel.LAYER_LOCATION))),
                SimpleDrillHeadModel::onRender,
                Industria.id("textures/block/simple_drill_head.png")));

        DrillHeadRegistry.register(ModItems.BLOCK_BUILDER_DRILL_HEAD.get(), DrillHeadRegistry.DrillHeadClientData.create(
                either -> new SimpleDrillHeadModel(either.map(ctx ->
                                ctx.bakeLayer(SimpleDrillHeadModel.LAYER_LOCATION),
                        loader -> loader.bakeLayer(SimpleDrillHeadModel.LAYER_LOCATION))),
                SimpleDrillHeadModel::onRender,
                Industria.id("textures/block/simple_drill_head.png")));
    }
}
