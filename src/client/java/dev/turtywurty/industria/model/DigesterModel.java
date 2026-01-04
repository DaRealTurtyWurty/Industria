// Made with Blockbench 4.12.3
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

public class DigesterModel extends Model<Void> {
    public static final Identifier TEXTURE_LOCATION = Industria.id("textures/block/digester.png");
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Industria.id("digester"), "main");

    private final ModelPart main;

    public DigesterModel(ModelPart root) {
        super(root, RenderTypes::entitySolid);
        this.main = root.getChild("main");
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition modelPartData = modelData.getRoot();
        modelPartData.addOrReplaceChild("main", CubeListBuilder.create().texOffs(0, 0).addBox(-28.0F, -80.0F, -12.0F, 40.0F, 80.0F, 40.0F, new CubeDeformation(0.0F)), PartPose.offset(8.0F, 24.0F, -8.0F));

        return LayerDefinition.create(modelData, 256, 256);
    }
}