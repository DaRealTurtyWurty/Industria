package dev.turtywurty.industria.model.conveyor;

import dev.turtywurty.industria.Industria;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;

public class SplitterConveyorAnchorPositionsModel extends Model<Void> {
    public static final ModelLayerLocation LEFT_LAYER_LOCATION = new ModelLayerLocation(Industria.id("splitter_conveyor_left_anchor_positions"), "main");
    public static final ModelLayerLocation RIGHT_LAYER_LOCATION = new ModelLayerLocation(Industria.id("splitter_conveyor_right_anchor_positions"), "main");

    public SplitterConveyorAnchorPositionsModel(ModelPart root) {
        super(root, RenderTypes::entitySolid);
        root.getChild("root");
    }

    public static LayerDefinition createLeftLayer() {
        return createLayer(8.0F);
    }

    public static LayerDefinition createRightLayer() {
        return createLayer(-8.0F);
    }

    private static LayerDefinition createLayer(float exitX) {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        PartDefinition root = partDefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        root.addOrReplaceChild("anchor0", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -5.0F, -8.0F));
        root.addOrReplaceChild("anchor1", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(exitX * 0.375F, -5.0F, -1.0F));
        root.addOrReplaceChild("anchor2", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(exitX, -5.0F, 0.0F));

        return LayerDefinition.create(meshDefinition, 16, 16);
    }
}
