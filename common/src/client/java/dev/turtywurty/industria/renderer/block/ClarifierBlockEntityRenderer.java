package dev.turtywurty.industria.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.turtywurty.industria.blockentity.ClarifierBlockEntity;
import dev.turtywurty.industria.model.ClarifierModel;
import dev.turtywurty.industria.state.ClarifierRenderState;
import dev.turtywurty.industria.util.FluidAmounts;
import dev.turtywurty.industria.util.InWorldFluidRenderingComponent;
import dev.turtywurty.industria.util.ModelSubmitUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ClarifierBlockEntityRenderer extends IndustriaBlockEntityRenderer<ClarifierBlockEntity, ClarifierRenderState> {
    private final ClarifierModel model;
    private final InWorldFluidRenderingComponent fluidRenderer = new InWorldFluidRenderingComponent();

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
        float progress = state.progress + (blockEntity.isRunning() ? tickProgress : 0f);
        state.bladeRotation = state.maxProgress > 0
                ? Mth.clamp(progress / state.maxProgress, 0f, 1f) * (float) (Math.PI * 2)
                : 0f;
        state.outputInventory = blockEntity.getOutputInventory();
        state.updateItemRenderState(0, this, blockEntity, blockEntity.getOutputInventory().getItem(0));
        state.updateItemRenderState(1, this, blockEntity, blockEntity.getNextOutputItemStack());
    }

    @Override
    protected void onRender(ClarifierRenderState state, PoseStack matrices, SubmitNodeCollector queue, int light, int overlay) {
        ModelSubmitUtils.submitModel(queue, this.model, state,
                matrices, this.model.renderType(ClarifierModel.TEXTURE_LOCATION),
                light, overlay, 0, state.breakProgress);

        renderInputFluid(state, matrices, queue, light, overlay);
        renderOutputFluid(state, matrices, queue, light, overlay);

        renderOutputStack(state, matrices, queue, light, overlay);
    }

    private void renderOutputStack(ClarifierRenderState state, PoseStack matrices, SubmitNodeCollector queue, int light, int overlay) {
        ItemStack outputStack = state.outputInventory.getItem(0);
        if (outputStack.isEmpty())
            return;

        float scale = 0.1f;
        for (int i = 0; i < Mth.clamp(outputStack.getCount(), 1, 64); i++) {
            matrices.pushPose();
            float x = ((i % 8) - 3.5f) * scale;
            float z = ((i / 8) - 3.5f) * scale;
            matrices.translate(x, 1.25f, z);
            matrices.scale(scale, scale, scale);
            state.renderItemRenderState(0, matrices, queue);
            matrices.popPose();
        }
    }

    private void renderInputFluid(ClarifierRenderState state, PoseStack matrices, SubmitNodeCollector queue, int light, int overlay) {
        if (state.inputFluidTank == null || state.inputFluidTank.getResource().isBlank() || state.inputFluidTank.getAmount() <= 0)
            return;

        float fill = Mth.clamp((float) state.inputFluidTank.getAmount() / (FluidAmounts.BUCKET * 5), 0f, 1f);
        float surfaceY = Mth.lerp(fill, 7f / 16f, -24f / 16f);
        this.fluidRenderer.renderOctagonalTopFaceOnly(state.inputFluidTank.getResource(),
                queue, matrices, light, overlay, Minecraft.getInstance().level, state.blockPos,
                surfaceY, 35.5f / 16f, 20.8f / 16f);
    }

    private void renderOutputFluid(ClarifierRenderState state, PoseStack matrices, SubmitNodeCollector queue, int light, int overlay) {
        if (state.outputFluidTank == null || state.outputFluidTank.getResource().isBlank() || state.outputFluidTank.getAmount() <= 0)
            return;

        float fill = Mth.clamp((float) state.outputFluidTank.getAmount() / (FluidAmounts.BUCKET * 5), 0f, 1f);
        float surfaceY = Mth.lerp(fill, 22f / 16f, 9f / 16f);
        this.fluidRenderer.renderOctagonalTopFaceOnly(state.outputFluidTank.getResource(),
                queue, matrices, light, overlay, Minecraft.getInstance().level, state.blockPos,
                surfaceY, 17.2f / 16f, 10.1f / 16f);
    }
}
