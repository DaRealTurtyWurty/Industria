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

        SyncingFluidStorage fluidStorage = entity.getInputFluidTank();
        if(fluidStorage == null || fluidStorage.isResourceBlank() || fluidStorage.amount <= 0)
            return;

        FluidVariant fluidVariant = fluidStorage.getResource();

        long amount = fluidStorage.amount;
        float fluidProgress = (float) amount / FluidConstants.BUCKET;
        //fluidProgress = (float) (Math.sin(entity.getWorld().getTime() / 64.0) * 0.5 + 0.5);
        float fluidHeight = -0.625f + (fluidProgress);

        float size = 1.25f;
        if(fluidHeight < 0f)
            size = 0.5f;

        this.fluidRenderer.renderTopFaceOnly(fluidVariant,
                vertexConsumers, matrices,
                light, overlay,
                entity.getWorld(), entity.getPos(),
                -size, fluidHeight, -size,
                size, size);
    }
}
