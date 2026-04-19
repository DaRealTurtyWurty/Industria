package dev.turtywurty.industria.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.turtywurty.industria.blockentity.ArcFurnaceBlockEntity;
import dev.turtywurty.industria.model.ArcFurnaceModel;
import dev.turtywurty.industria.state.IndustriaBlockEntityRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class ArcFurnaceBlockEntityRenderer extends IndustriaBlockEntityRenderer<ArcFurnaceBlockEntity, IndustriaBlockEntityRenderState> {
    private final ArcFurnaceModel model;

    public ArcFurnaceBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        this.model = new ArcFurnaceModel(context.bakeLayer(ArcFurnaceModel.LAYER_LOCATION));
    }

    @Override
    protected void onRender(IndustriaBlockEntityRenderState state, PoseStack matrices, SubmitNodeCollector queue, int light, int overlay) {
        queue.submitModel(this.model, null,
                matrices, this.model.renderType(ArcFurnaceModel.TEXTURE_LOCATION),
                light, overlay, 0, state.breakProgress);
    }
}
