package dev.turtywurty.industria.renderer.block;

import dev.turtywurty.industria.blockentity.MotorBlockEntity;
import dev.turtywurty.industria.model.MotorModel;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

public class MotorBlockEntityRenderer extends IndustriaBlockEntityRenderer<MotorBlockEntity> {

    private final MotorModel model;

    public MotorBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
        this.model = new MotorModel(context.getLayerModelPart(MotorModel.LAYER_LOCATION));
    }

    @Override
    protected void renderModel(MotorBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        float rotationSpeed = entity.getRotationSpeed();
        entity.rodRotation += rotationSpeed * tickDelta;

        this.model.getMotorParts().spinRod().pitch = entity.rodRotation;
        model.render(matrices, vertexConsumers.getBuffer(model.getLayer(MotorModel.TEXTURE_LOCATION)), light, overlay);
        this.model.getMotorParts().spinRod().pitch = 0;
    }

    @Override
    protected void onRender(MotorBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {

    }
}
