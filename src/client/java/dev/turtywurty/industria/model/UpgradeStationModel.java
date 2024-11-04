// Made with Blockbench 4.11.1
// Exported for Minecraft version 1.17+ for Yarn
// Paste this class into your mod and generate all required imports

package dev.turtywurty.industria.model;

import dev.turtywurty.industria.Industria;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public class UpgradeStationModel extends Model {
    public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(Industria.id("upgrade_station"), "main");
    public static final Identifier TEXTURE_LOCATION = Industria.id("textures/block/upgrade_station.png");

    public UpgradeStationModel(ModelPart root) {
        super(root, RenderLayer::getEntitySolid);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        ModelPartData main = modelPartData.addChild("main", ModelPartBuilder.create().uv(0, 0).cuboid(-24.0F, -16.0F, -40.0F, 48.0F, 16.0F, 64.0F, new Dilation(0.0F))
                .uv(0, 80).cuboid(-8.0F, -32.0F, -24.0F, 16.0F, 16.0F, 16.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 24.0F, 0.0F));
        return TexturedModelData.of(modelData, 256, 256);
    }
}