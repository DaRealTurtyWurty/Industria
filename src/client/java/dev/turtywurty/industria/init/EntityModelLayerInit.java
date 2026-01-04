package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.model.*;
import dev.turtywurty.industria.util.WoodRegistrySet;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.object.boat.BoatModel;
import net.minecraft.util.Tuple;

import java.util.HashMap;
import java.util.Map;

public class EntityModelLayerInit {
    private static final Map<WoodRegistrySet, Tuple<ModelLayerLocation, ModelLayerLocation>> BOAT_MODEL_LAYERS = new HashMap<>();

    public static void init() {
        ModelLayerRegistry.registerModelLayer(CrusherModel.LAYER_LOCATION, CrusherModel::getTexturedModelData);
        ModelLayerRegistry.registerModelLayer(WindTurbineModel.LAYER_LOCATION, WindTurbineModel::getTexturedModelData);
        ModelLayerRegistry.registerModelLayer(OilPumpJackModel.LAYER_LOCATION, OilPumpJackModel::getTexturedModelData);
        ModelLayerRegistry.registerModelLayer(DrillFrameModel.LAYER_LOCATION, DrillFrameModel::getTexturedModelData);
        ModelLayerRegistry.registerModelLayer(SimpleDrillHeadModel.LAYER_LOCATION, SimpleDrillHeadModel::getTexturedModelData);
        ModelLayerRegistry.registerModelLayer(MotorModel.LAYER_LOCATION, MotorModel::getTexturedModelData);
        ModelLayerRegistry.registerModelLayer(DrillMotorModel.LAYER_LOCATION, DrillMotorModel::getTexturedModelData);
        ModelLayerRegistry.registerModelLayer(DrillCableModel.LAYER_LOCATION, DrillCableModel::getTexturedModelData);
        ModelLayerRegistry.registerModelLayer(UpgradeStationModel.LAYER_LOCATION, UpgradeStationModel::getTexturedModelData);
        ModelLayerRegistry.registerModelLayer(MixerModel.LAYER_LOCATION, MixerModel::getTexturedModelData);
        ModelLayerRegistry.registerModelLayer(DigesterModel.LAYER_LOCATION, DigesterModel::getTexturedModelData);
        ModelLayerRegistry.registerModelLayer(ClarifierModel.LAYER_LOCATION, ClarifierModel::getTexturedModelData);
        ModelLayerRegistry.registerModelLayer(CrystallizerModel.LAYER_LOCATION, CrystallizerModel::getTexturedModelData);
        ModelLayerRegistry.registerModelLayer(RotaryKilnModel.LAYER_LOCATION, RotaryKilnModel::getTexturedModelData);
        ModelLayerRegistry.registerModelLayer(ElectrolyzerModel.LAYER_LOCATION, ElectrolyzerModel::getTexturedModelData);
        ModelLayerRegistry.registerModelLayer(ShakingTableModel.LAYER_LOCATION, ShakingTableModel::getTexturedModelData);
        ModelLayerRegistry.registerModelLayer(CentrifugalConcentratorModel.LAYER_LOCATION, CentrifugalConcentratorModel::getTexturedModelData);

        for (WoodRegistrySet woodSet : WoodRegistrySet.getWoodSets()) {
            var normalLayer = new ModelLayerLocation(Industria.id("boat/" + woodSet.getName()), "main");
            var chestLayer = new ModelLayerLocation(Industria.id("chest_boat/" + woodSet.getName()), "main");

            ModelLayerRegistry.registerModelLayer(normalLayer, BoatModel::createBoatModel);
            ModelLayerRegistry.registerModelLayer(chestLayer, BoatModel::createChestBoatModel);

            BOAT_MODEL_LAYERS.put(woodSet, new Tuple<>(normalLayer, chestLayer));
        }
    }

    public static ModelLayerLocation getBoatModelLayer(WoodRegistrySet woodSet) {
        return BOAT_MODEL_LAYERS.get(woodSet).getA();
    }

    public static ModelLayerLocation getChestBoatModelLayer(WoodRegistrySet woodSet) {
        return BOAT_MODEL_LAYERS.get(woodSet).getB();
    }
}
