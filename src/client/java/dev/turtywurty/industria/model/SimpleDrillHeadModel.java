package dev.turtywurty.industria.model;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.state.DrillRenderState;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;

public class SimpleDrillHeadModel extends Model<SimpleDrillHeadModel.SimpleDrillHeadModelRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Industria.id("simple_drill_head"), "main");

    private final DrillHeadParts parts;

    public SimpleDrillHeadModel(ModelPart root) {
        super(root, RenderTypes::entityCutout);

        ModelPart main = root.getChild("main");
        ModelPart clockwise = main.getChild("clockwise");
        ModelPart counterClockwise = main.getChild("counterClockwise");
        this.parts = new DrillHeadParts(main, clockwise, counterClockwise);
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition modelPartData = modelData.getRoot();
        PartDefinition main = modelPartData.addOrReplaceChild("main", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        main.addOrReplaceChild("clockwise", CubeListBuilder.create()
                .texOffs(0, 19)
                .addBox(-7.0F, -7.0F, -7.0F, 14.0F, 2.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(57, 19)
                .addBox(-1.0F, 5.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 51)
                .addBox(-3.0F, 1.0F, -3.0F, 6.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(49, 36)
                .addBox(-5.0F, -3.0F, -5.0F, 10.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -7.0F, 0.0F));

        main.addOrReplaceChild("counterClockwise", CubeListBuilder.create()
                .texOffs(25, 51)
                .addBox(-2.0F, 5.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(49, 49)
                .addBox(-4.0F, 1.0F, -4.0F, 8.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(0, 36)
                .addBox(-6.0F, -3.0F, -6.0F, 12.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0)
                .addBox(-8.0F, -7.0F, -8.0F, 16.0F, 2.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -9.0F, 0.0F));
        return LayerDefinition.create(modelData, 128, 128);
    }

    @Override
    public void setupAnim(SimpleDrillHeadModelRenderState state) {
        super.setupAnim(state);
        this.parts.clockwise().yRot = state.clockwiseYaw;
        this.parts.counterClockwise().yRot = state.counterClockwiseYaw;
    }

    public static void onRender(DrillRenderState state, PoseStack matrices, SubmitNodeCollector queue, Model<?> pModel, RenderType renderLayer, int light, int overlay) {
        SimpleDrillHeadModel model = (SimpleDrillHeadModel) pModel;

        float clockwiseYaw;
        float counterClockwiseYaw;
        if (state.isDrilling && !state.isPaused) {
            state.clockwiseRotation += 0.1F;
            state.counterClockwiseRotation -= 0.125F;
        } else {
            state.clockwiseRotation = (float) Math.clamp(state.clockwiseRotation - 0.1F, 0, Math.PI * 2);
            state.counterClockwiseRotation = (float) Math.clamp(state.counterClockwiseRotation + 0.125F, 0, Math.PI * 2);
        }
        clockwiseYaw = state.clockwiseRotation;
        counterClockwiseYaw = state.counterClockwiseRotation;

        matrices.pushPose();
        matrices.scale(0.9F, 0.9F, 0.9F);
        queue.submitModel(model, new SimpleDrillHeadModelRenderState(clockwiseYaw, counterClockwiseYaw), matrices, renderLayer, light, overlay, -1, state.breakProgress);
        matrices.popPose();
    }

    public DrillHeadParts getDrillHeadParts() {
        return this.parts;
    }

    public record SimpleDrillHeadModelRenderState(float clockwiseYaw, float counterClockwiseYaw) {
    }

    public record DrillHeadParts(ModelPart main, ModelPart clockwise, ModelPart counterClockwise) {
    }
}
