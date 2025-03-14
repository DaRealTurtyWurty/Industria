package dev.turtywurty.industria.renderer.block;

import dev.turtywurty.industria.blockentity.ClarifierBlockEntity;
import dev.turtywurty.industria.model.ClarifierModel;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;

public class ClarifierBlockEntityRenderer extends IndustriaBlockEntityRenderer<ClarifierBlockEntity> {
    private final ClarifierModel model;

    public ClarifierBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        super(context);

        this.model = new ClarifierModel(context.getLayerModelPart(ClarifierModel.LAYER_LOCATION));
    }

    @Override
    protected void onRender(ClarifierBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        this.model.getRootPart().render(matrices, vertexConsumers.getBuffer(this.model.getLayer(ClarifierModel.TEXTURE_LOCATION)), light, overlay);
    }
}
