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
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.util.math.RotationAxis;

public class ClarifierBlockEntityRenderer extends IndustriaBlockEntityRenderer<ClarifierBlockEntity> {
    private final ClarifierModel model;
    private final InWorldFluidRenderingComponent fluidRenderer = new InWorldFluidRenderingComponent();

    public ClarifierBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        super(context);

        this.model = new ClarifierModel(context.getLayerModelPart(ClarifierModel.LAYER_LOCATION));
    }

    @Override
    protected void onRender(ClarifierBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        this.model.getRootPart().render(matrices, vertexConsumers.getBuffer(this.model.getLayer(ClarifierModel.TEXTURE_LOCATION)), light, overlay);

        renderInputFluid(entity, matrices, vertexConsumers, light, overlay);
        renderOutputFluid(entity, matrices, vertexConsumers, light, overlay);

        renderOutputItem(entity, matrices, vertexConsumers, light, overlay);
    }

    private void renderOutputItem(ClarifierBlockEntity blockEntity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        ItemStack nextOutput = blockEntity.getNextOutputItemStack();
        nextOutput = Items.COBBLESTONE.getDefaultStack();

        int progress = (int) (blockEntity.getWorld().getTime() % 200);
        int maxProgress = 200;

        if (nextOutput.isEmpty()/* || progress <= 0 || maxProgress <= 0*/)
            return;

        float itemProgress = (float) progress / maxProgress;

        float firstStretch = 11f / 16f;
        float rampStretch = 8f / 16f;
        float finalStretch = 3f / 16f;
        float scale = 0.125f;

        matrices.push();
        matrices.translate(0, 0.75f - (0.5f * scale) , 0);
        if(itemProgress < 0.5f) {
            
        }

        matrices.scale(scale, scale, scale);
        this.context.getItemRenderer().renderItem(nextOutput, ModelTransformationMode.NONE, light, overlay, matrices, vertexConsumers, blockEntity.getWorld(), 0);
        matrices.pop();
    }

    private void renderInputFluid(ClarifierBlockEntity entity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        SyncingFluidStorage fluidStorage = entity.getInputFluidTank();
        if (fluidStorage == null || fluidStorage.isResourceBlank() || fluidStorage.amount <= 0)
            return;

        FluidVariant fluidVariant = fluidStorage.getResource();

        long amount = fluidStorage.amount;
        float fluidProgress = (float) amount / (FluidConstants.BUCKET * 5);
        //fluidProgress = (float) (Math.sin(entity.getWorld().getTime() / 64.0) * 0.5 + 0.5);
        float fluidHeight = -0.625f + (fluidProgress * 1 + 1.999f/16f);

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
        //fluidProgress = (float) (Math.sin(entity.getWorld().getTime() / 64.0) * 0.5 + 0.5);
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
}
