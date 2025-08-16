package dev.turtywurty.industria.renderer.block;

import dev.turtywurty.industria.blockentity.ShakingTableBlockEntity;
import dev.turtywurty.industria.model.ShakingTableModel;
import dev.turtywurty.industria.util.ColorMode;
import dev.turtywurty.industria.util.DebugRenderingRegistry;
import dev.turtywurty.industria.util.InWorldFluidRenderingComponent;
import dev.turtywurty.industria.util.IndeterminateBoolean;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec2f;

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

        Vec2f fluidEnd = renderSurfaceFluid(entity, matrices, vertexConsumers, light, overlay, shakeOffset);

        renderItemStacks(entity, matrices, vertexConsumers, light, overlay, shakeOffset, fluidEnd.x, fluidEnd.y);
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

    // TODO: Figure out why the items start off centered in the middle of the table
    // and then move to the left side of the table when the shaking starts.
    private void renderItemStacks(ShakingTableBlockEntity entity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, float shakeOffset, float surfaceFluidX, float surfaceFluidY) {
        ItemStack processingStack = entity.getInputInventory().getStackInSlot(0);
        if (processingStack.isEmpty())
            return;

        matrices.push();
        matrices.translate(0f, 0f, shakeOffset / 16f);

        float depth = 52f / 16f;

        for (float i = 0; i < 4; i++) {
            float x = surfaceFluidX + 2 / 16f;
            float y = (4f / 16f - 0.125f / 16f) + surfaceFluidY;
            float z = (-13 / 16f) + i * (depth / 4f);

            matrices.push();
            matrices.translate(x, y, z);
            matrices.scale(0.5f, 0.5f, 0.5f);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
            this.context.getItemRenderer().renderItem(
                    processingStack,
                    ItemDisplayContext.GROUND,
                    light,
                    overlay,
                    matrices,
                    vertexConsumers,
                    entity.getWorld(),
                    0
            );
            matrices.pop();
        }

        matrices.pop();
    }

    private Vec2f renderSurfaceFluid(ShakingTableBlockEntity entity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, float shakeOffset) {
        float progress = entity.getProgress() / (float) entity.getMaxProgress();

        if (progress <= 0.0f) return new Vec2f(0, 0);

        float totalVolume = 3f;
        float width = 3f;

        float minX = 1 + 1/16f;
        float startX = minX - 0.4f;
        float endX = -(1 + 2/16f);

        float fluidX = MathHelper.lerp(progress, startX, endX);

        float height = totalVolume / width / Math.abs(minX - fluidX);

        matrices.push();
        matrices.translate(0, 0.0f, shakeOffset / 16f);

        this.fluidRenderer.render(entity.getInputFluidTank(),
                vertexConsumers, matrices,
                light, overlay,
                entity.getWorld(), entity.getPos(),
                fluidX, -4 / 16f, -2.0f - 2f / 16f,
                minX, height, 1 + 2f / 16f,
                0xFFFFFFFF, ColorMode.MULTIPLICATION,
                IndeterminateBoolean.TRUE);

        matrices.pop();

        float endY = -(height / 16f);
        return new Vec2f(fluidX, endY);
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
