package dev.turtywurty.industria.init;

import dev.turtywurty.industria.block.ConveyorBlock;
import dev.turtywurty.industria.model.conveyor.CornerTurnConveyorAnchorPositionsModel;
import dev.turtywurty.industria.model.conveyor.StraightConveyorAnchorPositionsModel;
import dev.turtywurty.industria.model.conveyor.VerticalDownConveyorAnchorPositionsModel;
import dev.turtywurty.industria.model.conveyor.VerticalUpConveyorAnchorPositionsModel;
import dev.turtywurty.industria.renderer.world.ConveyorNetworkLevelRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;

public final class ConveyorAnchorProviderInit {
    private ConveyorAnchorProviderInit() {
    }

    public static void init() {
        ConveyorNetworkLevelRenderer.registerAnchorProvider(BlockInit.CONVEYOR, blockState -> {
            EntityModelSet entityModels = Minecraft.getInstance().getEntityModels();
            return switch (blockState.getValue(ConveyorBlock.SHAPE)) {
                case STRAIGHT ->
                        new StraightConveyorAnchorPositionsModel(entityModels.bakeLayer(StraightConveyorAnchorPositionsModel.LAYER_LOCATION));
                case UP ->
                        new VerticalUpConveyorAnchorPositionsModel(entityModels.bakeLayer(VerticalUpConveyorAnchorPositionsModel.LAYER_LOCATION));
                case DOWN ->
                        new VerticalDownConveyorAnchorPositionsModel(entityModels.bakeLayer(VerticalDownConveyorAnchorPositionsModel.LAYER_LOCATION));
                case TURN_LEFT, TURN_RIGHT ->
                        new CornerTurnConveyorAnchorPositionsModel(entityModels.bakeLayer(CornerTurnConveyorAnchorPositionsModel.LAYER_LOCATION));
            };
        });
    }
}
