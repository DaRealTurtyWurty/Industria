package dev.turtywurty.industria.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.turtywurty.industria.blockentity.ClarifierBlockEntity;
import dev.turtywurty.industria.model.ClarifierModel;
import dev.turtywurty.industria.state.ClarifierRenderState;
import dev.turtywurty.industria.util.InWorldFluidRenderingComponent;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ClarifierBlockEntityRenderer extends IndustriaBlockEntityRenderer<ClarifierBlockEntity, ClarifierRenderState> {
    private final ClarifierModel model;
    private final InWorldFluidRenderingComponent fluidRenderer = new InWorldFluidRenderingComponent();

    private static final GridPosition[] OUTPUT_ITEM_POSITIONS = new GridPosition[64];

    static {
        for (int i = 0; i < OUTPUT_ITEM_POSITIONS.length; i++) {
            OUTPUT_ITEM_POSITIONS[i] = getPosition(i, 14);
        }
    }

    private static GridPosition getPosition(int index, int width) {
        if (index < 0 || width < 1)
            throw new IllegalArgumentException("Invalid input");

        int y = index / width;
        int xIndex = index % width;
        int x;
        if (xIndex % 2 == 0) {
            x = xIndex / 2;              // Even: left side
        } else {
            x = width - 1 - (xIndex / 2); // Odd: right side
        }

        return new GridPosition(x, y);
    }

    public ClarifierBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);

        this.model = new ClarifierModel(context.bakeLayer(ClarifierModel.LAYER_LOCATION));
    }

    @Override
    public ClarifierRenderState createRenderState() {
        return new ClarifierRenderState();
    }

    @Override
    public void extractRenderState(ClarifierBlockEntity blockEntity, ClarifierRenderState state, float tickProgress, Vec3 cameraPos, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
        super.extractRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
        state.progress = blockEntity.getProgress();
        state.maxProgress = blockEntity.getMaxProgress();
        state.inputFluidTank = blockEntity.getInputFluidTank();
        state.outputFluidTank = blockEntity.getOutputFluidTank();
        state.nextOutputStack = blockEntity.getNextOutputItemStack();
        state.outputInventory = blockEntity.getOutputInventory();
        state.updateItemRenderState(0, this, blockEntity, blockEntity.getOutputInventory().getItem(0));
        state.updateItemRenderState(1, this, blockEntity, blockEntity.getNextOutputItemStack());
    }

    @Override
    protected void onRender(ClarifierRenderState state, PoseStack matrices, SubmitNodeCollector queue, int light, int overlay) {
        queue.submitModel(this.model, null,
                matrices, this.model.renderType(ClarifierModel.TEXTURE_LOCATION),
                light, overlay, 0, state.breakProgress);

        renderInputFluid(state, matrices, queue, light, overlay);
        renderOutputFluid(state, matrices, queue, light, overlay);

        renderCurrentOutputItem(state, matrices, queue, light, overlay);
        renderOutputStack(state, matrices, queue, light, overlay);
    }

    private void renderOutputStack(ClarifierRenderState state, PoseStack matrices, SubmitNodeCollector queue, int light, int overlay) {
        ItemStack outputStack = state.outputInventory.getItem(0);
        if (outputStack.isEmpty())
            return;

        float scale = 0.05f;
        float zOffset = 11f / 16f + 10f / 16f + 2f / 16f;
        float startY = 0.75f - scale / 2 + 9f / 16f;

        for (int i = 0; i < Mth.clamp(outputStack.getCount(), 1, 64); i++) {
            matrices.pushPose();

            GridPosition position = OUTPUT_ITEM_POSITIONS[i];
            float xOff = position.x * (scale + (0.0625f * scale)) - 0.345f;
            float yOff = position.y * (scale + (0.0625f * scale));
            matrices.translate(xOff, startY - yOff, zOffset);
            matrices.scale(scale, scale, scale);
            state.renderItemRenderState(0, matrices, queue);
            matrices.popPose();
        }
    }

    // Thanks to Basti for the item rendering math
    private void renderCurrentOutputItem(ClarifierRenderState state, PoseStack matrices, SubmitNodeCollector queue, int light, int overlay) {
        if (state.nextOutputStack.isEmpty())
            return;

        float itemProgress = (float) state.progress / state.maxProgress;

        float scale = 0.15f;
        float firstStretch = 11f / 16f;
        float rampStretch = 10f / 16f;
        float rampHeight = 9f / 16f;
        float finalStretch = 1f / 16f;

        float dz = 0;
        float dy = 0;
        float rotation = 0;
        if (itemProgress >= 0 && itemProgress < 0.6) {
            dz = Mth.map(itemProgress, 0, 0.6f, 0, firstStretch);
            dy = 0;
        }

        if (itemProgress >= 0.6 && itemProgress < 0.85) {
            float t = Mth.map(itemProgress, 0.6f, 0.85f, 0, 1);
            t = (float) (0.7 * Math.pow(t, 2) + 0.3 * t); // Curve t, so that t(0) = 0, t(1) = 1, t´(0) = 0.3
            dz = Mth.lerp(t, firstStretch, firstStretch + rampStretch);
            dy = Mth.lerp(t, 0, -rampHeight);

            rotation = (float) (Mth.lerp(t, 0, -Math.PI * 2));
        }

        if (itemProgress >= 0.85 && itemProgress < 1) {
            dz = Mth.map(itemProgress, 0.85f, 1f, firstStretch + rampStretch, firstStretch + rampStretch + finalStretch);
            dy = -rampHeight;
        }

        matrices.pushPose();
        matrices.translate(0, 0.75 - scale / 2 - dy, 0 + dz);
        matrices.scale(scale, scale, scale);
        matrices.mulPose(Axis.XP.rotation(rotation));
        state.renderItemRenderState(1, matrices, queue);
        matrices.popPose();
    }

    private void renderInputFluid(ClarifierRenderState state, PoseStack matrices, SubmitNodeCollector queue, int light, int overlay) {
        if (state.inputFluidTank == null || state.inputFluidTank.isResourceBlank() || state.inputFluidTank.amount <= 0)
            return;

        FluidVariant fluidVariant = state.inputFluidTank.getResource();

        long amount = state.inputFluidTank.amount;
        float fluidProgress = (float) amount / (FluidConstants.BUCKET * 5);
        // fluidProgress = (float) (Math.sin(entity.getWorld().getTime() / 64.0) * 0.5 + 0.5);
        float fluidHeight = -0.625f + (fluidProgress * 1 + 1.999f / 16f);

        float size = 1.25f;
        if (fluidHeight < 0f)
            size = 0.5f;

        this.fluidRenderer.renderTopFaceOnly(fluidVariant,
                queue, matrices,
                light, overlay,
                Minecraft.getInstance().level, state.blockPos,
                -size, fluidHeight, -size,
                size, size);
    }

    private void renderOutputFluid(ClarifierRenderState state, PoseStack matrices, SubmitNodeCollector queue, int light, int overlay) {
        if (state.outputFluidTank == null || state.outputFluidTank.isResourceBlank() || state.outputFluidTank.amount <= 0)
            return;

        FluidVariant fluidVariant = state.outputFluidTank.getResource();
        Level world = Minecraft.getInstance().level;

        long amount = state.outputFluidTank.amount;
        float fluidProgress = (float) amount / (FluidConstants.BUCKET * 5);
        // fluidProgress = (float) (Math.sin(world.getTime() / 64.0) * 0.5 + 0.5);
        float fluidHeight = -1.375f + (fluidProgress * 0.5f);

        this.fluidRenderer.renderTopFaceOnly(fluidVariant,
                queue, matrices,
                light, overlay,
                world, state.blockPos,
                -0.375f, fluidHeight, -0.5f,
                0.375f, 1.4375f);

        matrices.pushPose();
        matrices.mulPose(Axis.YP.rotationDegrees(180));
        this.fluidRenderer.drawTiledXYQuadOnly(fluidVariant,
                queue, matrices,
                light, overlay,
                world, state.blockPos,
                -0.375f, -1.375f, -1.4375f,
                0.375f, fluidHeight, -1.4375f);

        matrices.popPose();
    }

    private record GridPosition(int x, int y) {
    }
}
