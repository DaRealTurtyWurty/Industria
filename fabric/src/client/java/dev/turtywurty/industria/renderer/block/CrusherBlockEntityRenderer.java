package dev.turtywurty.industria.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.turtywurty.industria.blockentity.CrusherBlockEntity;
import dev.turtywurty.industria.model.CrusherModel;
import dev.turtywurty.industria.state.CrusherRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class CrusherBlockEntityRenderer extends IndustriaBlockEntityRenderer<CrusherBlockEntity, CrusherRenderState> {
    private final CrusherModel model;

    public CrusherBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        this.model = new CrusherModel(context.bakeLayer(CrusherModel.LAYER_LOCATION));
    }

    @Override
    public CrusherRenderState createRenderState() {
        return new CrusherRenderState();
    }

    @Override
    public void extractRenderState(CrusherBlockEntity blockEntity, CrusherRenderState state, float tickProgress, Vec3 cameraPos, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
        super.extractRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
        state.progress = blockEntity.getProgress();
        state.maxProgress = blockEntity.getMaxProgress();
    }

    @Override
    public void onRender(CrusherRenderState state, PoseStack matrices, SubmitNodeCollector queue, int light, int overlay) {
        queue.submitModel(this.model,
                state.progress > 0 ? (float) (((double) state.progress / state.maxProgress) * Math.PI) : 0F,
                matrices, this.model.renderType(CrusherModel.TEXTURE),
                light, overlay, 0, state.breakProgress);
    }
}
