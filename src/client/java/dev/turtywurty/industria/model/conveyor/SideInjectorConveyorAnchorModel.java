package dev.turtywurty.industria.model.conveyor;

import dev.turtywurty.industria.Industria;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.rendertype.RenderTypes;

public class SideInjectorConveyorAnchorModel extends Model<Void> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Industria.id("side_injector_conveyor_anchor_positions"), "main");

    public SideInjectorConveyorAnchorModel(ModelPart root) {
        super(root, RenderTypes::entitySolid);
        root.getChild("root");
    }

    public static LayerDefinition createMainLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partdefinition = meshDefinition.getRoot();

        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        root.addOrReplaceChild("anchor0", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-1.0F, -1.0F, -2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -4.0F, -8.0F));

        root.addOrReplaceChild("anchor1", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-1.0F, -1.0F, -2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -4.0F, -1.0F));

        root.addOrReplaceChild("anchor2", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-1.0F, -1.0F, -2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -4.2612F, 0.5737F, -0.5498F, 0.0F, 0.0F));

        root.addOrReplaceChild("anchor3", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-1.0F, -1.0F, -2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -1.1263F, 5.6895F, -0.5498F, 0.0F, 0.0F));

        root.addOrReplaceChild("anchor4", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-1.0F, -1.0F, -2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -1.0F, 6.0F));

        root.addOrReplaceChild("anchor5", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-1.0F, -1.0F, -2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -1.0F, 10.0F));

        root.addOrReplaceChild("anchor6", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-2.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-1.0F, -5.0F, 13.0F));

        return LayerDefinition.create(meshDefinition, 16, 16);
    }
}
