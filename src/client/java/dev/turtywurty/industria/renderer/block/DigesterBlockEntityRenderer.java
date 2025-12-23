package dev.turtywurty.industria.renderer.block;

import dev.turtywurty.industria.blockentity.DigesterBlockEntity;
import dev.turtywurty.industria.model.DigesterModel;
import dev.turtywurty.industria.state.IndustriaBlockEntityRenderState;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.util.math.MatrixStack;

public class DigesterBlockEntityRenderer extends IndustriaBlockEntityRenderer<DigesterBlockEntity, IndustriaBlockEntityRenderState> {
    private final DigesterModel model;

    public DigesterBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
        this.model = new DigesterModel(context.getLayerModelPart(DigesterModel.LAYER_LOCATION));
    }

    @Override
    protected void onRender(IndustriaBlockEntityRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, int light, int overlay) {
        queue.submitModel(this.model, null,
                matrices, this.model.getLayer(DigesterModel.TEXTURE_LOCATION),
                light, overlay, 0, state.crumblingOverlay);
    }
}
