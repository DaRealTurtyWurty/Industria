package dev.turtywurty.industria.renderer.block;

import dev.turtywurty.industria.blockentity.DigesterBlockEntity;
import dev.turtywurty.industria.model.DigesterModel;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;

public class DigesterBlockEntityRenderer extends IndustriaBlockEntityRenderer<DigesterBlockEntity> {
    private final DigesterModel model;

    public DigesterBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        super(context);

        this.model = new DigesterModel(context.getLayerModelPart(DigesterModel.LAYER_LOCATION));
    }

    @Override
    protected void onRender(DigesterBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        this.model.getRootPart().render(matrices, vertexConsumers.getBuffer(this.model.getLayer(DigesterModel.TEXTURE_LOCATION)), light, overlay);
    }
}
