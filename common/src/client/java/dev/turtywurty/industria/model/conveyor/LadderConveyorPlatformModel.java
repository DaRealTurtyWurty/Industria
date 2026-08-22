package dev.turtywurty.industria.model.conveyor;

import dev.turtywurty.industria.Industria;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public class LadderConveyorPlatformModel extends Model<Void> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Industria.id("ladder_conveyor_platform"), "main");
    public static final Identifier TEXTURE_LOCATION = Industria.id("textures/block/ladder_conveyor_platform.png");

    private final ModelPart root;

    public LadderConveyorPlatformModel(ModelPart root) {
        super(root, RenderTypes::entitySolid);
        this.root = root.getChild("root");
    }

    public ModelPart getRoot() {
        return this.root;
    }

    public static LayerDefinition createMainLayer() {
        var meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        partDefinition.addOrReplaceChild("root", CubeListBuilder.create()
                        .texOffs(0, 13)
                        .addBox(-8.0F, -1.0F, -2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(9, 13)
                        .addBox(6.0F, -1.0F, -2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0)
                        .addBox(-8.0F, -1.0F, -12.0F, 16.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 1.0F, 4.0F));

        return LayerDefinition.create(meshDefinition, 64, 64);
    }
}
