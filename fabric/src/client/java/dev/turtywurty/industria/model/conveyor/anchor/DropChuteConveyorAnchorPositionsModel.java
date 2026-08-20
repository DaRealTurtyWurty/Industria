package dev.turtywurty.industria.model.conveyor.anchor;

import dev.turtywurty.industria.Industria;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.rendertype.RenderTypes;

public class DropChuteConveyorAnchorPositionsModel extends Model<Void> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Industria.id("drop_chute_conveyor_anchor_positions"), "main");

    public DropChuteConveyorAnchorPositionsModel(ModelPart root) {
        super(root, RenderTypes::entitySolid);
        root.getChild("root");
    }

    public static LayerDefinition createMainLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        PartDefinition root = partDefinition.addOrReplaceChild("root", CubeListBuilder.create(),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        root.addOrReplaceChild("anchor0", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -5.0F, -8.0F));

        root.addOrReplaceChild("anchor1", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -5.0F, -3.0F));

        root.addOrReplaceChild("anchor2", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -4.0F, 0.0F));

        root.addOrReplaceChild("anchor3", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -8.0F, 0.0F));

        root.addOrReplaceChild("anchor4", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -12.0F, 0.0F));

        return LayerDefinition.create(meshDefinition, 16, 16);
    }
}
