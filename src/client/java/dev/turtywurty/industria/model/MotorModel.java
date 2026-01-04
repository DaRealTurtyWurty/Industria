package dev.turtywurty.industria.model;

import dev.turtywurty.industria.Industria;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public class MotorModel extends Model<MotorModel.MotorModelRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Industria.id("motor"), "main");
    public static final Identifier TEXTURE_LOCATION = Industria.id("textures/block/motor.png");

    private final MotorParts parts;

    public MotorModel(ModelPart root) {
        super(root, RenderTypes::entityCutout);

        ModelPart main = root.getChild("main");
        ModelPart spinRod = main.getChild("spinRod");
        this.parts = new MotorParts(main, spinRod);
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition modelPartData = modelData.getRoot();
        PartDefinition main = modelPartData.addOrReplaceChild("main", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -12.0F, 24.0F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)).texOffs(0, 10).addBox(3.0F, -12.0F, 24.0F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)).texOffs(19, 16).addBox(-4.0F, -13.0F, 29.0F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 25).addBox(-4.0F, -16.0F, 30.0F, 7.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(19, 5).addBox(-4.0F, -18.0F, 27.0F, 7.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(9, 33).addBox(3.0F, -17.0F, 27.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(28, 29).addBox(3.0F, -16.0F, 26.0F, 1.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(17, 29).addBox(-4.0F, -17.0F, 26.0F, 1.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(0, 29).addBox(-4.0F, -16.0F, 25.0F, 7.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(19, 13).addBox(-4.0F, -13.0F, 26.0F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(19, 9).addBox(-4.0F, -13.0F, 27.0F, 7.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(34, 24).addBox(3.0F, -14.0F, 27.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 35.0F, -28.0F));

        PartDefinition cube_r1 = main.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(36, 19).addBox(-3.0F, -0.5F, -0.5F, 1.0F, 1.0F, 1.4125F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.0F, -14.0F, 26.7071F, -0.7854F, 0.0F, 0.0F));

        PartDefinition cube_r2 = main.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(19, 0).addBox(-4.0F, -0.5F, -0.5F, 7.0F, 1.0F, 2.825F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -14.0F, 25.7071F, -0.7854F, 0.0F, 0.0F));

        PartDefinition cube_r3 = main.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(0, 36).addBox(-0.5F, -0.5F, -0.9125F, 1.0F, 1.0F, 1.4125F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.5F, -14.0F, 29.2929F, 0.7854F, 0.0F, 0.0F));

        PartDefinition cube_r4 = main.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(28, 36).addBox(-3.0F, 1.4075F, -0.005F, 1.0F, 1.4125F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.0F, -17.9929F, 28.0F, -0.7854F, 0.0F, 0.0F));

        PartDefinition cube_r5 = main.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(0, 20).addBox(-4.0F, -0.005F, -0.005F, 7.0F, 2.825F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -17.9929F, 27.0F, -0.7854F, 0.0F, 0.0F));

        PartDefinition cube_r6 = main.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(33, 36).addBox(2.0F, -0.5F, -0.5F, 1.0F, 1.4125F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, -16.2929F, 29.0F, 0.7854F, 0.0F, 0.0F));

        PartDefinition cube_r7 = main.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(19, 19).addBox(-4.0F, -0.5F, -0.5F, 7.0F, 2.825F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -17.2929F, 29.0F, 0.7854F, 0.0F, 0.0F));

        PartDefinition cube_r8 = main.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(17, 24).addBox(-4.0F, -0.5F, -0.5F, 7.0F, 2.825F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -14.0F, 30.2929F, -0.7854F, 0.0F, 0.0F));

        PartDefinition spinRod = main.addOrReplaceChild("spinRod", CubeListBuilder.create().texOffs(0, 33).addBox(-1.5F, -0.5F, -0.5F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(5.5F, -15.0F, 28.0F));
        return LayerDefinition.create(modelData, 64, 64);
    }

    @Override
    public void setupAnim(MotorModelRenderState state) {
        super.setupAnim(state);
        this.parts.spinRod().xRot = state.rodRotation;
    }

    public record MotorModelRenderState(float rodRotation) {
    }

    public record MotorParts(ModelPart main, ModelPart spinRod) {}
}
