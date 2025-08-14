package dev.turtywurty.industria.renderer.block;

import dev.turtywurty.industria.basti_shit.BastiUtil;
import dev.turtywurty.industria.blockentity.CentrifugalConcentratorBlockEntity;
import dev.turtywurty.industria.blockentity.util.fluid.SyncingFluidStorage;
import dev.turtywurty.industria.model.CentrifugalConcentratorModel;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;

public class CentrifugalConcentratorBlockEntityRenderer extends IndustriaBlockEntityRenderer<CentrifugalConcentratorBlockEntity> {
    private final CentrifugalConcentratorModel model;

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
        if (progress == 0 || Double.isNaN(progress)) {
            entity.bowlRotation = 0f;
        } else {
            entity.bowlRotation = (entity.bowlRotation + (rpm / 60f / 20f) * tickDelta) % 360f;
        }

        this.model.getBowl().yaw = prevBowlYRot + entity.bowlRotation;
        this.model.render(matrices, vertexConsumers.getBuffer(this.model.getLayer(CentrifugalConcentratorModel.TEXTURE_LOCATION)), light, overlay);
        this.model.getBowl().yaw = prevBowlYRot;

        this.model.getCylinderTop().hidden = false;


        fluidRendering(entity, tickDelta, matrices, vertexConsumers, light, overlay);
    }

    private void fluidRendering(CentrifugalConcentratorBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
       SyncingFluidStorage fluidTank = entity.getInputFluidTank();
        if (fluidTank == null || fluidTank.isResourceBlank() || fluidTank.amount <= 0) return;

        FluidVariant fluidVariant = fluidTank.variant; //FluidVariant.of(Fluids.WATER);

        Sprite fluidSprite = FluidVariantRendering.getSprite(fluidVariant);
        VertexConsumer vc = vertexConsumers.getBuffer(RenderLayer.getTranslucent());

        int sides = 16;
        float outerRadius = 1.3f;
        float innerRadius = 0.2f;
        int fluidColor = FluidVariantRendering.getColor(fluidVariant, entity.getWorld(), entity.getPos());

        matrices.push();
        matrices.translate(0, -1, 0);

        for (int i = 0; i < sides; i += 1) {
            float angle0 = (float) (2.0 * Math.PI * i / sides);
            float angle1 = (float) (2.0 * Math.PI * (i + 1) / sides);

            angledFluidVertex(vc, matrices, fluidSprite, fluidColor, angle0, innerRadius, outerRadius, light, overlay);
            angledFluidVertex(vc, matrices, fluidSprite, fluidColor, angle0, outerRadius, outerRadius, light, overlay);
            angledFluidVertex(vc, matrices, fluidSprite, fluidColor, angle1, outerRadius, outerRadius, light, overlay);
            angledFluidVertex(vc, matrices, fluidSprite, fluidColor, angle1, innerRadius, outerRadius, light, overlay);
        }

        matrices.pop();
    }

    private void angledFluidVertex(VertexConsumer vc, MatrixStack matrixStack, Sprite sprite, int fluidColor, float angle, float radius, float uvSize, int light, int overlay) {
        float x = radius * (float) Math.cos(angle);
        float z = radius * (float) Math.sin(angle);

        float u = BastiUtil.map(x, -uvSize, uvSize, sprite.getMinU(), sprite.getMaxU());
        float v = BastiUtil.map(z, -uvSize, uvSize, sprite.getMinV(), sprite.getMaxV());

        vc.vertex(matrixStack.peek(), x, 0, z).color(fluidColor).texture(u, v).overlay(overlay).light(light).normal(0.0f, 1f, 0.0f);

    }
}
