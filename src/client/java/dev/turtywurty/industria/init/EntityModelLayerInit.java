package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.model.*;
import dev.turtywurty.industria.util.WoodRegistrySet;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.render.entity.model.BoatEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Pair;

import java.util.HashMap;
import java.util.Map;

public class EntityModelLayerInit {
    private static final Map<WoodRegistrySet, Pair<EntityModelLayer, EntityModelLayer>> BOAT_MODEL_LAYERS = new HashMap<>();

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
        EntityModelLayerRegistry.registerModelLayer(CrystallizerModel.LAYER_LOCATION, CrystallizerModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(RotaryKilnModel.LAYER_LOCATION, RotaryKilnModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(ElectrolyzerModel.LAYER_LOCATION, ElectrolyzerModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(ShakingTableModel.LAYER_LOCATION, ShakingTableModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(CentrifugalConcentratorModel.LAYER_LOCATION, CentrifugalConcentratorModel::getTexturedModelData);

        for (WoodRegistrySet woodSet : WoodRegistrySet.getWoodSets()) {
            var normalLayer = new EntityModelLayer(Industria.id("boat/" + woodSet.getName()), "main");
            var chestLayer = new EntityModelLayer(Industria.id("chest_boat/" + woodSet.getName()), "main");

            EntityModelLayerRegistry.registerModelLayer(normalLayer, BoatEntityModel::getTexturedModelData);
            EntityModelLayerRegistry.registerModelLayer(chestLayer, BoatEntityModel::getChestTexturedModelData);

            BOAT_MODEL_LAYERS.put(woodSet, new Pair<>(normalLayer, chestLayer));
        }
    }

    public static EntityModelLayer getBoatModelLayer(WoodRegistrySet woodSet) {
        return BOAT_MODEL_LAYERS.get(woodSet).getLeft();
    }

    public static EntityModelLayer getChestBoatModelLayer(WoodRegistrySet woodSet) {
        return BOAT_MODEL_LAYERS.get(woodSet).getRight();
    }
}
