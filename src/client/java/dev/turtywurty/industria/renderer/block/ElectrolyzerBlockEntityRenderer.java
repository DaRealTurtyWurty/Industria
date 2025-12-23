package dev.turtywurty.industria.renderer.block;

import dev.turtywurty.industria.blockentity.ElectrolyzerBlockEntity;
import dev.turtywurty.industria.model.ElectrolyzerModel;
import dev.turtywurty.industria.state.IndustriaBlockEntityRenderState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.util.math.MatrixStack;

public class ElectrolyzerBlockEntityRenderer extends IndustriaBlockEntityRenderer<ElectrolyzerBlockEntity, IndustriaBlockEntityRenderState> {
    private final ElectrolyzerModel model;

    public ElectrolyzerBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
        this.model = new ElectrolyzerModel(context.getLayerModelPart(ElectrolyzerModel.LAYER_LOCATION));
    }

    @Override
    protected void onRender(IndustriaBlockEntityRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, int light, int overlay) {
        RenderLayer renderLayer = this.model.getLayer(ElectrolyzerModel.TEXTURE_LOCATION);
        queue.submitModel(this.model, state, matrices, renderLayer, light, overlay, 0, state.crumblingOverlay);
    }
}
