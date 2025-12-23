package dev.turtywurty.industria.renderer.block;

import dev.turtywurty.industria.blockentity.CentrifugalConcentratorBlockEntity;
import dev.turtywurty.industria.model.CentrifugalConcentratorModel;
import dev.turtywurty.industria.state.CentrifugalConcentratorRenderState;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

// TODO: Finish OBJLoader and use that for rendering
public class CentrifugalConcentratorBlockEntityRenderer extends IndustriaBlockEntityRenderer<CentrifugalConcentratorBlockEntity, CentrifugalConcentratorRenderState> {
    private static final int NUM_SPINNING_ITEMS = 5;

    private final CentrifugalConcentratorModel model;

    public CentrifugalConcentratorBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
        this.model = new CentrifugalConcentratorModel(context.getLayerModelPart(CentrifugalConcentratorModel.LAYER_LOCATION));
    }

    @Override
    public CentrifugalConcentratorRenderState createRenderState() {
        return new CentrifugalConcentratorRenderState();
    }

    @Override
    public void updateRenderState(CentrifugalConcentratorBlockEntity blockEntity, CentrifugalConcentratorRenderState state, float tickProgress, Vec3d cameraPos, ModelCommandRenderer.@Nullable CrumblingOverlayCommand crumblingOverlay) {
        super.updateRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
        state.recipeRPM = blockEntity.getRecipeRPM();
        state.progress = blockEntity.getProgress();
        state.maxProgress = blockEntity.getMaxProgress();
        state.inputFluidTank = blockEntity.getInputFluidTank();
        state.updateItemRenderState(0, this, blockEntity, blockEntity.getInputInventory().getStackInSlot(0));
    }

    @Override
    protected void onRender(CentrifugalConcentratorRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, int light, int overlay) {
        float progress = state.progress / (float) state.maxProgress;
        if (progress == 0 || Double.isNaN(progress)) {
            state.bowlRotation = 0f;
        } else {
            state.bowlRotation = (state.bowlRotation + (state.recipeRPM / 60f / 20f) * state.tickProgress) % 360f;
        }

        queue.submitModel(this.model,
                state.bowlRotation,
                matrices, this.model.getLayer(CentrifugalConcentratorModel.TEXTURE_LOCATION),
                light, overlay, 0, state.crumblingOverlay);

        renderInputFluid(state, matrices, queue, light, overlay);
    }

    private void renderInputFluid(CentrifugalConcentratorRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, int light, int overlay) {
        SingleFluidStorage fluidTank = state.inputFluidTank;
        if (fluidTank.isResourceBlank() || fluidTank.amount <= 0)
            return;

        FluidVariant fluidVariant = fluidTank.variant;
        Sprite fluidSprite = FluidVariantRendering.getSprite(fluidVariant);
        if (fluidSprite == null)
            return;

        RenderLayer renderLayer = RenderLayer.getItemEntityTranslucentCull(fluidSprite.getAtlasId());
        World world = MinecraftClient.getInstance().world;

        int sides = 16;
        float outerRadius = 19 / 16f;
        float innerRadius;
        int fluidColor = FluidVariantRendering.getColor(fluidVariant, world, state.pos);

        float fillPercent = fluidTank.amount / (float) fluidTank.getCapacity();

        // fillPercent = (float) (Math.sin(world.getTime() / world.getTickManager().getTickRate()) * 0.5f + 0.5f);

        if (fillPercent <= 7 / 16f) {
            innerRadius = 0;
        } else {
            innerRadius = 4 / 16f;
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

            queue.submitCustom(matrices, renderLayer, (entry, vertexConsumer) -> {
                angledFluidVertex(vertexConsumer, matrices, fluidSprite, fluidColor, angle0, innerRadius, outerRadius, light, overlay);
                angledFluidVertex(vertexConsumer, matrices, fluidSprite, fluidColor, angle0, outerRadius, outerRadius, light, overlay);
                angledFluidVertex(vertexConsumer, matrices, fluidSprite, fluidColor, angle1, outerRadius, outerRadius, light, overlay);
                angledFluidVertex(vertexConsumer, matrices, fluidSprite, fluidColor, angle1, innerRadius, outerRadius, light, overlay);
            });
        }

        float radius = 1.1f;

        for (int i = 0; i < NUM_SPINNING_ITEMS; i++) {
            float angle = -state.bowlRotation + (float) Math.TAU / NUM_SPINNING_ITEMS * i;

            float x = (float) Math.cos(angle) * radius;
            float y = (float) Math.sin(angle * 3) * 0.02f + 0.225f;
            float z = (float) Math.sin(angle) * radius;

            matrices.push();
            matrices.translate(x, y, z);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotation(angle + (float) Math.PI / 2f));
            matrices.scale(0.5f, 0.5f, 0.5f);
            state.renderItemRenderState(0, matrices, queue);

            Vector3f pos = localToWorldPosition(matrices);
            world.addParticleClient(ParticleTypes.BUBBLE, pos.x, pos.y + 0.25, pos.z, 0, 0, 0);

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
