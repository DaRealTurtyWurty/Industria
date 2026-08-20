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
import net.minecraft.resources.Identifier;

public class LadderConveyorTopPlatformModel extends Model<Void> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Industria.id("ladder_conveyor_top_platform"), "main");
    public static final Identifier TEXTURE_LOCATION = Industria.id("textures/block/ladder_conveyor_platform.png");

    private final ModelPart root;
    private final ModelPart platform;
    private final ModelPart conveyorPart;

    public LadderConveyorTopPlatformModel(ModelPart root) {
        super(root, RenderTypes::entitySolid);
        this.root = root.getChild("root");
        this.platform = this.root.getChild("platform");
        this.conveyorPart = this.root.getChild("conveyor_part");
    }

    public ModelPart getRoot() {
        return this.root;
    }

    public ModelPart getPlatform() {
        return this.platform;
    }

    public ModelPart getConveyorPart() {
        return this.conveyorPart;
    }

    public static LayerDefinition createMainLayer() {
        var meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        PartDefinition root = partDefinition.addOrReplaceChild("root",
                CubeListBuilder.create()
                        .texOffs(10, 0)
                        .addBox(-1.0F, -1.001F, -1.001F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 15.0F, 7.0F));

        root.addOrReplaceChild("platform",
                CubeListBuilder.create()
                        .texOffs(0, 13)
                        .addBox(-8.0F, -1.0F, -2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(9, 13)
                        .addBox(6.0F, -1.0F, -2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0)
                        .addBox(-8.0F, -1.0F, -12.0F, 16.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, -3.0F));

        PartDefinition conveyorPart = root.addOrReplaceChild("conveyor_part",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, -0.2F, -1.1F));

        conveyorPart.addOrReplaceChild("cube_r1",
                CubeListBuilder.create()
                        .texOffs(0, 28)
                        .addBox(-6.0F, 0.0F, 6.0F, 12.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 31)
                        .addBox(-8.0F, 0.0F, 6.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 31)
                        .addBox(6.0F, 0.0F, 6.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 22)
                        .addBox(-8.0F, -2.0F, 6.0F, 16.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 26)
                        .addBox(-8.0F, -2.0F, 6.0F, 16.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -6.8F, 0.1F, 1.5708F, 0.0F, 0.0F));

        return LayerDefinition.create(meshDefinition, 64, 64);
    }
}
