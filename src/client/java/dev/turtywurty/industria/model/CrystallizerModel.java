package dev.turtywurty.industria.model;

import dev.turtywurty.industria.Industria;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public class CrystallizerModel extends Model<Void> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Industria.id("crystallizer"), "main");
    public static final Identifier TEXTURE_LOCATION = Industria.id("textures/block/crystallizer.png");

    private final ModelPart main;

    public CrystallizerModel(ModelPart root) {
        super(root, RenderTypes::entityCutout);

        this.main = root.getChild("main");
    }

    public static LayerDefinition getTexturedModelData() {
        var modelData = new MeshDefinition();
        PartDefinition modelPartData = modelData.getRoot();
        modelPartData.addOrReplaceChild("main", CubeListBuilder.create().texOffs(0, 0).addBox(-28.0F, -64.0F, -12.0F, 40.0F, 2.0F, 40.0F, new CubeDeformation(0.0F))
                .texOffs(0, 43).addBox(-28.0F, -2.0F, -12.0F, 40.0F, 2.0F, 40.0F, new CubeDeformation(0.0F))
                .texOffs(161, 0).addBox(-26.0F, -16.0F, -10.0F, 36.0F, 2.0F, 36.0F, new CubeDeformation(0.0F))
                .texOffs(170, 151).addBox(-26.0F, -16.0F, -12.0F, 36.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(170, 102).addBox(-26.0F, -62.0F, -12.0F, 36.0F, 46.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(170, 172).addBox(0.0F, -14.0F, -12.0F, 10.0F, 12.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 187).addBox(-26.0F, -14.0F, -12.0F, 10.0F, 12.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(170, 39).addBox(-26.0F, -62.0F, 26.0F, 36.0F, 60.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(85, 86).addBox(-28.0F, -62.0F, -12.0F, 2.0F, 60.0F, 40.0F, new CubeDeformation(0.0F))
                .texOffs(0, 86).addBox(10.0F, -62.0F, -12.0F, 2.0F, 60.0F, 40.0F, new CubeDeformation(0.0F))
                .texOffs(170, 156).addBox(-16.0F, -14.0F, -13.0F, 16.0F, 12.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(8.0F, 24.0F, -8.0F));

        return LayerDefinition.create(modelData, 512, 512);
    }
}