package dev.turtywurty.industria.renderer.block;

import dev.turtywurty.industria.blockentity.ClarifierBlockEntity;
import dev.turtywurty.industria.blockentity.util.fluid.SyncingFluidStorage;
import dev.turtywurty.industria.model.ClarifierModel;
import dev.turtywurty.industria.util.InWorldFluidRenderingComponent;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class ClarifierBlockEntityRenderer extends IndustriaBlockEntityRenderer<ClarifierBlockEntity> {
    private static final GridPosition[] OUTPUT_ITEM_POSITIONS = new GridPosition[64];

    static {
        for (int i = 0; i < OUTPUT_ITEM_POSITIONS.length; i++) {
            OUTPUT_ITEM_POSITIONS[i] = getPosition(i, 14);
        }
    }

    private final ClarifierModel model;
    private final InWorldFluidRenderingComponent fluidRenderer = new InWorldFluidRenderingComponent();

    public ClarifierBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        super(context);

        this.model = new ClarifierModel(context.getLayerModelPart(ClarifierModel.LAYER_LOCATION));
    }

    private static GridPosition getPosition(int index, int width) {
        if (index < 0 || width < 1)
            throw new IllegalArgumentException("Invalid input");

        int y = index / width;
        int xIndex = index % width;
        int x;
        if (xIndex % 2 == 0) {
            x = xIndex / 2;              // Even: left side
        } else {
            x = width - 1 - (xIndex / 2); // Odd: right side
        }

        return new GridPosition(x, y);
    }

    @Override
    protected void onRender(ClarifierBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        this.model.getRootPart().render(matrices, vertexConsumers.getBuffer(this.model.getLayer(ClarifierModel.TEXTURE_LOCATION)), light, overlay);

        if (entity.getWorld() == null)
            return;

        renderInputFluid(entity, matrices, vertexConsumers, light, overlay);
        renderOutputFluid(entity, matrices, vertexConsumers, light, overlay);

        renderCurrentOutputItem(entity, matrices, vertexConsumers, light, overlay);
        renderOutputStack(entity, matrices, vertexConsumers, light, overlay);
    }

    private void renderOutputStack(ClarifierBlockEntity entity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        ItemStack outputStack = entity.getOutputInventory().getStack(0);
        if (outputStack.isEmpty())
            return;

        float scale = 0.05f;
        float zOffset = 11f / 16f + 10f / 16f + 2f / 16f;
        float startY = 0.75f - scale / 2 + 9f / 16f;

        for (int i = 0; i < MathHelper.clamp(outputStack.getCount(), 1, 64); i++) {
            matrices.push();

            GridPosition position = OUTPUT_ITEM_POSITIONS[i];
            float xOff = position.x * (scale + (0.0625f * scale)) - 0.345f;
            float yOff = position.y * (scale + (0.0625f * scale));
            matrices.translate(xOff, startY - yOff, zOffset);
            matrices.scale(scale, scale, scale);
            this.context.getItemRenderer().renderItem(outputStack, ItemDisplayContext.NONE, light, overlay, matrices, vertexConsumers, entity.getWorld(), 0);
            matrices.pop();
        }
    }

    // Thanks to Basti for the item rendering math
    private void renderCurrentOutputItem(ClarifierBlockEntity blockEntity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        ItemStack nextOutput = blockEntity.getNextOutputItemStack();

        if (nextOutput.isEmpty())
            return;

        float itemProgress = (float) blockEntity.getProgress() / blockEntity.getMaxProgress();

        float scale = 0.15f;
        float firstStretch = 11f / 16f;
        float rampStretch = 10f / 16f;
        float rampHeight = 9f / 16f;
        float finalStretch = 1f / 16f;

        float dz = 0;
        float dy = 0;
        float rotation = 0;
        if (itemProgress >= 0 && itemProgress < 0.6) {
            dz = MathHelper.map(itemProgress, 0, 0.6f, 0, firstStretch);
            dy = 0;
        }

        if (itemProgress >= 0.6 && itemProgress < 0.85) {
            float t = MathHelper.map(itemProgress, 0.6f, 0.85f, 0, 1);
            t = (float) (0.7 * Math.pow(t, 2) + 0.3 * t); // Curve t, so that t(0) = 0, t(1) = 1, t´(0) = 0.3
            dz = MathHelper.lerp(t, firstStretch, firstStretch + rampStretch);
            dy = MathHelper.lerp(t, 0, -rampHeight);

            rotation = (float) (MathHelper.lerp(t, 0, -Math.PI * 2));
        }

        if (itemProgress >= 0.85 && itemProgress < 1) {
            dz = MathHelper.map(itemProgress, 0.85f, 1f, firstStretch + rampStretch, firstStretch + rampStretch + finalStretch);
            dy = -rampHeight;
        }

        matrices.push();
        matrices.translate(0, 0.75 - scale / 2 - dy, 0 + dz);
        matrices.scale(scale, scale, scale);
        matrices.multiply(RotationAxis.POSITIVE_X.rotation(rotation));
        this.context.getItemRenderer().renderItem(nextOutput, ItemDisplayContext.NONE, light, overlay, matrices, vertexConsumers, blockEntity.getWorld(), 0);
        matrices.pop();
    }

    private void renderInputFluid(ClarifierBlockEntity entity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        SyncingFluidStorage fluidStorage = entity.getInputFluidTank();
        if (fluidStorage == null || fluidStorage.isResourceBlank() || fluidStorage.amount <= 0)
            return;

        FluidVariant fluidVariant = fluidStorage.getResource();

        long amount = fluidStorage.amount;
        float fluidProgress = (float) amount / (FluidConstants.BUCKET * 5);
        // fluidProgress = (float) (Math.sin(entity.getWorld().getTime() / 64.0) * 0.5 + 0.5);
        float fluidHeight = -0.625f + (fluidProgress * 1 + 1.999f / 16f);

        float size = 1.25f;
        if (fluidHeight < 0f)
            size = 0.5f;

        this.fluidRenderer.renderTopFaceOnly(fluidVariant,
                vertexConsumers, matrices,
                light, overlay,
                entity.getWorld(), entity.getPos(),
                -size, fluidHeight, -size,
                size, size);
    }

    private void renderOutputFluid(ClarifierBlockEntity entity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        SyncingFluidStorage fluidStorage = entity.getOutputFluidTank();
        if (fluidStorage == null || fluidStorage.isResourceBlank() || fluidStorage.amount <= 0)
            return;

        FluidVariant fluidVariant = fluidStorage.getResource();

        long amount = fluidStorage.amount;
        float fluidProgress = (float) amount / (FluidConstants.BUCKET * 5);
        // fluidProgress = (float) (Math.sin(entity.getWorld().getTime() / 64.0) * 0.5 + 0.5);
        float fluidHeight = -1.375f + (fluidProgress * 0.5f);

        this.fluidRenderer.renderTopFaceOnly(fluidVariant,
                vertexConsumers, matrices,
                light, overlay,
                entity.getWorld(), entity.getPos(),
                -0.375f, fluidHeight, -0.5f,
                0.375f, 1.4375f);

        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
        this.fluidRenderer.drawTiledXYQuadOnly(fluidVariant,
                vertexConsumers, matrices,
                light, overlay,
                entity.getWorld(), entity.getPos(),
                -0.375f, -1.375f, -1.4375f,
                0.375f, fluidHeight, -1.4375f);

        matrices.pop();
    }

    private record GridPosition(int x, int y) {
    }
}
