package dev.turtywurty.industria.renderer.block;

import dev.turtywurty.industria.blockentity.ExampleMultiblockControllerBlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;

public class ExampleMultiblockControllerBlockEntityRenderer extends IndustriaBlockEntityRenderer<ExampleMultiblockControllerBlockEntity> {
    public ExampleMultiblockControllerBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    protected void onRender(ExampleMultiblockControllerBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();
        matrices.translate(0, 1, 0);
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getDebugFilledBox());
        BlockPos entityPos = entity.getPos();
        for (BlockPos position : entity.getPositions()) {
            double relativeX = position.getX() - entityPos.getX();
            double relativeY = entityPos.getY() - position.getY();
            double relativeZ = entityPos.getZ() - position.getZ();

            VertexRendering.drawFilledBox(matrices, vertexConsumer,
                    relativeX - 0.5, relativeY - 0.5, relativeZ - 0.5,
                    relativeX + 0.5, relativeY + 0.5, relativeZ + 0.5,
                    0f, 1f, 0f, 0.5f);
        }
        matrices.pop();
    }
}
