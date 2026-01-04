package dev.turtywurty.industria.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.turtywurty.industria.blockentity.CentrifugalConcentratorBlockEntity;
import dev.turtywurty.industria.model.CentrifugalConcentratorModel;
import dev.turtywurty.industria.state.CentrifugalConcentratorRenderState;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

// TODO: Finish OBJLoader and use that for rendering
public class CentrifugalConcentratorBlockEntityRenderer extends IndustriaBlockEntityRenderer<CentrifugalConcentratorBlockEntity, CentrifugalConcentratorRenderState> {
    private static final int NUM_SPINNING_ITEMS = 5;

    private final CentrifugalConcentratorModel model;

    public CentrifugalConcentratorBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        this.model = new CentrifugalConcentratorModel(context.bakeLayer(CentrifugalConcentratorModel.LAYER_LOCATION));
    }

    @Override
    public CentrifugalConcentratorRenderState createRenderState() {
        return new CentrifugalConcentratorRenderState();
    }

    @Override
    public void extractRenderState(CentrifugalConcentratorBlockEntity blockEntity, CentrifugalConcentratorRenderState state, float tickProgress, Vec3 cameraPos, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
        super.extractRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
        state.recipeRPM = blockEntity.getRecipeRPM();
        state.progress = blockEntity.getProgress();
        state.maxProgress = blockEntity.getMaxProgress();
        state.inputFluidTank = blockEntity.getInputFluidTank();
        state.updateItemRenderState(0, this, blockEntity, blockEntity.getInputInventory().getItem(0));

        float progress = state.progress / (float) state.maxProgress;
        if (progress == 0 || Double.isNaN(progress)) {
            state.bowlRotation = 0f;
        } else {
            state.bowlRotation = (blockEntity.bowlRotation * state.tickProgress) % 360f;
        }
    }

    @Override
    protected void onRender(CentrifugalConcentratorRenderState state, PoseStack matrices, SubmitNodeCollector queue, int light, int overlay) {
        queue.submitModel(this.model,
                state.bowlRotation,
                matrices, this.model.renderType(CentrifugalConcentratorModel.TEXTURE_LOCATION),
                light, overlay, 0, state.breakProgress);

        renderInputFluid(state, matrices, queue, light, overlay);
    }

    private void renderInputFluid(CentrifugalConcentratorRenderState state, PoseStack matrices, SubmitNodeCollector queue, int light, int overlay) {
        SingleFluidStorage fluidTank = state.inputFluidTank;
        if (fluidTank.isResourceBlank() || fluidTank.amount <= 0)
            return;

        FluidVariant fluidVariant = fluidTank.variant;
        TextureAtlasSprite fluidSprite = FluidVariantRendering.getSprite(fluidVariant);
        if (fluidSprite == null)
            return;

        RenderType renderLayer = RenderTypes.itemEntityTranslucentCull(fluidSprite.atlasLocation());
        Level world = Minecraft.getInstance().level;

        int sides = 16;
        float outerRadius = 19 / 16f;
        float innerRadius;
        int fluidColor = FluidVariantRendering.getColor(fluidVariant, world, state.blockPos);

        float fillPercent = fluidTank.amount / (float) fluidTank.getCapacity();

        // fillPercent = (float) (Math.sin(world.getTime() / world.getTickManager().getTickRate()) * 0.5f + 0.5f);

        if (fillPercent <= 7 / 16f) {
            innerRadius = 0;
        } else {
            innerRadius = 4 / 16f;
        }

        float yMin = 1 / 16f;
        float yMax = 18 / 16f;
        float yOffset = Mth.lerp(fillPercent, yMin, yMax);
        matrices.pushPose();
        matrices.translate(0, -yOffset, 0);

        float angleOffset = (float) Math.PI / sides;
        for (int i = 0; i < sides; i += 1) {
            float angle0 = angleOffset + (float) (2.0 * Math.PI * i / sides);
            float angle1 = angleOffset + (float) (2.0 * Math.PI * (i + 1) / sides);

            queue.submitCustomGeometry(matrices, renderLayer, (entry, vertexConsumer) -> {
                angledFluidVertex(vertexConsumer, entry, fluidSprite, fluidColor, angle0, innerRadius, outerRadius, light, overlay);
                angledFluidVertex(vertexConsumer, entry, fluidSprite, fluidColor, angle0, outerRadius, outerRadius, light, overlay);
                angledFluidVertex(vertexConsumer, entry, fluidSprite, fluidColor, angle1, outerRadius, outerRadius, light, overlay);
                angledFluidVertex(vertexConsumer, entry, fluidSprite, fluidColor, angle1, innerRadius, outerRadius, light, overlay);
            });
        }

        float radius = 1.1f;

        for (int i = 0; i < NUM_SPINNING_ITEMS; i++) {
            float angle = -state.bowlRotation + (float) Math.TAU / NUM_SPINNING_ITEMS * i;

            float x = (float) Math.cos(angle) * radius;
            float y = (float) Math.sin(angle * 3) * 0.02f + 0.225f;
            float z = (float) Math.sin(angle) * radius;

            matrices.pushPose();
            matrices.translate(x, y, z);
            matrices.mulPose(Axis.XP.rotationDegrees(180));
            matrices.mulPose(Axis.YP.rotation(angle + (float) Math.PI / 2f));
            matrices.scale(0.5f, 0.5f, 0.5f);
            state.renderItemRenderState(0, matrices, queue);

            Vector3f pos = localToWorldPosition(matrices);
            world.addParticle(ParticleTypes.BUBBLE, pos.x, pos.y + 0.25, pos.z, 0, 0, 0);

            matrices.popPose();
        }

        matrices.popPose();
    }

    private Vector3f localToWorldPosition(PoseStack matrices) {
        Vector3f pos = matrices.last().pose().transformPosition(0, 0, 0, new Vector3f());
        Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().position();

        return new Vector3f((float) (pos.x() + cameraPos.x), (float) (pos.y() + cameraPos.y), (float) (pos.z() + cameraPos.z));
    }

    private void angledFluidVertex(VertexConsumer vc, PoseStack.Pose entry, TextureAtlasSprite sprite, int fluidColor, float angle, float radius, float uvSize, int light, int overlay) {
        float x = radius * Mth.cos(angle);
        float z = radius * Mth.sin(angle);

        float u = Mth.map(x, -uvSize, uvSize, sprite.getU0(), sprite.getU1());
        float v = Mth.map(z, -uvSize, uvSize, sprite.getV0(), sprite.getV1());

        vc.addVertex(entry, x, 0, z)
                .setColor(fluidColor)
                .setUv(u, v)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(0.0f, 1f, 0.0f);
    }
}
