package dev.turtywurty.industria.model;

import dev.turtywurty.industria.Industria;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public class DrillCableModel extends Model<DrillCableModel.DrillCableModelRenderState> {
    public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(Industria.id("drill_cable"), "main");
    public static final Identifier TEXTURE_LOCATION = Industria.id("textures/block/drill_cable.png");

    private final ModelPart main;
    private final float baseXScale;
    private final float baseYScale;
    private final float baseZScale;

    public DrillCableModel(ModelPart root) {
        super(root, RenderLayers::entitySolid);

        this.main = root.getChild("main");
        this.baseXScale = this.main.xScale;
        this.baseYScale = this.main.yScale;
        this.baseZScale = this.main.zScale;
    }

    public static TexturedModelData getTexturedModelData() {
        var modelData = new ModelData();

        ModelPartData modelPartData = modelData.getRoot();
        modelPartData.addChild("main", ModelPartBuilder.create()
                        .uv(0, 0)
                        .cuboid(-1.5F, -3.5F, -3.5F, 3.0F, 7.0F, 7.0F, new Dilation(-0.05F)),
                ModelTransform.origin(0.1223F, -22.642F, 7.8787F));

        return TexturedModelData.of(modelData, 32, 32);
    }

    @Override
    public void setAngles(DrillCableModelRenderState state) {
        super.setAngles(state);
        float scale = 1.0f - state.cableScaleFactor;
        this.main.pitch = state.clientMotorRotation;
        this.main.xScale = this.baseXScale * scale;
        this.main.yScale = this.baseYScale * scale;
        this.main.zScale = this.baseZScale * scale;
    }

    public record DrillCableModelRenderState(float clientMotorRotation, float cableScaleFactor) {
    }
}
