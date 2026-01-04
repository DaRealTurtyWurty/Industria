// Made with Blockbench 4.12.4
// Exported for Minecraft version 1.17+ for Yarn
// Paste this class into your mod and generate all required imports

package dev.turtywurty.industria.model;

import dev.turtywurty.industria.Industria;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public class ElectrolyzerModel extends Model<Void> {
    public static final Identifier TEXTURE_LOCATION = Industria.id("textures/block/electrolyzer.png");
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Industria.id("electrolyzer"), "main");

    public ElectrolyzerModel(ModelPart root) {
        super(root, RenderTypes::entityCutout);
    }

    public static LayerDefinition getTexturedModelData() {
        var modelData = new MeshDefinition();
        PartDefinition modelPartData = modelData.getRoot();
        modelPartData.addOrReplaceChild("main", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-23.0F, -32.0F, -1.0F, 48.0F, 32.0F, 32.0F,
                                new CubeDeformation(0.0F)),
                PartPose.offset(-1.0F, 24.0F, -7.0F));

        return LayerDefinition.create(modelData, 256, 256);
    }
}
