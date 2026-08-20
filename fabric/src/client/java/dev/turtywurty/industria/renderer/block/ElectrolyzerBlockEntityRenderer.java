package dev.turtywurty.industria.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.turtywurty.industria.blockentity.ElectrolyzerBlockEntity;
import dev.turtywurty.industria.model.ElectrolyzerModel;
import dev.turtywurty.industria.state.IndustriaBlockEntityRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class ElectrolyzerBlockEntityRenderer extends IndustriaBlockEntityRenderer<ElectrolyzerBlockEntity, IndustriaBlockEntityRenderState> {
    private final ElectrolyzerModel model;

    public ElectrolyzerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        this.model = new ElectrolyzerModel(context.bakeLayer(ElectrolyzerModel.LAYER_LOCATION));
    }

    @Override
    protected void onRender(IndustriaBlockEntityRenderState state, PoseStack matrices, SubmitNodeCollector queue, int light, int overlay) {
        queue.submitModel(this.model, null,
                matrices, this.model.renderType(ElectrolyzerModel.TEXTURE_LOCATION),
                light, overlay, 0, state.breakProgress);
    }
}
