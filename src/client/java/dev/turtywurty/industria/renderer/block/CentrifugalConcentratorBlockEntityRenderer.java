package dev.turtywurty.industria.renderer.block;

import com.mojang.blaze3d.vertex.VertexFormat;
import dev.turtywurty.industria.blockentity.CentrifugalConcentratorBlockEntity;
import dev.turtywurty.industria.blockentity.util.fluid.SyncingFluidStorage;
import dev.turtywurty.industria.model.CentrifugalConcentratorModel;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.TriState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.jbox2d.common.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector4f;

// TODO: Finish OBJLoader and use that for rendering
public class CentrifugalConcentratorBlockEntityRenderer extends IndustriaBlockEntityRenderer<CentrifugalConcentratorBlockEntity> {
    private static final int NUM_SPINNING_ITEMS = 5;

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

        renderInputFluid(entity, tickDelta, matrices, vertexConsumers, light, overlay);
    }

    private void renderInputFluid(CentrifugalConcentratorBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        SyncingFluidStorage fluidTank = entity.getInputFluidTank();
        if (fluidTank.isResourceBlank() || fluidTank.amount <= 0) return;


        FluidVariant fluidVariant = fluidTank.variant;
        Sprite fluidSprite = FluidVariantRendering.getSprite(fluidVariant);
        if (fluidSprite == null)
            return;

        RenderLayer renderLayer = RenderLayer.getItemEntityTranslucentCull(fluidSprite.getAtlasId());
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(renderLayer);

        int sides = 16;
        float outerRadius = 19 / 16f;
        float innerRadius = 4 / 16f;
        int fluidColor = FluidVariantRendering.getColor(fluidVariant, entity.getWorld(), entity.getPos());

        float fillPercent = fluidTank.amount / (float) fluidTank.getCapacity();

        // fillPercent = (float) (Math.sin(entity.getWorld().getTime() / entity.getWorld().getTickManager().getTickRate()) * 0.5f + 0.5f);

        if (fillPercent <= 7 / 16f) {
            innerRadius = 0;
        }

        float yMin = 1 / 16f;
        float yMax = 18 / 16f;
        float yOffset = MathHelper.lerp(fillPercent, yMin, yMax);
        matrices.push();
        matrices.translate(0, -yOffset, 0);

        float angleOffset = (float) Math.PI / sides;
        for (int i = 0; i < sides; i += 1) {
            float angle0 = angleOffset + (float) (2.0 * Math.PI * i / sides);
            float angle1 = angleOffset + (float) (2.0 * Math.PI * (i + 1) / sides);

            angledFluidVertex(vertexConsumer, matrices, fluidSprite, fluidColor, angle0, innerRadius, outerRadius, light, overlay);
            angledFluidVertex(vertexConsumer, matrices, fluidSprite, fluidColor, angle0, outerRadius, outerRadius, light, overlay);
            angledFluidVertex(vertexConsumer, matrices, fluidSprite, fluidColor, angle1, outerRadius, outerRadius, light, overlay);
            angledFluidVertex(vertexConsumer, matrices, fluidSprite, fluidColor, angle1, innerRadius, outerRadius, light, overlay);
        }

        ItemStack stackInSlot = Items.DIAMOND.getDefaultStack(); //entity.getInputInventory().getStackInSlot(0);
        float radius = 1.1f;

        for (int i = 0; i < NUM_SPINNING_ITEMS; i++) {
            float angle = -entity.bowlRotation + (float) Math.TAU / NUM_SPINNING_ITEMS * i;

            float x = (float) Math.cos(angle) * radius;
            float y = (float) Math.sin(angle * 3) * 0.02f + 0.225f;
            float z = (float) Math.sin(angle) * radius;

            matrices.push();
            matrices.translate(x, y, z);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotation(angle + (float) Math.PI / 2f));
            matrices.scale(0.5f, 0.5f, 0.5f);
            this.context.getItemRenderer().renderItem(stackInSlot, ItemDisplayContext.NONE, light, overlay, matrices, vertexConsumers, entity.getWorld(), 0);


            Vector3f pos = localToWorldPosition(matrices);
            entity.getWorld().addParticleClient(ParticleTypes.BUBBLE, pos.x, pos.y + 0.25, pos.z, 0, 0, 0);

            matrices.pop();
        }

        matrices.pop();
    }

    private Vector3f localToWorldPosition(MatrixStack matrices) {
        Vector3f pos = matrices.peek().getPositionMatrix().transformPosition(0, 0, 0, new Vector3f());
        Vec3d cameraPos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();

        return new Vector3f((float) (pos.x() + cameraPos.x), (float) (pos.y() + cameraPos.y), (float) (pos.z() + cameraPos.z));
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
