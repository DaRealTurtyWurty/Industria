package dev.turtywurty.industria.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.turtywurty.industria.blockentity.MotorBlockEntity;
import dev.turtywurty.industria.model.MotorModel;
import dev.turtywurty.industria.state.MotorRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class MotorBlockEntityRenderer extends IndustriaBlockEntityRenderer<MotorBlockEntity, MotorRenderState> {
    private final MotorModel model;

    public MotorBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        this.model = new MotorModel(context.bakeLayer(MotorModel.LAYER_LOCATION));
    }

    @Override
    public MotorRenderState createRenderState() {
        return new MotorRenderState();
    }

    @Override
    public void extractRenderState(MotorBlockEntity blockEntity, MotorRenderState state, float tickProgress, Vec3 cameraPos, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
        super.extractRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
        state.rodRotation = blockEntity.rodRotation;
        state.rotationSpeed = blockEntity.getRotationSpeed();
    }

    @Override
    public void onRender(MotorRenderState state, PoseStack matrices, SubmitNodeCollector queue, int light, int overlay) {
        state.rodRotation += state.rotationSpeed * state.tickProgress;

        queue.submitModel(this.model,
                new MotorModel.MotorModelRenderState(state.rodRotation),
                matrices, this.model.renderType(MotorModel.TEXTURE_LOCATION),
                light, overlay, 0, state.breakProgress);
    }
}
