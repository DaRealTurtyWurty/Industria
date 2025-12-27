package dev.turtywurty.industria.renderer.block;

import dev.turtywurty.industria.blockentity.ShakingTableBlockEntity;
import dev.turtywurty.industria.model.ShakingTableModel;
import dev.turtywurty.industria.state.ShakingTableRenderState;
import dev.turtywurty.industria.util.ColorMode;
import dev.turtywurty.industria.util.DebugRenderingRegistry;
import dev.turtywurty.industria.util.InWorldFluidRenderingComponent;
import dev.turtywurty.industria.util.IndeterminateBoolean;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ShakingTableBlockEntityRenderer extends IndustriaBlockEntityRenderer<ShakingTableBlockEntity, ShakingTableRenderState> {
    private final ShakingTableModel model;
    private final InWorldFluidRenderingComponent fluidRenderer = new InWorldFluidRenderingComponent();

    public ShakingTableBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
        this.model = new ShakingTableModel(context.getLayerModelPart(ShakingTableModel.LAYER_LOCATION));
    }

    @Override
    public ShakingTableRenderState createRenderState() {
        return new ShakingTableRenderState();
    }

    @Override
    public void updateRenderState(ShakingTableBlockEntity blockEntity, ShakingTableRenderState state, float tickProgress, Vec3d cameraPos, ModelCommandRenderer.@Nullable CrumblingOverlayCommand crumblingOverlay) {
        super.updateRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
        state.recipeFrequency = blockEntity.getRecipeFrequency();
        state.progress = blockEntity.getProgress();
        state.maxProgress = blockEntity.getMaxProgress();
        state.shakeBox = blockEntity.createShakeBox();
        state.processingStack = blockEntity.getInputInventory().getStackInSlot(0);
        state.inputFluidTank = blockEntity.getInputFluidTank();

        state.updateItemRenderState(0, this, blockEntity, state.processingStack, ItemDisplayContext.GROUND);
    }

    @Override
    protected void onRender(ShakingTableRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, int light, int overlay) {
        World world = MinecraftClient.getInstance().world;

        float shakeOffset = 0.0f;
        if (state.progress > 0 && state.progress < state.maxProgress) {
            float time = state.tickProgress + world.getTime();
            float frequency = state.recipeFrequency * (float) Math.PI;
            float shakeAmount = 2f;

            shakeOffset = (float) Math.sin(time * frequency) * shakeAmount;
        }

        state.shakeOffset = shakeOffset;

        queue.submitModel(this.model,
                new ShakingTableModel.ShakingTableModelRenderState(state.shakeOffset),
                matrices, this.model.getLayer(ShakingTableModel.TEXTURE_LOCATION),
                light, overlay, 0, state.crumblingOverlay);

        renderGutterFluids(state, matrices, queue, light, overlay, shakeOffset);
        Vec2f fluidEnd = renderSurfaceFluid(state, matrices, queue, light, overlay, shakeOffset);
        renderItemStacks(state, matrices, queue, light, overlay, shakeOffset, fluidEnd.x, fluidEnd.y);
    }

    @Override
    protected void postRender(ShakingTableRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, int light, int overlay) {
        if (DebugRenderingRegistry.debugRendering && state.shakeBox != null) {
            Box shakeBox = state.shakeBox.offset(-state.pos.getX(), -state.pos.getY(), -state.pos.getZ());
            queue.submitCustom(matrices, RenderLayer.getLines(), (matricesEntry, vertexConsumer) ->
                    VertexRendering.drawBox(matricesEntry, vertexConsumer, shakeBox, 1.0f, 1.0f, 1.0f, 1.0f));
        }
    }

    // TODO: Figure out why the items start off centered in the middle of the table
    // and then move to the left side of the table when the shaking starts.
    private void renderItemStacks(ShakingTableRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, int light, int overlay, float shakeOffset, float surfaceFluidX, float surfaceFluidY) {
        if (state.processingStack.isEmpty())
            return;

        matrices.push();
        matrices.translate(0f, 0f, shakeOffset / 16f);

        float depth = 52f / 16f;

        for (float i = 0; i < 4; i++) {
            float x = surfaceFluidX + 2 / 16f;
            float y = (4f / 16f - 0.125f / 16f) + surfaceFluidY;
            float z = (-13 / 16f) + i * (depth / 4f);

            matrices.push();
            matrices.translate(x, y, z);
            matrices.scale(0.5f, 0.5f, 0.5f);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
            state.renderItemRenderState(0, matrices, queue);
            matrices.pop();
        }

        matrices.pop();
    }

    private Vec2f renderSurfaceFluid(ShakingTableRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, int light, int overlay, float shakeOffset) {
        float progress = state.progress / (float) state.maxProgress;

        if (progress <= 0.0f)
            return new Vec2f(0, 0);

        float totalVolume = 3f;
        float width = 3f;

        float minX = 1 + 1 / 16f;
        float startX = minX - 0.4f;
        float endX = -(1 + 2 / 16f);

        float fluidX = MathHelper.lerp(progress, startX, endX);

        float height = totalVolume / width / Math.abs(minX - fluidX);

        matrices.push();
        matrices.translate(0, 0.0f, shakeOffset / 16f);

        this.fluidRenderer.render(state.inputFluidTank,
                queue, matrices,
                light, overlay,
                MinecraftClient.getInstance().world, state.pos,
                fluidX, -4 / 16f, -2.0f - 2f / 16f,
                minX, height, 1 + 2f / 16f,
                0xFFFFFFFF, ColorMode.MULTIPLICATION,
                IndeterminateBoolean.TRUE);

        matrices.pop();

        float endY = -(height / 16f);
        return new Vec2f(fluidX, endY);
    }

    private void renderGutterFluids(ShakingTableRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, int light, int overlay, float shakeOffset) {
        matrices.push();
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
                    MinecraftClient.getInstance().world, state.pos,
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
                    MinecraftClient.getInstance().world, state.pos,
                    x1, y1, z1, x2, 1.999f, z2,
                    0xFFFFFFFF, ColorMode.MULTIPLICATION,
                    IndeterminateBoolean.TRUE);
        }

        matrices.pop();
    }
}
