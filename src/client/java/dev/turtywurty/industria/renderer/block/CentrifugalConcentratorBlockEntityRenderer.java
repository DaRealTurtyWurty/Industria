package dev.turtywurty.industria.renderer.block;

import dev.turtywurty.industria.blockentity.CentrifugalConcentratorBlockEntity;
import dev.turtywurty.industria.blockentity.util.fluid.SyncingFluidStorage;
import dev.turtywurty.industria.model.CentrifugalConcentratorModel;
import dev.turtywurty.industria.util.InWorldFluidRenderingComponent;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;

import java.util.function.UnaryOperator;

public class CentrifugalConcentratorBlockEntityRenderer extends IndustriaBlockEntityRenderer<CentrifugalConcentratorBlockEntity> {
    private final CentrifugalConcentratorModel model;
    private final InWorldFluidRenderingComponent fluidRenderer = new InWorldFluidRenderingComponent();

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

        renderInputFluid(entity, matrices, vertexConsumers, light, overlay);
    }

    private void renderInputFluid(CentrifugalConcentratorBlockEntity entity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        SyncingFluidStorage fluidTank = entity.getInputFluidTank();
        long fluidAmount = fluidTank.getAmount();
        if (fluidAmount <= 0)
            return;

        long capacity = fluidTank.getCapacity();
        FluidVariant fluidVariant = fluidTank.getResource();

        float y1 = -2 / 16f;
        float height = 1 + 5 / 16f;
        float y2 = y1 + (height * (fluidAmount / (float) capacity));

        float radius = 18/16f;

//        ShaderInit.uOuterInner.set(36f, 4f);
//        ShaderInit.uSides.set(16);

        this.fluidRenderer.renderTopFaceOnly(
                fluidVariant,
                vertexConsumers,
                matrices,
                light,
                overlay,
                entity.getWorld(),
                entity.getPos(),
                -radius,
                y2,
                -radius,
                radius,
                radius,
                UnaryOperator.identity()/*ShaderInit.CENTRIFUGAL_CONCENTRATOR_FRAMEBUFFER::getRenderLayer*/
        );
    }
}
