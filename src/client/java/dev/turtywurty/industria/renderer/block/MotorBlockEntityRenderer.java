package dev.turtywurty.industria.renderer.block;

import dev.turtywurty.industria.blockentity.MotorBlockEntity;
import dev.turtywurty.industria.model.MotorModel;
import dev.turtywurty.industria.state.MotorRenderState;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.util.math.MatrixStack;
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
        state.rodRotation += state.rotationSpeed * state.tickProgress;

        queue.submitModel(this.model,
                new MotorModel.MotorModelRenderState(state.rodRotation),
                matrices, this.model.getLayer(MotorModel.TEXTURE_LOCATION),
                light, overlay, 0, state.crumblingOverlay);
    }
}
