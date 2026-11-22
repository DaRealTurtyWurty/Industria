package dev.turtywurty.industria.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.turtywurty.industria.blockentity.SteamBoilerBlockEntity;
import dev.turtywurty.industria.state.IndustriaBlockEntityRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class SteamBoilerBlockEntityRenderer
    extends IndustriaBlockEntityRenderer<SteamBoilerBlockEntity, IndustriaBlockEntityRenderState> {
    public SteamBoilerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void setupBlockEntityTransformations(PoseStack matrices, IndustriaBlockEntityRenderState state) {
        matrices.pushPose();
        if (state.multiblockFormed) {
            // The controller occupies the center of the 3-by-3 foundation.
            matrices.translate(-1.0, 0.0, -1.0);
            matrices.scale(3.0f, 4.0f, 3.0f);
        }
    }

    @Override
    protected void onRender(
        IndustriaBlockEntityRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        int light,
        int overlay
    ) {
        if (state.movingBlockRenderState != null) {
            queue.submitMovingBlock(matrices, state.movingBlockRenderState, 0);
        }
    }
}
