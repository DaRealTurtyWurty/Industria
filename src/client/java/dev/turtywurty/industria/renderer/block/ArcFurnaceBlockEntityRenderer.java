package dev.turtywurty.industria.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.turtywurty.industria.blockentity.ArcFurnaceBlockEntity;
import dev.turtywurty.industria.state.IndustriaBlockEntityRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class ArcFurnaceBlockEntityRenderer extends IndustriaBlockEntityRenderer<ArcFurnaceBlockEntity, IndustriaBlockEntityRenderState> {
    public ArcFurnaceBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void onRender(IndustriaBlockEntityRenderState state, PoseStack matrices, SubmitNodeCollector queue, int light, int overlay) {

    }
}
