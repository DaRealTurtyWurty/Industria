package dev.turtywurty.industria.model;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.state.DrillRenderState;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.math.MatrixStack;

public class SimpleDrillHeadModel extends Model<SimpleDrillHeadModel.SimpleDrillHeadModelRenderState> {
    public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(Industria.id("simple_drill_head"), "main");

    private final DrillHeadParts parts;

    public SimpleDrillHeadModel(ModelPart root) {
        super(root, RenderLayers::entityCutout);

        ModelPart main = root.getChild("main");
        ModelPart clockwise = main.getChild("clockwise");
        ModelPart counterClockwise = main.getChild("counterClockwise");
        this.parts = new DrillHeadParts(main, clockwise, counterClockwise);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        ModelPartData main = modelPartData.addChild("main", ModelPartBuilder.create(), ModelTransform.origin(0.0F, 24.0F, 0.0F));

        main.addChild("clockwise", ModelPartBuilder.create()
                .uv(0, 19)
                .cuboid(-7.0F, -7.0F, -7.0F, 14.0F, 2.0F, 14.0F, new Dilation(0.0F))
                .uv(57, 19)
                .cuboid(-1.0F, 5.0F, -1.0F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F))
                .uv(0, 51)
                .cuboid(-3.0F, 1.0F, -3.0F, 6.0F, 2.0F, 6.0F, new Dilation(0.0F))
                .uv(49, 36)
                .cuboid(-5.0F, -3.0F, -5.0F, 10.0F, 2.0F, 10.0F, new Dilation(0.0F)), ModelTransform.origin(0.0F, -7.0F, 0.0F));

        main.addChild("counterClockwise", ModelPartBuilder.create()
                .uv(25, 51)
                .cuboid(-2.0F, 5.0F, -2.0F, 4.0F, 2.0F, 4.0F, new Dilation(0.0F))
                .uv(49, 49)
                .cuboid(-4.0F, 1.0F, -4.0F, 8.0F, 2.0F, 8.0F, new Dilation(0.0F))
                .uv(0, 36)
                .cuboid(-6.0F, -3.0F, -6.0F, 12.0F, 2.0F, 12.0F, new Dilation(0.0F))
                .uv(0, 0)
                .cuboid(-8.0F, -7.0F, -8.0F, 16.0F, 2.0F, 16.0F, new Dilation(0.0F)), ModelTransform.origin(0.0F, -9.0F, 0.0F));
        return TexturedModelData.of(modelData, 128, 128);
    }

    @Override
    public void setAngles(SimpleDrillHeadModelRenderState state) {
        super.setAngles(state);
        this.parts.clockwise().yaw = state.clockwiseYaw;
        this.parts.counterClockwise().yaw = state.counterClockwiseYaw;
    }

    public static void onRender(DrillRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, Model<?> pModel, RenderLayer renderLayer, int light, int overlay) {
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

        matrices.push();
        matrices.scale(0.9F, 0.9F, 0.9F);
        queue.submitModel(model, new SimpleDrillHeadModelRenderState(clockwiseYaw, counterClockwiseYaw), matrices, renderLayer, light, overlay, -1, state.crumblingOverlay);
        matrices.pop();
    }

    public DrillHeadParts getDrillHeadParts() {
        return this.parts;
    }

    public record SimpleDrillHeadModelRenderState(float clockwiseYaw, float counterClockwiseYaw) {
    }

    public record DrillHeadParts(ModelPart main, ModelPart clockwise, ModelPart counterClockwise) {
    }
}
