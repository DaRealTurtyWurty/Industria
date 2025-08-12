package dev.turtywurty.industria.model;

import dev.turtywurty.industria.Industria;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public class WindTurbineModel extends Model {
    public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(Industria.id("wind_turbine"), "main");
    public static final Identifier TEXTURE_LOCATION = Industria.id("textures/block/wind_turbine.png");

    private final WindTurbineParts parts;

    public WindTurbineModel(ModelPart root) {
        super(root, RenderLayer::getEntitySolid);
        ModelPart core = root.getChild("core");
        ModelPart propellers = root.getChild("propellers");
        ModelPart propeller0 = propellers.getChild("propeller0");
        ModelPart propeller1 = propellers.getChild("propeller1");
        ModelPart propeller2 = propellers.getChild("propeller2");

        this.parts = new WindTurbineParts(core, propellers, propeller0, propeller1, propeller2);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        ModelPartData core = modelPartData.addChild("core", ModelPartBuilder.create().uv(0, 0).cuboid(-1.0F, -28.125F, -10.0625F, 2.0F, 2.0F, 3.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-7.0F, 29.875F, -7.0625F, 14.0F, 2.0F, 14.0F, new Dilation(0.0F))
                .uv(0, 41).cuboid(-4.0F, -2.125F, -4.0625F, 8.0F, 21.0F, 8.0F, new Dilation(0.0F))
                .uv(0, 17).cuboid(-6.0F, 18.875F, -6.0625F, 12.0F, 11.0F, 12.0F, new Dilation(0.0F))
                .uv(33, 48).cuboid(-2.0F, -24.125F, -2.0625F, 4.0F, 22.0F, 4.0F, new Dilation(0.0F))
                .uv(38, 30).cuboid(-3.0F, -30.125F, -6.0625F, 6.0F, 6.0F, 11.0F, new Dilation(0.0F))
                .uv(0, 17).cuboid(-1.0F, 29.875F, -1.0625F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F))
                .uv(0, 6).cuboid(-2.0F, -29.125F, -7.0625F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F)), ModelTransform.origin(0.0F, -7.875F, 0.0625F));

        ModelPartData propellers = modelPartData.addChild("propellers", ModelPartBuilder.create(), ModelTransform.origin(0.0F, -35.0F, -8.0F));

        ModelPartData propeller0 = propellers.addChild("propeller0", ModelPartBuilder.create().uv(50, 48).cuboid(-1.0F, -21.0F, -1.0F, 2.0F, 22.0F, 1.0F, new Dilation(0.0F)), ModelTransform.origin(0.0F, 0.0F, 0.0F));

        ModelPartData propeller1 = propellers.addChild("propeller1", ModelPartBuilder.create(), ModelTransform.origin(0.0F, 0.0F, 0.0F));

        ModelPartData cube_r1 = propeller1.addChild("cube_r1", ModelPartBuilder.create().uv(57, 0).cuboid(-1.0F, -21.0F, -1.0F, 2.0F, 22.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -2.1817F));

        ModelPartData propeller2 = propellers.addChild("propeller2", ModelPartBuilder.create(), ModelTransform.origin(0.0F, 0.0F, 0.0F));

        ModelPartData cube_r2 = propeller2.addChild("cube_r2", ModelPartBuilder.create().uv(57, 48).cuboid(-1.0F, -21.0F, -1.0F, 2.0F, 22.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 2.1817F));
        return TexturedModelData.of(modelData, 128, 128);
    }

    public WindTurbineParts getWindTurbineParts() {
        return this.parts;
    }

    public record WindTurbineParts(ModelPart core, ModelPart propellers, ModelPart propeller0, ModelPart propeller1,
                                   ModelPart propeller2) {
    }
}