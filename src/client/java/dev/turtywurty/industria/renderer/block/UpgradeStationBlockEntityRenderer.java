package dev.turtywurty.industria.renderer.block;

import dev.turtywurty.industria.blockentity.UpgradeStationBlockEntity;
import dev.turtywurty.industria.model.UpgradeStationModel;
import dev.turtywurty.industria.state.UpgradeStationRenderState;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.util.math.MatrixStack;

public class UpgradeStationBlockEntityRenderer extends IndustriaBlockEntityRenderer<UpgradeStationBlockEntity, UpgradeStationRenderState> {
    private final UpgradeStationModel model;

    public UpgradeStationBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
        this.model = new UpgradeStationModel(context.getLayerModelPart(UpgradeStationModel.LAYER_LOCATION));
    }

    @Override
    public UpgradeStationRenderState createRenderState() {
        return new UpgradeStationRenderState();
    }

    @Override
    public void onRender(UpgradeStationRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, int light, int overlay) {
        queue.submitModel(this.model, null,
                matrices, this.model.getLayer(UpgradeStationModel.TEXTURE_LOCATION),
                light, overlay, 0, state.crumblingOverlay);
    }
}
