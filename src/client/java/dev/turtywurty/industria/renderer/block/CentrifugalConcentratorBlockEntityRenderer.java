package dev.turtywurty.industria.renderer.block;

import dev.turtywurty.industria.blockentity.CentrifugalConcentratorBlockEntity;
import dev.turtywurty.industria.blockentity.util.fluid.SyncingFluidStorage;
import dev.turtywurty.industria.model.CentrifugalConcentratorModel;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Vector3f;

// TODO: Finish OBJLoader and use that for rendering
public class CentrifugalConcentratorBlockEntityRenderer extends IndustriaBlockEntityRenderer<CentrifugalConcentratorBlockEntity> {
    private static final int NUM_SPINNING_ITEMS = 5;

    private final CentrifugalConcentratorModel model;

    public CentrifugalConcentratorBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
        this.model = new CentrifugalConcentratorModel(context.getLayerModelPart(CentrifugalConcentratorModel.LAYER_LOCATION));
    }

    @Override
    protected void renderModel(CentrifugalConcentratorBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
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
    }

    @Override
    protected void onRender(CentrifugalConcentratorBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {

        float fluidYOffset = renderInputFluid(entity, tickDelta, matrices, vertexConsumers, light, overlay);

        matrices.push();
        matrices.translate(0, fluidYOffset, 0);
        renderInputItems(entity, tickDelta, matrices, vertexConsumers, light, overlay);
        matrices.pop();
    }

    private void renderInputItems(CentrifugalConcentratorBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        ItemStack stackInSlot = Items.DIAMOND.getDefaultStack(); //entity.getInputInventory().getStackInSlot(0);
        if(stackInSlot.isEmpty()) return;

        float radius = 1.1f;

        for (int i = 0; i < NUM_SPINNING_ITEMS; i++) {
            float angle = -entity.bowlRotation + (float) Math.TAU / NUM_SPINNING_ITEMS * i;

            float x = (float) Math.cos(angle) * radius;
            float y = (float) Math.sin(angle * 3) * 0.02f - 0.2f;
            float z = (float) Math.sin(angle) * radius;

            matrices.push();
            matrices.translate(x, y, z);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotation(angle + (float) Math.PI / 2f));
            matrices.scale(0.5f, 0.5f, 0.5f);
            this.context.getItemRenderer().renderItem(stackInSlot, ItemDisplayContext.NONE, light, overlay, matrices, vertexConsumers, entity.getWorld(), 0);

            Vector3f pos = matrixStackToWorldPosition(matrices);
            entity.getWorld().addParticleClient(ParticleTypes.BUBBLE, pos.x, pos.y + 0.3f, pos.z, 0, 0, 0);

            matrices.pop();
        }
    }


    private float renderInputFluid(CentrifugalConcentratorBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        SyncingFluidStorage fluidTank = entity.getInputFluidTank();
        if (fluidTank.isResourceBlank() || fluidTank.amount <= 0) return 0;

        FluidVariant fluidVariant = fluidTank.variant;
        Sprite fluidSprite = FluidVariantRendering.getSprite(fluidVariant);
        if (fluidSprite == null) return 0;

        RenderLayer renderLayer = RenderLayer.getItemEntityTranslucentCull(fluidSprite.getAtlasId());
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(renderLayer);

        float fillPercent = fluidTank.amount / (float) fluidTank.getCapacity();

        int sides = 16;
        float outerRadius = 19 / 16f;
        float innerRadius = fillPercent <= 7 / 16f ? 0 : 4 / 16f;

        int fluidColor = FluidVariantRendering.getColor(fluidVariant, entity.getWorld(), entity.getPos());

        float yMin = 1 / 16f;
        float yMax = 18 / 16f;
        float yOffset = MathHelper.lerp(fillPercent, yMin, yMax);

        matrices.push();
        matrices.translate(0, yOffset, 0);

        float angleOffset = (float) Math.PI / sides;
        for (int i = 0; i < sides; i += 1) {
            float angle0 = angleOffset - (float) (2.0 * Math.PI * i / sides);
            float angle1 = angleOffset - (float) (2.0 * Math.PI * (i + 1) / sides);

            angledFluidVertex(vertexConsumer, matrices, fluidSprite, fluidColor, angle0, innerRadius, outerRadius, light, overlay);
            angledFluidVertex(vertexConsumer, matrices, fluidSprite, fluidColor, angle0, outerRadius, outerRadius, light, overlay);
            angledFluidVertex(vertexConsumer, matrices, fluidSprite, fluidColor, angle1, outerRadius, outerRadius, light, overlay);
            angledFluidVertex(vertexConsumer, matrices, fluidSprite, fluidColor, angle1, innerRadius, outerRadius, light, overlay);
        }

        matrices.pop();

        return yOffset;
    }

    private void angledFluidVertex(VertexConsumer vc, MatrixStack matrixStack, Sprite sprite, int fluidColor, float angle, float radius, float uvSize, int light, int overlay) {
        float x = radius * MathHelper.cos(angle);
        float z = radius * MathHelper.sin(angle);

        float u = MathHelper.map(x, -uvSize, uvSize, sprite.getMinU(), sprite.getMaxU());
        float v = MathHelper.map(z, -uvSize, uvSize, sprite.getMinV(), sprite.getMaxV());

        vc.vertex(matrixStack.peek(), x, 0, z)
                .color(fluidColor)
                .texture(u, v)
                .overlay(overlay)
                .light(light)
                .normal(0.0f, 1f, 0.0f);
    }
}
