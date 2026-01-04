package dev.turtywurty.industria.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.turtywurty.industria.blockentity.UpgradeStationBlockEntity;
import dev.turtywurty.industria.model.UpgradeStationModel;
import dev.turtywurty.industria.state.UpgradeStationRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class UpgradeStationBlockEntityRenderer extends IndustriaBlockEntityRenderer<UpgradeStationBlockEntity, UpgradeStationRenderState> {
    private final UpgradeStationModel model;

    public UpgradeStationBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        this.model = new UpgradeStationModel(context.bakeLayer(UpgradeStationModel.LAYER_LOCATION));
    }

    @Override
    public UpgradeStationRenderState createRenderState() {
        return new UpgradeStationRenderState();
    }

    @Override
    public void onRender(UpgradeStationRenderState state, PoseStack matrices, SubmitNodeCollector queue, int light, int overlay) {
        queue.submitModel(this.model, null,
                matrices, this.model.renderType(UpgradeStationModel.TEXTURE_LOCATION),
                light, overlay, 0, state.breakProgress);
    }
}
