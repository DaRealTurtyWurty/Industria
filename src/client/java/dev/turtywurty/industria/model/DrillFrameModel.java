package dev.turtywurty.industria.model;

import dev.turtywurty.industria.Industria;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public class DrillFrameModel extends Model<DrillFrameModel.DrillFrameModelRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Industria.id("drill_frame"), "main");
    public static final Identifier TEXTURE_LOCATION = Industria.id("textures/block/drill_frame.png");

    private final ModelPart cableWheel;
    private final ModelPart cableWheelRod;

    public DrillFrameModel(ModelPart root) {
        super(root, RenderTypes::entityCutout);
        ModelPart main = root.getChild("main");
        this.cableWheel = main.getChild("cableWheel");
        this.cableWheelRod = main.getChild("cableWheelRod");
    }

    public static LayerDefinition getTexturedModelData() {
        var modelData = new MeshDefinition();
        PartDefinition modelPartData = modelData.getRoot();
        PartDefinition main = modelPartData.addOrReplaceChild("main", CubeListBuilder.create().texOffs(33, 85).addBox(15.9135F, 36.3298F, 15.1928F, 8.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(66, 85).addBox(15.9135F, 36.3298F, -24.8072F, 8.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(86, 72).addBox(-24.0865F, 36.3298F, -24.8072F, 8.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(86, 61).addBox(-24.0865F, 36.3298F, 15.1928F, 8.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(68, 31).addBox(-11.0865F, -3.3146F, -6.8072F, 5.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(67, 0).addBox(-11.0865F, -3.3146F, -11.8072F, 22.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(67, 8).addBox(-11.0865F, -3.3146F, 5.1928F, 22.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(68, 46).addBox(5.9135F, -3.3146F, -6.8072F, 5.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(23, 100).addBox(6.9135F, -15.3146F, 6.1928F, 2.0F, 12.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(103, 31).addBox(-9.0865F, -15.3146F, 6.1928F, 2.0F, 12.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(103, 46).addBox(-9.0865F, -15.3146F, -9.8072F, 2.0F, 12.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(103, 101).addBox(6.9135F, -15.3146F, -9.8072F, 2.0F, 12.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(53, 68).addBox(6.9135F, -15.3146F, -7.8072F, 2.0F, 2.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(0, 73).addBox(-9.0865F, -15.3146F, -7.8072F, 2.0F, 2.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(0, 90).addBox(-7.0865F, -15.3146F, -9.8072F, 14.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 95).addBox(-7.0865F, -15.3146F, 6.1928F, 14.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(86, 101).addBox(-1.9652F, -19.3122F, -2.9285F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 105).addBox(2.0348F, -19.3122F, -1.9285F, 7.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(33, 73).addBox(-0.9652F, -19.3122F, -9.9285F, 2.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(67, 96).addBox(-0.9652F, -19.3122F, 1.0715F, 2.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(32, 111).addBox(-0.9652F, -17.3122F, -9.9285F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(41, 111).addBox(-0.9652F, -17.3122F, 6.0715F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(67, 106).addBox(-8.9652F, -19.3122F, -1.9285F, 7.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 110).addBox(7.0348F, -17.3122F, -1.9285F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(9, 110).addBox(-8.9652F, -17.3122F, -1.9285F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(18, 110).addBox(0.0358F, -13.3122F, -1.9285F, 0.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-15.0865F, 24.3298F, -17.8072F, 31.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 68).addBox(-12.0865F, 11.3298F, -14.8072F, 24.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 5).addBox(-15.0865F, 24.3298F, 14.1928F, 31.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(68, 16).addBox(-12.0865F, 11.3298F, 11.1928F, 24.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0865F, -14.3298F, 0.8072F));

        PartDefinition cube_r1 = main.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(68, 21).addBox(-12.25F, -7.5F, 12.0F, 24.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 10).addBox(-15.25F, 5.5F, 15.0F, 31.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(68, 26).addBox(-12.25F, -7.5F, -14.0F, 24.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 15).addBox(-15.25F, 5.5F, -17.0F, 31.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.1635F, 18.8298F, -0.8072F, 0.0F, -1.5708F, 0.0F));

        PartDefinition cube_r2 = main.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 100).addBox(-10.0F, -0.9986F, -1.0F, 9.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(99, 83).addBox(1.0F, -0.9986F, -1.0F, 9.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0348F, -14.3146F, -0.9285F, 0.0F, -0.7854F, 0.0F));

        PartDefinition cube_r3 = main.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(99, 88).addBox(-7.0F, -0.9986F, -1.0F, 9.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(86, 96).addBox(4.0F, -0.9986F, -1.0F, 9.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0865F, -14.3146F, 1.1928F, 0.0F, 0.7854F, 0.0F));

        PartDefinition cube_r4 = main.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(34, 20).addBox(-2.0F, -21.5F, -2.0F, 4.0F, 43.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-13.8099F, 17.3142F, -14.5305F, -2.7489F, -0.7854F, -3.1416F));

        PartDefinition cube_r5 = main.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(51, 20).addBox(-2.0F, -21.5F, -2.0F, 4.0F, 43.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(13.6368F, 17.3142F, 12.9162F, 2.7489F, -0.7854F, 3.1416F));

        PartDefinition cube_r6 = main.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(17, 20).addBox(-2.0F, -21.5F, -2.0F, 4.0F, 43.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-13.8099F, 17.3142F, 12.9162F, 0.3927F, -0.7854F, 0.0F));

        PartDefinition cube_r7 = main.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(0, 20).addBox(-2.0F, -21.5F, -2.0F, 4.0F, 43.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(13.6368F, 17.3142F, -14.5305F, -0.3927F, -0.7854F, 0.0F));

        PartDefinition cableWheelRod = main.addOrReplaceChild("cableWheelRod", CubeListBuilder.create().texOffs(68, 64).addBox(1.0F, -0.5F, -0.5F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(68, 61).addBox(-7.0F, -0.5F, -0.5F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.0865F, -8.3146F, 7.1928F));

        PartDefinition cableWheel = main.addOrReplaceChild("cableWheel", CubeListBuilder.create().texOffs(109, 93).addBox(0.0F, -2.0F, -2.0F, 1.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(33, 96).addBox(1.0F, -4.0F, -4.0F, 1.0F, 7.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(50, 96).addBox(-1.0F, -4.0F, -4.0F, 1.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.4642F, -7.8122F, 7.5715F));
        return LayerDefinition.create(modelData, 128, 128);
    }

    @Override
    public void setupAnim(DrillFrameModelRenderState state) {
        super.setupAnim(state);
        this.cableWheel.xRot = state.clientMotorRotation;
        this.cableWheelRod.xRot = state.clientMotorRotation;
    }

    public record DrillFrameModelRenderState(float clientMotorRotation) {
    }
}
