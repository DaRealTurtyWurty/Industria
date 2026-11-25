package dev.turtywurty.industria.model;

import dev.turtywurty.industria.Industria;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public class CokeOvenModel extends Model<Void> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Industria.id("coke_oven"), "main");
    public static final Identifier TEXTURE_LOCATION = Industria.id("textures/block/coke_oven.png");

    private final ModelPart body;
    private final ModelPart bb_main;

    public CokeOvenModel(ModelPart root) {
        super(root, RenderTypes::entitySolid);
        this.body = root.getChild("body");
        this.bb_main = root.getChild("bb_main");
    }

    public static LayerDefinition createMainLayer() {
        var meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        PartDefinition body = partDefinition.addOrReplaceChild("body",
            CubeListBuilder.create().texOffs(188, 138)
                .addBox(12.0F, -32.0F, -24.0F, 8.0F, 32.0F, 64.0F, new CubeDeformation(0.0F))
                .texOffs(192, 0).addBox(-20.0F, -32.0F, -24.0F, 8.0F, 32.0F, 64.0F, new CubeDeformation(0.0F))
                .texOffs(0, 72).addBox(-16.0F, -34.0F, -24.0F, 32.0F, 2.0F, 64.0F, new CubeDeformation(0.0F))
                .texOffs(6, 138).addBox(-12.0F, -32.0F, -24.0F, 24.0F, 4.0F, 64.0F, new CubeDeformation(0.0F))
                .texOffs(6, 0).addBox(-12.0F, -8.0F, -24.0F, 24.0F, 8.0F, 64.0F, new CubeDeformation(0.0F))
                .texOffs(6, 206).addBox(-12.0F, -28.0F, 0.0F, 24.0F, 20.0F, 40.0F, new CubeDeformation(0.0F))
                .texOffs(140, 234).addBox(-22.0F, -8.0F, 8.0F, 2.0F, 8.0F, 32.0F, new CubeDeformation(0.0F))
                .texOffs(232, 302).addBox(20.0F, -8.0F, -24.0F, 2.0F, 8.0F, 20.0F, new CubeDeformation(0.0F))
                .texOffs(272, 128).addBox(20.0F, -8.0F, 4.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(208, 234).addBox(20.0F, -8.0F, 8.0F, 2.0F, 8.0F, 32.0F, new CubeDeformation(0.0F))
                .texOffs(248, 128).addBox(-22.0F, -8.0F, 4.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(170, 339).addBox(-22.0F, -8.0F, -24.0F, 2.0F, 8.0F, 20.0F, new CubeDeformation(0.0F))
                .texOffs(276, 234).addBox(19.0F, -31.0F, -22.0F, 4.0F, 31.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(276, 234).addBox(19.0F, -31.0F, 6.0F, 4.0F, 31.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(276, 234).addBox(19.0F, -31.0F, 34.0F, 4.0F, 31.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(288, 120).addBox(18.0F, -5.9F, -23.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(288, 120).addBox(18.0F, -5.9F, 5.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(288, 120).addBox(18.0F, -5.9F, 33.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(40, 266).addBox(19.0F, -5.0F, -17.0F, 4.0F, 4.0F, 22.0F, new CubeDeformation(0.0F))
                .texOffs(40, 266).addBox(19.0F, -5.0F, 11.0F, 4.0F, 4.0F, 22.0F, new CubeDeformation(0.0F))
                .texOffs(198, 96).addBox(-23.0F, -35.0F, -22.0F, 46.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(198, 96).addBox(-23.0F, -35.0F, 6.0F, 46.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(198, 96).addBox(-23.0F, -35.0F, 34.0F, 46.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(88, 300).addBox(-3.0F, -36.0F, -23.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(88, 300).addBox(-3.0F, -36.0F, 5.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(88, 300).addBox(-3.0F, -36.0F, 33.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(280, 269).addBox(-23.0F, -31.0F, -22.0F, 4.0F, 31.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(40, 292).addBox(-23.0F, -31.0F, 34.0F, 4.0F, 31.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(72, 292).addBox(-23.0F, -31.0F, 6.0F, 4.0F, 31.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 266).addBox(11.0F, -40.1F, 17.0F, 10.0F, 40.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(192, 120).addBox(10.0F, -44.0F, 16.0F, 12.0F, 4.0F, 12.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition cube_r1 = body.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(192, 104)
            .addBox(-26.0F, -2.0F, -2.0F, 52.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(0.0F, -33.0F, 8.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition cube_r2 = body.addOrReplaceChild("cube_r2",
            CubeListBuilder.create().texOffs(40, 266)
                .addBox(-2.0F, -2.0F, -25.0F, 4.0F, 4.0F, 22.0F, new CubeDeformation(0.0F))
                .texOffs(40, 266).addBox(-2.0F, -2.0F, 3.0F, 4.0F, 4.0F, 22.0F, new CubeDeformation(0.0F))
                .texOffs(288, 120).addBox(-3.0F, -2.9F, -31.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(288, 120).addBox(-3.0F, -2.9F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(288, 120).addBox(-3.0F, -2.9F, 25.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(-21.0F, -3.0F, 8.0F, 0.0F, 3.1416F, 0.0F));

        PartDefinition bb_main = partDefinition.addOrReplaceChild("bb_main", CubeListBuilder.create().texOffs(168, 300)
            .addBox(-24.0F, -11.0F, -3.0F, 2.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition cube_r3 = bb_main.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(132, 300)
            .addBox(-2.0F, -2.0F, -5.0F, 4.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(21.0F, -8.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition item_port_r1 = bb_main.addOrReplaceChild("item_port_r1", CubeListBuilder.create()
            .texOffs(168, 300).addBox(-3.0F, -3.0F, -3.0F, 2.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(21.0F, -8.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

        PartDefinition cube_r4 = bb_main.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(132, 300)
            .addBox(-2.0F, -2.0F, -3.0F, 4.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(-19.0F, -8.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition item_port_r2 = bb_main.addOrReplaceChild("item_port_r2", CubeListBuilder.create()
            .texOffs(152, 300).addBox(-1.0F, -3.0F, -3.0F, 2.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(-16.0F, -8.0F, 41.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition fluid_port_r1 = bb_main
            .addOrReplaceChild(
                "fluid_port_r1", CubeListBuilder.create().texOffs(172, 206).addBox(-1.0F, -3.0F, -3.0F, 2.0F, 6.0F,
                    6.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(16.0F, -8.0F, 41.0F, 0.0F, -1.5708F, 0.0F));

        return LayerDefinition.create(meshDefinition, 512, 512);
    }
}
