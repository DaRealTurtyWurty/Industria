package dev.turtywurty.industria.model;

import dev.turtywurty.industria.Industria;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public class WindTurbineModel extends Model<WindTurbineModel.WindTurbineModelRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Industria.id("wind_turbine"), "main");
    public static final Identifier TEXTURE_LOCATION = Industria.id("textures/block/wind_turbine.png");

    private final WindTurbineParts parts;

    public WindTurbineModel(ModelPart root) {
        super(root, RenderTypes::entitySolid);
        ModelPart core = root.getChild("core");
        ModelPart propellers = root.getChild("propellers");
        ModelPart propeller0 = propellers.getChild("propeller0");
        ModelPart propeller1 = propellers.getChild("propeller1");
        ModelPart propeller2 = propellers.getChild("propeller2");

        this.parts = new WindTurbineParts(core, propellers, propeller0, propeller1, propeller2);
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition modelPartData = modelData.getRoot();
        PartDefinition core = modelPartData.addOrReplaceChild("core", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -28.125F, -10.0625F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-7.0F, 29.875F, -7.0625F, 14.0F, 2.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(0, 41).addBox(-4.0F, -2.125F, -4.0625F, 8.0F, 21.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(0, 17).addBox(-6.0F, 18.875F, -6.0625F, 12.0F, 11.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(33, 48).addBox(-2.0F, -24.125F, -2.0625F, 4.0F, 22.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(38, 30).addBox(-3.0F, -30.125F, -6.0625F, 6.0F, 6.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(0, 17).addBox(-1.0F, 29.875F, -1.0625F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 6).addBox(-2.0F, -29.125F, -7.0625F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -7.875F, 0.0625F));

        PartDefinition propellers = modelPartData.addOrReplaceChild("propellers", CubeListBuilder.create(), PartPose.offset(0.0F, -35.0F, -8.0F));

        PartDefinition propeller0 = propellers.addOrReplaceChild("propeller0", CubeListBuilder.create().texOffs(50, 48).addBox(-1.0F, -21.0F, -1.0F, 2.0F, 22.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition propeller1 = propellers.addOrReplaceChild("propeller1", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition cube_r1 = propeller1.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(57, 0).addBox(-1.0F, -21.0F, -1.0F, 2.0F, 22.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -2.1817F));

        PartDefinition propeller2 = propellers.addOrReplaceChild("propeller2", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition cube_r2 = propeller2.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(57, 48).addBox(-1.0F, -21.0F, -1.0F, 2.0F, 22.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 2.1817F));
        return LayerDefinition.create(modelData, 128, 128);
    }

    @Override
    public void setupAnim(WindTurbineModelRenderState state) {
        super.setupAnim(state);
        this.parts.propellers().zRot = state.propellerRotation;
    }

    public record WindTurbineModelRenderState(float propellerRotation) {
    }

    public record WindTurbineParts(ModelPart core, ModelPart propellers, ModelPart propeller0, ModelPart propeller1,
                                   ModelPart propeller2) {
    }
}
