package dev.turtywurty.industria.model;

import dev.turtywurty.industria.Industria;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public class DrillCableModel extends Model<DrillCableModel.DrillCableModelRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Industria.id("drill_cable"), "main");
    public static final Identifier TEXTURE_LOCATION = Industria.id("textures/block/drill_cable.png");

    private final ModelPart main;
    private final float baseXScale;
    private final float baseYScale;
    private final float baseZScale;

    public DrillCableModel(ModelPart root) {
        super(root, RenderTypes::entitySolid);

        this.main = root.getChild("main");
        this.baseXScale = this.main.xScale;
        this.baseYScale = this.main.yScale;
        this.baseZScale = this.main.zScale;
    }

    public static LayerDefinition getTexturedModelData() {
        var modelData = new MeshDefinition();

        PartDefinition modelPartData = modelData.getRoot();
        modelPartData.addOrReplaceChild("main", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-1.5F, -3.5F, -3.5F, 3.0F, 7.0F, 7.0F, new CubeDeformation(-0.05F)),
                PartPose.offset(0.1223F, -22.642F, 7.8787F));

        return LayerDefinition.create(modelData, 32, 32);
    }

    @Override
    public void setupAnim(DrillCableModelRenderState state) {
        super.setupAnim(state);
        float scale = 1.0f - state.cableScaleFactor;
        this.main.xRot = state.clientMotorRotation;
        this.main.xScale = this.baseXScale * scale;
        this.main.yScale = this.baseYScale * scale;
        this.main.zScale = this.baseZScale * scale;
    }

    public record DrillCableModelRenderState(float clientMotorRotation, float cableScaleFactor) {
    }
}
