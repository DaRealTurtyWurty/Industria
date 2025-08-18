package dev.turtywurty.industria.renderer.block;

import dev.turtywurty.industria.blockentity.ArcFurnaceBlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;

public class ArcFurnaceBlockEntityRenderer extends IndustriaBlockEntityRenderer<ArcFurnaceBlockEntity> {
    public ArcFurnaceBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    protected void renderModel(ArcFurnaceBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {

    }

    @Override
    protected void onRender(ArcFurnaceBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {

    }
}
