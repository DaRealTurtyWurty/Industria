package dev.turtywurty.industria.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.turtywurty.industria.blockentity.DigesterBlockEntity;
import dev.turtywurty.industria.model.DigesterModel;
import dev.turtywurty.industria.state.IndustriaBlockEntityRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class DigesterBlockEntityRenderer extends IndustriaBlockEntityRenderer<DigesterBlockEntity, IndustriaBlockEntityRenderState> {
    private final DigesterModel model;

    public DigesterBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        this.model = new DigesterModel(context.bakeLayer(DigesterModel.LAYER_LOCATION));
    }

    @Override
    protected void onRender(IndustriaBlockEntityRenderState state, PoseStack matrices, SubmitNodeCollector queue, int light, int overlay) {
        queue.submitModel(this.model, null,
                matrices, this.model.renderType(DigesterModel.TEXTURE_LOCATION),
                light, overlay, 0, state.breakProgress);
    }
}
