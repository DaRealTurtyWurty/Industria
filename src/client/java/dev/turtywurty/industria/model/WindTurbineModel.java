package dev.turtywurty.industria.model;

import dev.turtywurty.industria.Industria;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.math.MatrixStack;

public class WindTurbineModel extends Model {
    public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(Industria.id("wind_turbine"), "main");

	private final Parts parts;

	public WindTurbineModel(ModelPart root) {
        super(RenderLayer::getEntitySolid);
        ModelPart main = root.getChild("main");
        ModelPart propellers = main.getChild("propellers");
        ModelPart propeller0 = propellers.getChild("propeller0");
		ModelPart propeller1 = propellers.getChild("propeller1");
        ModelPart propeller2 = propellers.getChild("propeller2");

        this.parts = new Parts(main, propellers, propeller0, propeller1, propeller2);
	}

	public static TexturedModelData createMainLayer() {
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		ModelPartData main = modelPartData.addChild("main", ModelPartBuilder.create().uv(0, 17).cuboid(-1.0F, 26.125F, -6.1875F, 2.0F, 2.0F, 1.0F, new Dilation(0.0F))
		.uv(0, 0).cuboid(-7.0F, -31.875F, -5.1875F, 14.0F, 2.0F, 14.0F, new Dilation(0.0F))
		.uv(0, 41).cuboid(-4.0F, -18.875F, -2.1875F, 8.0F, 21.0F, 8.0F, new Dilation(0.0F))
		.uv(0, 17).cuboid(-6.0F, -29.875F, -4.1875F, 12.0F, 11.0F, 12.0F, new Dilation(0.0F))
		.uv(33, 48).cuboid(-2.0F, 2.125F, -0.1875F, 4.0F, 22.0F, 4.0F, new Dilation(0.0F))
		.uv(38, 30).cuboid(-3.0F, 24.125F, -4.1875F, 6.0F, 6.0F, 11.0F, new Dilation(0.0F))
		.uv(0, 6).cuboid(-1.0F, -31.875F, 0.8125F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F))
		.uv(0, 0).cuboid(-2.0F, 25.125F, -5.1875F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 31.875F, -1.8125F));

		ModelPartData propellers = main.addChild("propellers", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 27.125F, -6.1875F));
		propellers.addChild("propeller0", ModelPartBuilder.create().uv(50, 48).cuboid(-1.0F, -1.0F, -1.0F, 2.0F, 22.0F, 1.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 0.0F, 0.0F));
		ModelPartData propeller1 = propellers.addChild("propeller1", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 0.0F, 0.0F));
		propeller1.addChild("cube_r1", ModelPartBuilder.create().uv(57, 0).cuboid(-1.0F, -1.0F, -1.0F, 2.0F, 22.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -2.1817F));
		ModelPartData propeller2 = propellers.addChild("propeller2", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 0.0F, 0.0F));
		propeller2.addChild("cube_r2", ModelPartBuilder.create().uv(57, 48).cuboid(-1.0F, -1.0F, -1.0F, 2.0F, 22.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 2.1817F));
		return TexturedModelData.of(modelData, 128, 128);
	}

	@Override
	public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, int color) {
		this.parts.main().render(matrices, vertexConsumer, light, overlay, color);
	}

    public Parts getParts() {
        return this.parts;
    }

    public record Parts(ModelPart main, ModelPart propellers, ModelPart propeller0, ModelPart propeller1, ModelPart propeller2) {}
}