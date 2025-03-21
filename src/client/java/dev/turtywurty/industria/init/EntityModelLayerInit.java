package dev.turtywurty.industria.init;

import dev.turtywurty.industria.model.*;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;

public class EntityModelLayerInit {
    public static void init() {
        EntityModelLayerRegistry.registerModelLayer(CrusherModel.LAYER_LOCATION, CrusherModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(WindTurbineModel.LAYER_LOCATION, WindTurbineModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(OilPumpJackModel.LAYER_LOCATION, OilPumpJackModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(DrillFrameModel.LAYER_LOCATION, DrillFrameModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(SimpleDrillHeadModel.LAYER_LOCATION, SimpleDrillHeadModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(MotorModel.LAYER_LOCATION, MotorModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(DrillMotorModel.LAYER_LOCATION, DrillMotorModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(DrillCableModel.LAYER_LOCATION, DrillCableModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(UpgradeStationModel.LAYER_LOCATION, UpgradeStationModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(MixerModel.LAYER_LOCATION, MixerModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(DigesterModel.LAYER_LOCATION, DigesterModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(ClarifierModel.LAYER_LOCATION, ClarifierModel::getTexturedModelData);
    }
}
