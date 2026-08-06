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
        ModelPart main = root.getChild("main");
        ModelPart propellers = main.getChild("propellers");
        ModelPart wires = main.getChild("wires");

        this.parts = new WindTurbineParts(main, propellers, wires);
    }

    public static LayerDefinition createMainLayer() {
        var meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        PartDefinition main = partDefinition.addOrReplaceChild("main", CubeListBuilder.create().texOffs(0, 36).addBox(-2.0F, 3.0F, 2.5F, 4.0F, 32.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-6.0F, 51.0F, -1.5F, 12.0F, 6.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(16, 36).addBox(-3.0F, 35.0F, 1.5F, 6.0F, 14.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(48, 5).addBox(-8.0F, 52.0F, 2.5F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(48, 0).addBox(-5.0F, 52.0F, -2.5F, 10.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(40, 43).addBox(-3.0F, -3.0F, 1.5F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 18).addBox(-2.0F, -2.0F, -1.5F, 4.0F, 4.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(36, 18).addBox(-4.0F, 49.0F, 0.5F, 8.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(1, 19).addBox(-4.5F, 48.5F, 5.0F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(71, 10).addBox(-3.5F, 34.5F, 5.0F, 2.0F, 14.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(1, 23).addBox(-4.5F, 48.5F, 3.0F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(77, 10).addBox(-3.5F, 34.5F, 3.0F, 2.0F, 14.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(7, 19).addBox(-3.5F, 1.5F, 5.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(7, 22).addBox(-3.5F, 1.5F, 3.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -33.0F, -4.5F));

        main.addOrReplaceChild("Plug_r1", CubeListBuilder.create().texOffs(48, 5).addBox(-1.0F, -2.0F, -2.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.0F, 54.0F, 4.5F, 0.0F, 3.1416F, 0.0F));

        PartDefinition propellers = main.addOrReplaceChild("propellers", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.5708F));

        propellers.addOrReplaceChild("Blade_r1", CubeListBuilder.create().texOffs(40, 38).addBox(-18.0F, -2.0F, -0.5F, 16.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.5236F, 0.0F, -2.0944F));

        propellers.addOrReplaceChild("Blade_r2", CubeListBuilder.create().texOffs(40, 38).addBox(-18.0F, -2.0F, -0.5F, 16.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.5236F, 0.0F, 2.0944F));

        propellers.addOrReplaceChild("Blade_r3", CubeListBuilder.create().texOffs(40, 38).addBox(-18.0F, -2.0F, -0.5F, 16.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.5236F, 0.0F, 0.0F));

        PartDefinition wires = main.addOrReplaceChild("wires", CubeListBuilder.create().texOffs(4, 3).addBox(-6.5F, -6.5F, -1.5F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(4, 6).addBox(-6.5F, -6.5F, 0.5F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 57.0F, 4.5F));

        wires.addOrReplaceChild("Wire_r1", CubeListBuilder.create().texOffs(61, 22).addBox(-1.5F, -0.5F, -0.5F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(61, 19).addBox(-1.5F, -2.5F, -0.5F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, -2.0F, -5.0F, 0.0F, 3.1416F, 0.0F));

        return LayerDefinition.create(meshDefinition, 128, 128);
    }

    @Override
    public void setupAnim(WindTurbineModelRenderState state) {
        super.setupAnim(state);
        this.parts.propellers().zRot = state.propellerRotation;
    }

    public record WindTurbineModelRenderState(float propellerRotation) {
    }

    public record WindTurbineParts(ModelPart main, ModelPart propellers, ModelPart wires) {
    }
}
