package dev.turtywurty.industria.renderer.block;

import dev.turtywurty.industria.blockentity.CrusherBlockEntity;
import dev.turtywurty.industria.model.CrusherModel;
import dev.turtywurty.industria.state.CrusherRenderState;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class CrusherBlockEntityRenderer extends IndustriaBlockEntityRenderer<CrusherBlockEntity, CrusherRenderState> {
    private final CrusherModel model;

    public CrusherBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
        this.model = new CrusherModel(context.getLayerModelPart(CrusherModel.LAYER_LOCATION));
    }

    @Override
    public CrusherRenderState createRenderState() {
        return new CrusherRenderState();
    }

    @Override
    public void updateRenderState(CrusherBlockEntity blockEntity, CrusherRenderState state, float tickProgress, Vec3d cameraPos, ModelCommandRenderer.@Nullable CrumblingOverlayCommand crumblingOverlay) {
        super.updateRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
        state.progress = blockEntity.getProgress();
    }

    @Override
    public void onRender(CrusherRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, int light, int overlay) {
        queue.submitModel(this.model,
                state.progress > 0 ? state.progress / 100.0F : 0F,
                matrices, this.model.getLayer(CrusherModel.TEXTURE),
                light, overlay, 0, state.crumblingOverlay);
    }
}
