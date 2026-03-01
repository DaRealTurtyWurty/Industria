package dev.turtywurty.industria.init;

import dev.turtywurty.industria.conveyor.block.impl.BasicConveyorBlock;
import dev.turtywurty.industria.conveyor.block.impl.MergerConveyorBlock;
import dev.turtywurty.industria.conveyor.block.impl.SplitterConveyorBlock;
import dev.turtywurty.industria.model.conveyor.*;
import dev.turtywurty.industria.renderer.world.ConveyorNetworkLevelRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;

import java.util.Map;

public final class ConveyorAnchorProviderInit {
    private ConveyorAnchorProviderInit() {
    }

    public static void init() {
        ConveyorNetworkLevelRenderer.registerAnchorProvider(BlockInit.CONVEYOR, blockState -> {
            EntityModelSet entityModels = Minecraft.getInstance().getEntityModels();
            return Map.of(ConveyorNetworkLevelRenderer.DEFAULT_ANCHOR_ROUTE, switch (blockState.getValue(BasicConveyorBlock.SHAPE)) {
                case STRAIGHT ->
                        new StraightConveyorAnchorPositionsModel(entityModels.bakeLayer(StraightConveyorAnchorPositionsModel.LAYER_LOCATION));
                case UP ->
                        new VerticalUpConveyorAnchorPositionsModel(entityModels.bakeLayer(VerticalUpConveyorAnchorPositionsModel.LAYER_LOCATION));
                case DOWN ->
                        new VerticalDownConveyorAnchorPositionsModel(entityModels.bakeLayer(VerticalDownConveyorAnchorPositionsModel.LAYER_LOCATION));
                case TURN_LEFT, TURN_RIGHT ->
                        new CornerTurnConveyorAnchorPositionsModel(entityModels.bakeLayer(CornerTurnConveyorAnchorPositionsModel.LAYER_LOCATION));
            });
        });

        ConveyorNetworkLevelRenderer.registerAnchorProvider(BlockInit.SPLITTER_CONVEYOR, blockState -> {
            EntityModelSet entityModels = Minecraft.getInstance().getEntityModels();
            return Map.of(
                    SplitterConveyorBlock.LEFT_OUTPUT_ID,
                    new SplitterConveyorAnchorPositionsModel(entityModels.bakeLayer(SplitterConveyorAnchorPositionsModel.LEFT_LAYER_LOCATION)),
                    SplitterConveyorBlock.RIGHT_OUTPUT_ID,
                    new SplitterConveyorAnchorPositionsModel(entityModels.bakeLayer(SplitterConveyorAnchorPositionsModel.RIGHT_LAYER_LOCATION))
            );
        });

        ConveyorNetworkLevelRenderer.registerAnchorProvider(BlockInit.MERGER_CONVEYOR, blockState -> {
            EntityModelSet entityModels = Minecraft.getInstance().getEntityModels();
            return Map.of(
                    MergerConveyorBlock.LEFT_INPUT_ID,
                    new MergerConveyorAnchorPositionsModel(entityModels.bakeLayer(MergerConveyorAnchorPositionsModel.LEFT_LAYER_LOCATION)),
                    MergerConveyorBlock.RIGHT_INPUT_ID,
                    new MergerConveyorAnchorPositionsModel(entityModels.bakeLayer(MergerConveyorAnchorPositionsModel.RIGHT_LAYER_LOCATION))
            );
        });
    }
}
