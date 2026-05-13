package dev.turtywurty.industria.model;

import dev.turtywurty.industria.Industria;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public class DistillationTowerModel extends Model<Void> {
    public static final Identifier TEXTURE_LOCATION = Industria.id("textures/block/distillation_tower.png");
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Industria.id("distillation_tower"), "main");

    public DistillationTowerModel(ModelPart root) {
        super(root, RenderTypes::entitySolid);
    }

    public static LayerDefinition createMainLayer() {
        var meshDefinition = new MeshDefinition();
        PartDefinition root = meshDefinition.getRoot();

        root.addOrReplaceChild("bone", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-24.0F, -112.0F, -24.0F, 48.0F, 112.0F, 48.0F),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        return LayerDefinition.create(meshDefinition, 256, 256);
    }
}
