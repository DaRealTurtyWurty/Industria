package dev.turtywurty.industria.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.turtywurty.industria.blockentity.ShakingTableBlockEntity;
import dev.turtywurty.industria.model.ShakingTableModel;
import dev.turtywurty.industria.state.ShakingTableRenderState;
import dev.turtywurty.industria.util.ColorMode;
import dev.turtywurty.industria.util.DebugRenderingRegistry;
import dev.turtywurty.industria.util.InWorldFluidRenderingComponent;
import dev.turtywurty.industria.util.IndeterminateBoolean;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ShakingTableBlockEntityRenderer extends IndustriaBlockEntityRenderer<ShakingTableBlockEntity, ShakingTableRenderState> {
    private final ShakingTableModel model;
    private final InWorldFluidRenderingComponent fluidRenderer = new InWorldFluidRenderingComponent();

    public ShakingTableBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        this.model = new ShakingTableModel(context.bakeLayer(ShakingTableModel.LAYER_LOCATION));
    }

    @Override
    public ShakingTableRenderState createRenderState() {
        return new ShakingTableRenderState();
    }

    @Override
    public void extractRenderState(ShakingTableBlockEntity blockEntity, ShakingTableRenderState state, float tickProgress, Vec3 cameraPos, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
        super.extractRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
        state.recipeFrequency = blockEntity.getRecipeFrequency();
        state.progress = blockEntity.getProgress();
        state.maxProgress = blockEntity.getMaxProgress();
        state.shakeBox = blockEntity.createShakeBox();
        state.processingStack = blockEntity.getInputInventory().getItem(0);
        state.inputFluidTank = blockEntity.getInputFluidTank();

        state.updateItemRenderState(0, this, blockEntity, state.processingStack, ItemDisplayContext.GROUND);
    }

    @Override
    protected void onRender(ShakingTableRenderState state, PoseStack matrices, SubmitNodeCollector queue, int light, int overlay) {
        Level world = Minecraft.getInstance().level;

        float shakeOffset = 0.0f;
        if (state.progress > 0 && state.progress < state.maxProgress) {
            float time = state.tickProgress + world.getGameTime();
            float frequency = state.recipeFrequency * (float) Math.PI;
            float shakeAmount = 2f;

            shakeOffset = (float) Math.sin(time * frequency) * shakeAmount;
        }

        state.shakeOffset = shakeOffset;

        queue.submitModel(this.model,
                new ShakingTableModel.ShakingTableModelRenderState(state.shakeOffset),
                matrices, this.model.renderType(ShakingTableModel.TEXTURE_LOCATION),
                light, overlay, 0, state.breakProgress);

        renderGutterFluids(state, matrices, queue, light, overlay, shakeOffset);
        Vec2 fluidEnd = renderSurfaceFluid(state, matrices, queue, light, overlay, shakeOffset);
        renderItemStacks(state, matrices, queue, light, overlay, shakeOffset, fluidEnd.x, fluidEnd.y);
    }

    @Override
    protected void postRender(ShakingTableRenderState state, PoseStack matrices, SubmitNodeCollector queue, int light, int overlay) {
        if (DebugRenderingRegistry.debugRendering && state.shakeBox != null) {
            Gizmos.cuboid(state.shakeBox, GizmoStyle.stroke(0xFFFF0000));
        }
    }

    // TODO: Figure out why the items start off centered in the middle of the table
    // and then move to the left side of the table when the shaking starts.
    private void renderItemStacks(ShakingTableRenderState state, PoseStack matrices, SubmitNodeCollector queue, int light, int overlay, float shakeOffset, float surfaceFluidX, float surfaceFluidY) {
        if (state.processingStack.isEmpty())
            return;

        matrices.pushPose();
        matrices.translate(0f, 0f, shakeOffset / 16f);

        float depth = 52f / 16f;

        for (float i = 0; i < 4; i++) {
            float x = surfaceFluidX + 2 / 16f;
            float y = (4f / 16f - 0.125f / 16f) + surfaceFluidY;
            float z = (-13 / 16f) + i * (depth / 4f);

            matrices.pushPose();
            matrices.translate(x, y, z);
            matrices.scale(0.5f, 0.5f, 0.5f);
            matrices.mulPose(Axis.XP.rotationDegrees(90));
            state.renderItemRenderState(0, matrices, queue);
            matrices.popPose();
        }

        matrices.popPose();
    }

    private Vec2 renderSurfaceFluid(ShakingTableRenderState state, PoseStack matrices, SubmitNodeCollector queue, int light, int overlay, float shakeOffset) {
        float progress = state.progress / (float) state.maxProgress;

        if (progress <= 0.0f)
            return new Vec2(0, 0);

        float totalVolume = 3f;
        float width = 3f;

        float minX = 1 + 1 / 16f;
        float startX = minX - 0.4f;
        float endX = -(1 + 2 / 16f);

        float fluidX = Mth.lerp(progress, startX, endX);

        float height = totalVolume / width / Math.abs(minX - fluidX);

        matrices.pushPose();
        matrices.translate(0, 0.0f, shakeOffset / 16f);

        this.fluidRenderer.render(state.inputFluidTank,
                queue, matrices,
                light, overlay,
                Minecraft.getInstance().level, state.blockPos,
                fluidX, -4 / 16f, -2.0f - 2f / 16f,
                minX, height, 1 + 2f / 16f,
                0xFFFFFFFF, ColorMode.MULTIPLICATION,
                IndeterminateBoolean.TRUE);

        matrices.popPose();

        float endY = -(height / 16f);
        return new Vec2(fluidX, endY);
    }

    private void renderGutterFluids(ShakingTableRenderState state, PoseStack matrices, SubmitNodeCollector queue, int light, int overlay, float shakeOffset) {
        matrices.pushPose();
        matrices.translate(0.0f, 0.0f, shakeOffset / 16f);
        {
            float x1 = 1f - 1f / 16f;
            float y1 = -2f / 16f;
            float z1 = -1f - 1f / 16f;
            float x2 = 1f + 1f / 16f;
            float z2 = -5f / 16f;
            this.fluidRenderer.render(state.inputFluidTank,
                    queue, matrices,
                    light, overlay,
                    Minecraft.getInstance().level, state.blockPos,
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
            this.fluidRenderer.render(state.inputFluidTank,
                    queue, matrices,
                    light, overlay,
                    Minecraft.getInstance().level, state.blockPos,
                    x1, y1, z1, x2, 1.999f, z2,
                    0xFFFFFFFF, ColorMode.MULTIPLICATION,
                    IndeterminateBoolean.TRUE);
        }

        matrices.popPose();
    }
}
