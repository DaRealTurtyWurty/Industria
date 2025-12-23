package dev.turtywurty.industria.renderer.block;

import dev.turtywurty.industria.blockentity.MotorBlockEntity;
import dev.turtywurty.industria.model.MotorModel;
import dev.turtywurty.industria.state.MotorRenderState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class MotorBlockEntityRenderer extends IndustriaBlockEntityRenderer<MotorBlockEntity, MotorRenderState> {
    private final MotorModel model;

    public MotorBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
        this.model = new MotorModel(context.getLayerModelPart(MotorModel.LAYER_LOCATION));
    }

    @Override
    public MotorRenderState createRenderState() {
        return new MotorRenderState();
    }

    @Override
    public void updateRenderState(MotorBlockEntity blockEntity, MotorRenderState state, float tickProgress, Vec3d cameraPos, ModelCommandRenderer.@Nullable CrumblingOverlayCommand crumblingOverlay) {
        super.updateRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
        state.rodRotation = blockEntity.rodRotation;
        state.rotationSpeed = blockEntity.getRotationSpeed();
    }

    @Override
    public void onRender(MotorRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, int light, int overlay) {
        matrices.push();
        matrices.translate(0.5, 1.5, 0.5);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));

        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180 + switch (state.blockState.get(Properties.HORIZONTAL_FACING)) {
            case EAST -> 90;
            case SOUTH -> 180;
            case WEST -> 270;
            default -> 0;
        }));

        state.rodRotation += state.rotationSpeed * state.tickProgress;

        this.model.getMotorParts().spinRod().pitch = state.rodRotation;
        RenderLayer renderLayer = model.getLayer(MotorModel.TEXTURE_LOCATION);
        queue.submitModel(this.model, state, matrices, renderLayer, light, overlay, 0, state.crumblingOverlay);
        this.model.getMotorParts().spinRod().pitch = 0;

        matrices.pop();
    }
}
