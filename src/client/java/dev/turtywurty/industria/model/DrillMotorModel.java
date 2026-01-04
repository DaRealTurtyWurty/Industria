package dev.turtywurty.industria.model;

import dev.turtywurty.industria.Industria;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public class DrillMotorModel extends Model<DrillMotorModel.DrillMotorModelRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Industria.id("drill_motor"), "main");
    public static final Identifier TEXTURE_LOCATION = Industria.id("textures/block/drill_motor.png");

    private final DrillMotorParts parts;

    public DrillMotorModel(ModelPart root) {
        super(root, RenderTypes::entityCutout);

        ModelPart main = root.getChild("main");
        ModelPart spinRod = main.getChild("spinRod");
        ModelPart rodGear = spinRod.getChild("rodGear");
        ModelPart connectingGear = spinRod.getChild("connectingGear");
        this.parts = new DrillMotorParts(main, spinRod, rodGear, connectingGear);
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition modelPartData = modelData.getRoot();
        PartDefinition main = modelPartData.addOrReplaceChild("main", CubeListBuilder.create().texOffs(15, 13).addBox(-4.9583F, -1.0455F, -4.0001F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(15, 23).addBox(2.0417F, -1.0455F, -4.0001F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(38, 33).addBox(-4.9583F, -2.0455F, 0.9999F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(47, 36).addBox(-4.9583F, -5.0455F, 1.9999F, 7.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(34, 29).addBox(-4.9583F, -7.0455F, -1.0001F, 7.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(39, 48).addBox(2.0417F, -6.0455F, -1.0001F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(28, 48).addBox(2.0417F, -5.0455F, -2.0001F, 1.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(17, 47).addBox(-4.9583F, -6.0455F, -2.0001F, 1.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(47, 40).addBox(-4.9583F, -5.0455F, -3.0001F, 7.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(28, 45).addBox(-4.9583F, -2.0455F, -2.0001F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(28, 41).addBox(-4.9583F, -2.0455F, -1.0001F, 7.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(46, 51).addBox(2.0417F, -3.0455F, -1.0001F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(15, 0).addBox(-5.9583F, -0.3955F, -5.0001F, 10.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(47, 44).addBox(0.0417F, 28.6045F, -2.0001F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(28, 36).addBox(0.0417F, 15.6045F, -1.0001F, 7.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-2.9583F, 1.6045F, -2.0001F, 3.0F, 41.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(18.7417F, -18.6045F, 13.0001F, 0.0F, 3.1416F, 0.0F));

        PartDefinition cube_r1 = main.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(5, 52).addBox(2.5F, -9.8919F, -11.7203F, 1.0F, 1.0F, 1.4125F, new CubeDeformation(0.0F))
                .texOffs(34, 13).addBox(-4.5F, -9.1848F, -12.4274F, 7.0F, 1.0F, 2.825F, new CubeDeformation(0.0F))
                .texOffs(10, 52).addBox(2.5F, -11.722F, -13.1345F, 1.0F, 1.4125F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(47, 23).addBox(-4.5F, -12.4274F, -13.8416F, 7.0F, 2.825F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 47).addBox(-4.5F, -12.4274F, -9.1848F, 7.0F, 2.825F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.4583F, 11.5295F, -0.0001F, -0.7854F, 0.0F, 0.0F));

        PartDefinition cube_r2 = main.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 52).addBox(2.5F, -9.8919F, 10.3078F, 1.0F, 1.0F, 1.4125F, new CubeDeformation(0.0F))
                .texOffs(39, 52).addBox(2.5F, -11.7203F, 12.1345F, 1.0F, 1.4125F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(47, 18).addBox(-4.5F, -12.4274F, 12.8416F, 7.0F, 2.825F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.4583F, 11.5295F, -0.0001F, 0.7854F, 0.0F, 0.0F));

        PartDefinition spinRod = main.addOrReplaceChild("spinRod", CubeListBuilder.create(), PartPose.offset(8.2583F, -4.0455F, -0.0001F));

        PartDefinition rodGear = spinRod.addOrReplaceChild("rodGear", CubeListBuilder.create().texOffs(15, 33).addBox(-7.75F, -0.5F, -0.5F, 10.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(34, 18).addBox(2.25F, -2.5F, -2.5F, 1.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(2.5333F, 0.0F, 0.0F));

        PartDefinition connectingGear = spinRod.addOrReplaceChild("connectingGear", CubeListBuilder.create().texOffs(15, 36).addBox(-0.5F, -2.5F, -2.5F, 1.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(5.2833F, 0.0F, 5.0F));
        return LayerDefinition.create(modelData, 64, 64);
    }

    @Override
    public void setupAnim(DrillMotorModelRenderState state) {
        super.setupAnim(state);
        this.parts.rodGear().xRot = state.clientMotorRotation;
        this.parts.connectingGear().xRot = -state.clientMotorRotation;
    }

    public record DrillMotorModelRenderState(float clientMotorRotation) {
    }

    public record DrillMotorParts(ModelPart main, ModelPart spinRod, ModelPart rodGear, ModelPart connectingGear) {
    }
}
