package dev.turtywurty.industria.renderer.block;

import dev.turtywurty.industria.blockentity.CentrifugalConcentratorBlockEntity;
import dev.turtywurty.industria.model.CentrifugalConcentratorModel;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;

public class CentrifugalConcentratorBlockEntityRenderer extends IndustriaBlockEntityRenderer<CentrifugalConcentratorBlockEntity> {
    private final CentrifugalConcentratorModel model;

    public CentrifugalConcentratorBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
        this.model = new CentrifugalConcentratorModel(context.getLayerModelPart(CentrifugalConcentratorModel.LAYER_LOCATION));
    }

    @Override
    protected void onRender(CentrifugalConcentratorBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        this.model.getCylinderTop().hidden = true;

        int rpm = entity.getRecipeRPM();
        float progress = entity.getProgress() / (float) entity.getMaxProgress();
        float prevBowlYRot = this.model.getBowl().yaw;
        if(progress == 0 || Double.isNaN(progress)) {
            entity.bowlRotation = 0f;
        } else {
            entity.bowlRotation = (entity.bowlRotation + (rpm / 60f / 20f) * tickDelta) % 360f;
        }

        this.model.getBowl().yaw = prevBowlYRot + entity.bowlRotation;
        this.model.render(matrices, vertexConsumers.getBuffer(this.model.getLayer(CentrifugalConcentratorModel.TEXTURE_LOCATION)), light, overlay);
        this.model.getBowl().yaw = prevBowlYRot;

        this.model.getCylinderTop().hidden = false;
    }
}
