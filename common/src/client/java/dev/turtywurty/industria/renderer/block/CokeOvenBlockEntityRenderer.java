package dev.turtywurty.industria.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.turtywurty.industria.blockentity.CokeOvenBlockEntity;
import dev.turtywurty.industria.model.CokeOvenModel;
import dev.turtywurty.industria.state.IndustriaBlockEntityRenderState;
import dev.turtywurty.industria.util.ModelSubmitUtils;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class CokeOvenBlockEntityRenderer
    extends IndustriaBlockEntityRenderer<CokeOvenBlockEntity, IndustriaBlockEntityRenderState> {
    private final CokeOvenModel model;

    public CokeOvenBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        this.model = new CokeOvenModel(context.bakeLayer(CokeOvenModel.LAYER_LOCATION));
    }

    @Override
    protected void setupBlockEntityTransformations(PoseStack poses, IndustriaBlockEntityRenderState state) {
        poses.pushPose();
        if (!state.multiblockFormed)
            return;

        double centerX = state.multiblockRenderOffsetX;
        double centerZ = state.multiblockRenderOffsetZ;
        poses.translate(0.5 + centerX, 1.5, 0.5 + centerZ);
        poses.rotate(Axis.YP.rotation((float) Math.atan2(centerX, centerZ)));
        poses.rotate(Axis.ZP.rotationDegrees(180));
        poses.translate(0, 0, -0.5);
    }

    @Override
    protected void onRender(
        IndustriaBlockEntityRenderState state,
        PoseStack poses,
        SubmitNodeCollector queue,
        int light,
        int overlay
    ) {
        ModelSubmitUtils.submitModel(queue, this.model, null,
            poses, this.model.renderType(CokeOvenModel.TEXTURE_LOCATION),
            light, overlay, 0, state.breakProgress);
    }
}
