package dev.turtywurty.industria.model;

import dev.turtywurty.industria.Industria;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public class WaterWheelModel extends Model<WaterWheelModel.WaterWheelModelRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Industria.id("water_wheel"), "main");
    public static final Identifier TEXTURE_LOCATION = Industria.id("textures/block/water_wheel.png");
    private final ModelPart rotor;

    public WaterWheelModel(ModelPart root) {
        super(root, RenderTypes::entityCutout);
        rotor = root.getChild("rotor");
    }

    public static LayerDefinition createMainLayer() {
        var meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition rotor = partdefinition.addOrReplaceChild("rotor",
            CubeListBuilder.create().texOffs(0, 0)
                .addBox(-8.5F, -20.5F, -5.0F, 17.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 48).addBox(-8.0F, -22.5F, -5.0F, 16.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(48, 0).addBox(-1.0F, -20.0F, -4.5F, 2.0F, 40.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-8.5F, -20.5F, 3.0F, 17.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(48, 0).addBox(-1.0F, -20.0F, 3.5F, 2.0F, 40.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 76).addBox(-4.0F, -4.0F, -5.0F, 8.0F, 8.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(48, 76).addBox(-2.0F, -2.0F, -8.0F, 4.0F, 4.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(96, 76).addBox(-3.0F, -3.0F, -6.0F, 6.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(96, 76).addBox(-3.0F, -3.0F, 5.0F, 6.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 16.0F, 0.0F));

        PartDefinition paddle_7_r1 = rotor.addOrReplaceChild("paddle_7_r1",
            CubeListBuilder.create().texOffs(0, 48)
                .addBox(-8.0F, -22.5F, -5.0F, 16.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-8.5F, -20.5F, 3.0F, 17.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-8.5F, -20.5F, -5.0F, 17.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 5.4978F));

        PartDefinition paddle_6_r1 = rotor.addOrReplaceChild("paddle_6_r1",
            CubeListBuilder.create().texOffs(0, 48)
                .addBox(-8.0F, -22.5F, -5.0F, 16.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-8.5F, -20.5F, 3.0F, 17.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-8.5F, -20.5F, -5.0F, 17.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 4.7124F));

        PartDefinition paddle_5_r1 = rotor.addOrReplaceChild("paddle_5_r1",
            CubeListBuilder.create().texOffs(0, 48)
                .addBox(-8.0F, -22.5F, -5.0F, 16.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-8.5F, -20.5F, 3.0F, 17.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-8.5F, -20.5F, -5.0F, 17.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 3.927F));

        PartDefinition paddle_4_r1 = rotor.addOrReplaceChild("paddle_4_r1",
            CubeListBuilder.create().texOffs(0, 48)
                .addBox(-8.0F, -22.5F, -5.0F, 16.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-8.5F, -20.5F, 3.0F, 17.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-8.5F, -20.5F, -5.0F, 17.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 3.1416F));

        PartDefinition paddle_3_r1 = rotor.addOrReplaceChild("paddle_3_r1",
            CubeListBuilder.create().texOffs(0, 48)
                .addBox(-8.0F, -22.5F, -5.0F, 16.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(48, 0).addBox(-1.0F, -20.0F, 3.5F, 2.0F, 40.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-8.5F, -20.5F, 3.0F, 17.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(48, 0).addBox(-1.0F, -20.0F, -4.5F, 2.0F, 40.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-8.5F, -20.5F, -5.0F, 17.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 2.3562F));

        PartDefinition paddle_2_r1 = rotor.addOrReplaceChild("paddle_2_r1",
            CubeListBuilder.create().texOffs(0, 48)
                .addBox(-8.0F, -22.5F, -5.0F, 16.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(48, 0).addBox(-1.0F, -20.0F, 3.5F, 2.0F, 40.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-8.5F, -20.5F, 3.0F, 17.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(48, 0).addBox(-1.0F, -20.0F, -4.5F, 2.0F, 40.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-8.5F, -20.5F, -5.0F, 17.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.5708F));

        PartDefinition paddle_1_r1 = rotor.addOrReplaceChild("paddle_1_r1",
            CubeListBuilder.create().texOffs(0, 48)
                .addBox(-8.0F, -22.5F, -5.0F, 16.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(48, 0).addBox(-1.0F, -20.0F, 3.5F, 2.0F, 40.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-8.5F, -20.5F, 3.0F, 17.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(48, 0).addBox(-1.0F, -20.0F, -4.5F, 2.0F, 40.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-8.5F, -20.5F, -5.0F, 17.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(WaterWheelModelRenderState state) {
        super.setupAnim(state);
        rotor.zRot = state.rotation();
    }

    public record WaterWheelModelRenderState(float rotation) {
    }
}
