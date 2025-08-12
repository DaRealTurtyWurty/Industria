package dev.turtywurty.industria.renderer.block;

import dev.turtywurty.industria.blockentity.ShakingTableBlockEntity;
import dev.turtywurty.industria.model.ShakingTableModel;
import dev.turtywurty.industria.util.ColorMode;
import dev.turtywurty.industria.util.DebugRenderingRegistry;
import dev.turtywurty.industria.util.InWorldFluidRenderingComponent;
import dev.turtywurty.industria.util.IndeterminateBoolean;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;

public class ShakingTableBlockEntityRenderer extends IndustriaBlockEntityRenderer<ShakingTableBlockEntity> {
    private final ShakingTableModel model;
    private final InWorldFluidRenderingComponent fluidRenderer = new InWorldFluidRenderingComponent();

    public ShakingTableBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        super(context);

        this.model = new ShakingTableModel(context.getLayerModelPart(ShakingTableModel.LAYER_LOCATION));
    }

    @Override
    protected void onRender(ShakingTableBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        float shakesPerSecond = entity.getRecipeFrequency();
        int progress = entity.getProgress();
        int maxProgress = entity.getMaxProgress();

        float shakeOffset = 0.0f;
        float previousOriginZ = this.model.getModelParts().table().originZ;
        if (progress > 0 && progress < maxProgress) {
            float time = tickDelta + entity.getWorld().getTime();
            float frequency = shakesPerSecond * (float) Math.PI;
            float shakeAmount = 2f;

            shakeOffset = (float) Math.sin(time * frequency) * shakeAmount;
            this.model.getModelParts().table().originZ += shakeOffset;
        }

        this.model.render(matrices, vertexConsumers.getBuffer(this.model.getLayer(ShakingTableModel.TEXTURE_LOCATION)), light, overlay);
        this.model.getModelParts().table().originZ = previousOriginZ;

        renderGutterFluids(entity, matrices, vertexConsumers, light, overlay, shakeOffset);
        renderSurfaceFluid(entity, matrices, vertexConsumers, light, overlay, shakeOffset);
    }

    @Override
    protected void postRender(ShakingTableBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        super.postRender(entity, tickDelta, matrices, vertexConsumers, light, overlay);
        if (DebugRenderingRegistry.debugRendering) {
            Box shakeBox = entity.createShakeBox();
            shakeBox = shakeBox.offset(-entity.getPos().getX(), -entity.getPos().getY(), -entity.getPos().getZ());
            if (shakeBox != null) {
                VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getLines());
                VertexRendering.drawBox(
                        matrices,
                        vertexConsumer,
                        shakeBox,
                        1.0f, 1.0f, 1.0f, 1.0f
                );
            }
        }
    }

    private void renderSurfaceFluid(ShakingTableBlockEntity entity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, float shakeOffset) {
        float progress = entity.getProgress() / (float) entity.getMaxProgress();
        if (progress <= 0.0f)
            return;

        float width = 35f / 16f;
        float waterWidth = 3 / 16f;

        // x1 > x2
        float x2Start = 1.0f + 1 / 16f;
        float x1Start = x2Start - waterWidth;

        float x1End = x2Start - width;

        float x1 = x1Start + (x1End - x1Start) * progress;

        float startHeight = 1f;
        float endHeight = 0.1f;

        float height = startHeight + (endHeight - startHeight) * progress;

        matrices.push();
        matrices.translate(0.0f, 0.0f, shakeOffset / 16f);
        this.fluidRenderer.render(entity.getInputFluidTank(),
                vertexConsumers, matrices,
                light, overlay,
                entity.getWorld(), entity.getPos(),
                x1, -4 / 16f, -2.0f - 2f / 16f,
                x2Start, height, 1 + 2f / 16f,
                0xFFFFFFFF, ColorMode.MULTIPLICATION,
                IndeterminateBoolean.TRUE);
        matrices.pop();
    }

    private void renderGutterFluids(ShakingTableBlockEntity entity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, float shakeOffset) {
        matrices.push();
        matrices.translate(0.0f, 0.0f, shakeOffset / 16f);
        {
            float x1 = 1f - 1f / 16f;
            float y1 = -2f / 16f;
            float z1 = -1f - 1f / 16f;
            float x2 = 1f + 1f / 16f;
            float z2 = -5f / 16f;
            this.fluidRenderer.render(entity.getInputFluidTank(),
                    vertexConsumers, matrices,
                    light, overlay,
                    entity.getWorld(), entity.getPos(),
                    x1, y1, z1, x2, 1.999f, z2,
                    0xFFFFFFFF, ColorMode.MULTIPLICATION,
                    IndeterminateBoolean.TRUE);
        }

        {
            float x1 = 1f - 1f / 16f;
            float y1 = -2f / 16f;
            float z1 = -1f / 16f;
            float x2 = 1f + 1f / 16f;
            float z2 = 1 + 2f / 16f;
            this.fluidRenderer.render(entity.getInputFluidTank(),
                    vertexConsumers, matrices,
                    light, overlay,
                    entity.getWorld(), entity.getPos(),
                    x1, y1, z1, x2, 1.999f, z2,
                    0xFFFFFFFF, ColorMode.MULTIPLICATION,
                    IndeterminateBoolean.TRUE);
        }

        matrices.pop();
    }
}
