package dev.turtywurty.industria.init;

import dev.turtywurty.industria.model.*;
import dev.turtywurty.industria.model.conveyor.ConveyorFlapsModel;
import dev.turtywurty.industria.model.conveyor.LadderConveyorPlatformModel;
import dev.turtywurty.industria.model.conveyor.LadderConveyorTopPlatformModel;
import dev.turtywurty.industria.model.conveyor.anchor.*;
import dev.turtywurty.turtymultiloader.client.registration.ClientRegistrations;

public class ModEntityModelLayers {
    public static void init() {
        ClientRegistrations.registerModelLayer(CrusherModel.LAYER_LOCATION, CrusherModel::getTexturedModelData);
        ClientRegistrations.registerModelLayer(AdvancedSolarPanelModel.LAYER_LOCATION, AdvancedSolarPanelModel::createMainLayer);
        ClientRegistrations.registerModelLayer(AdvancedSolarPanelModel.STAIR_LAYER_LOCATION, AdvancedSolarPanelModel::createStairLayer);
        ClientRegistrations.registerModelLayer(WindTurbineModel.LAYER_LOCATION, WindTurbineModel::createMainLayer);
        ClientRegistrations.registerModelLayer(OilPumpJackModel.LAYER_LOCATION, OilPumpJackModel::getTexturedModelData);
        ClientRegistrations.registerModelLayer(DrillFrameModel.LAYER_LOCATION, DrillFrameModel::getTexturedModelData);
        ClientRegistrations.registerModelLayer(SimpleDrillHeadModel.LAYER_LOCATION, SimpleDrillHeadModel::getTexturedModelData);
        ClientRegistrations.registerModelLayer(MotorModel.LAYER_LOCATION, MotorModel::getTexturedModelData);
        ClientRegistrations.registerModelLayer(DrillMotorModel.LAYER_LOCATION, DrillMotorModel::getTexturedModelData);
        ClientRegistrations.registerModelLayer(DrillCableModel.LAYER_LOCATION, DrillCableModel::getTexturedModelData);
        ClientRegistrations.registerModelLayer(UpgradeStationModel.LAYER_LOCATION, UpgradeStationModel::getTexturedModelData);
        ClientRegistrations.registerModelLayer(MixerModel.LAYER_LOCATION, MixerModel::getMainLayer);
        ClientRegistrations.registerModelLayer(DigesterModel.LAYER_LOCATION, DigesterModel::getTexturedModelData);
        ClientRegistrations.registerModelLayer(ClarifierModel.LAYER_LOCATION, ClarifierModel::getTexturedModelData);
        ClientRegistrations.registerModelLayer(CrystallizerModel.LAYER_LOCATION, CrystallizerModel::getTexturedModelData);
        ClientRegistrations.registerModelLayer(RotaryKilnModel.LAYER_LOCATION, RotaryKilnModel::getTexturedModelData);
        ClientRegistrations.registerModelLayer(ElectrolyzerModel.LAYER_LOCATION, ElectrolyzerModel::getTexturedModelData);
        ClientRegistrations.registerModelLayer(ShakingTableModel.LAYER_LOCATION, ShakingTableModel::getTexturedModelData);
        ClientRegistrations.registerModelLayer(CentrifugalConcentratorModel.LAYER_LOCATION, CentrifugalConcentratorModel::getTexturedModelData);
        ClientRegistrations.registerModelLayer(CornerTurnConveyorAnchorPositionsModel.LAYER_LOCATION, CornerTurnConveyorAnchorPositionsModel::createMainLayer);
        ClientRegistrations.registerModelLayer(StraightConveyorAnchorPositionsModel.LAYER_LOCATION, StraightConveyorAnchorPositionsModel::createMainLayer);
        ClientRegistrations.registerModelLayer(MergerConveyorAnchorPositionsModel.LEFT_LAYER_LOCATION, MergerConveyorAnchorPositionsModel::createLeftLayer);
        ClientRegistrations.registerModelLayer(MergerConveyorAnchorPositionsModel.RIGHT_LAYER_LOCATION, MergerConveyorAnchorPositionsModel::createRightLayer);
        ClientRegistrations.registerModelLayer(SplitterConveyorAnchorPositionsModel.LEFT_LAYER_LOCATION, SplitterConveyorAnchorPositionsModel::createLeftLayer);
        ClientRegistrations.registerModelLayer(SplitterConveyorAnchorPositionsModel.RIGHT_LAYER_LOCATION, SplitterConveyorAnchorPositionsModel::createRightLayer);
        ClientRegistrations.registerModelLayer(VerticalUpConveyorAnchorPositionsModel.LAYER_LOCATION, VerticalUpConveyorAnchorPositionsModel::createMainLayer);
        ClientRegistrations.registerModelLayer(VerticalDownConveyorAnchorPositionsModel.LAYER_LOCATION, VerticalDownConveyorAnchorPositionsModel::createMainLayer);
        ClientRegistrations.registerModelLayer(DropChuteConveyorAnchorPositionsModel.LAYER_LOCATION, DropChuteConveyorAnchorPositionsModel::createMainLayer);
        ClientRegistrations.registerModelLayer(ConveyorFlapsModel.LAYER_LOCATION, ConveyorFlapsModel::createMainLayer);
        ClientRegistrations.registerModelLayer(SideInjectorConveyorAnchorModel.LAYER_LOCATION, SideInjectorConveyorAnchorModel::createMainLayer);
        ClientRegistrations.registerModelLayer(LadderConveyorPlatformModel.LAYER_LOCATION, LadderConveyorPlatformModel::createMainLayer);
        ClientRegistrations.registerModelLayer(LadderConveyorTopPlatformModel.LAYER_LOCATION, LadderConveyorTopPlatformModel::createMainLayer);
        ClientRegistrations.registerModelLayer(ArcFurnaceModel.LAYER_LOCATION, ArcFurnaceModel::createMainLayer);
        ClientRegistrations.registerModelLayer(TreeTapModel.LAYER_LOCATION, TreeTapModel::createMainLayer);
        ClientRegistrations.registerModelLayer(DistillationTowerModel.LAYER_LOCATION, DistillationTowerModel::createMainLayer);
    }
}
