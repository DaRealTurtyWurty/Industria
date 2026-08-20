package dev.turtywurty.industria.model;

import dev.turtywurty.industria.Industria;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public class ClarifierModel extends Model<Void> {
    public static final Identifier TEXTURE_LOCATION = Industria.id("textures/block/clarifier.png");
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Industria.id("clarifier"), "main");

    private final ModelPart main;
    private final ModelPart fluid_ramp;
    private final ModelPart bone;
    private final ModelPart bone2;
    private final ModelPart bone3;
    private final ModelPart bone4;
    private final ModelPart bone5;
    private final ModelPart bone6;

    public ClarifierModel(ModelPart root) {
        super(root, RenderTypes::entityCutout);
        this.main = root.getChild("main");
        this.fluid_ramp = main.getChild("fluid_ramp");
        this.bone = main.getChild("bone");
        this.bone2 = main.getChild("bone2");
        this.bone3 = main.getChild("bone3");
        this.bone4 = main.getChild("bone4");
        this.bone5 = main.getChild("bone5");
        this.bone6 = main.getChild("bone6");
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition modelPartData = modelData.getRoot();
        PartDefinition main = modelPartData.addOrReplaceChild("main", CubeListBuilder.create().texOffs(0, 135).addBox(-8.0F, -2.0F, -23.0F, 16.0F, 1.0F, 31.0F, new CubeDeformation(0.0F))
                .texOffs(67, 224).addBox(-8.0F, -12.0F, -23.0F, 16.0F, 2.0F, 15.0F, new CubeDeformation(0.0F))
                .texOffs(220, 0).addBox(-8.0F, -16.0F, -23.0F, 16.0F, 4.0F, 15.0F, new CubeDeformation(0.0F))
                .texOffs(220, 20).addBox(-8.0F, -16.0F, 8.0F, 16.0F, 1.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-10.0F, -1.0F, -23.0F, 20.0F, 1.0F, 47.0F, new CubeDeformation(0.0F))
                .texOffs(142, 208).addBox(-8.0F, -10.0F, -23.0F, 2.0F, 8.0F, 32.0F, new CubeDeformation(0.0F))
                .texOffs(211, 208).addBox(6.0F, -10.0F, -23.0F, 2.0F, 8.0F, 32.0F, new CubeDeformation(0.0F))
                .texOffs(218, 105).addBox(-8.0F, -12.0F, -8.0F, 16.0F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(0, 168).addBox(8.0F, -16.0F, -23.0F, 2.0F, 15.0F, 33.0F, new CubeDeformation(0.0F))
                .texOffs(71, 175).addBox(-10.0F, -16.0F, -23.0F, 2.0F, 15.0F, 33.0F, new CubeDeformation(0.0F))
                .texOffs(71, 168).addBox(-8.0F, -12.0F, 8.0F, 16.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(247, 185).addBox(-8.0F, -3.0F, 19.0F, 16.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(157, 249).addBox(-8.0F, -15.0F, 8.0F, 2.0F, 12.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(120, 249).addBox(6.0F, -15.0F, 8.0F, 2.0F, 12.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(182, 43).addBox(-6.0F, -15.0F, 22.0F, 12.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(194, 249).addBox(8.0F, -16.0F, 10.0F, 2.0F, 15.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(227, 249).addBox(-10.0F, -16.0F, 10.0F, 2.0F, 15.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(74, 259).addBox(-10.0F, -24.0F, -10.0F, 20.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 251).addBox(-10.0F, -24.0F, -8.0F, 2.0F, 8.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(37, 259).addBox(8.0F, -24.0F, -8.0F, 2.0F, 8.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(247, 174).addBox(-10.0F, -24.0F, 8.0F, 20.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(259, 146).addBox(-6.0F, -10.0F, 8.0F, 12.0F, 9.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(142, 193).addBox(-21.0F, -28.0F, 13.0F, 42.0F, 7.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(218, 43).addBox(14.0F, -28.0F, -13.0F, 7.0F, 7.0F, 26.0F, new CubeDeformation(0.0F))
                .texOffs(198, 131).addBox(-21.0F, -28.0F, -20.0F, 42.0F, 7.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(0, 217).addBox(-21.0F, -28.0F, -13.0F, 7.0F, 7.0F, 26.0F, new CubeDeformation(0.0F))
                .texOffs(198, 146).addBox(-14.0F, -22.0F, -13.0F, 4.0F, 1.0F, 26.0F, new CubeDeformation(0.0F))
                .texOffs(135, 43).addBox(-10.0F, -22.0F, 10.0F, 20.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(218, 124).addBox(-10.0F, -22.0F, -13.0F, 20.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(218, 77).addBox(10.0F, -22.0F, -13.0F, 4.0F, 1.0F, 26.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        main.addOrReplaceChild("fluid_ramp", CubeListBuilder.create().texOffs(241, 193).addBox(-8.0F, -0.5F, -6.0F, 16.0F, 1.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.9455F, 2.3968F, 0.2618F, 0.0F, 0.0F));

        PartDefinition bone6 = main.addOrReplaceChild("bone6", CubeListBuilder.create(), PartPose.offset(1.0F, -12.0F, 11.0F));

        bone6.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(67, 242).addBox(-7.0F, 0.0F, 0.0F, 12.0F, 2.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.7418F, 0.0F, 0.0F));

        PartDefinition bone = main.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(142, 175).addBox(-24.0F, -32.0F, -23.0F, 48.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(135, 0).addBox(-24.0F, -32.0F, -19.0F, 4.0F, 4.0F, 38.0F, new CubeDeformation(0.0F))
                .texOffs(111, 131).addBox(20.0F, -32.0F, -20.0F, 4.0F, 4.0F, 39.0F, new CubeDeformation(0.0F))
                .texOffs(142, 184).addBox(-24.0F, -32.0F, 19.0F, 48.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition bone2 = main.addOrReplaceChild("bone2", CubeListBuilder.create(), PartPose.offset(20.0553F, -31.9453F, -0.5F));

        bone2.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(111, 90).addBox(-15.0106F, -0.077F, -18.5F, 15.0F, 2.0F, 38.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.5847F));

        PartDefinition bone3 = main.addOrReplaceChild("bone3", CubeListBuilder.create(), PartPose.offset(-20.0513F, -31.9416F, -0.5F));

        bone3.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(111, 49).addBox(-15.0106F, -0.077F, -19.5F, 15.0F, 2.0F, 38.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 3.1416F, 0.0F, -2.5569F));

        PartDefinition bone4 = main.addOrReplaceChild("bone4", CubeListBuilder.create(), PartPose.offset(0.5F, -27.0247F, -1.002F));

        bone4.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(0, 49).addBox(4.4254F, 6.891F, -19.5F, 15.0F, 2.0F, 40.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.5708F, -0.9861F, -1.5708F));

        PartDefinition bone5 = main.addOrReplaceChild("bone5", CubeListBuilder.create(), PartPose.offset(0.5F, -27.0284F, 1.002F));

        bone5.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(0, 92).addBox(4.4234F, 6.8941F, -20.5F, 15.0F, 2.0F, 40.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.5708F, 0.9861F, -1.5708F));
        return LayerDefinition.create(modelData, 512, 512);
    }
}