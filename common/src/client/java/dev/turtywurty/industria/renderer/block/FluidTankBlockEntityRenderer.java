package dev.turtywurty.industria.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.turtywurty.industria.blockentity.FluidTankBlockEntity;
import dev.turtywurty.industria.state.FluidTankRenderState;
import dev.turtywurty.industria.util.InWorldFluidRenderingComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class FluidTankBlockEntityRenderer extends IndustriaBlockEntityRenderer<FluidTankBlockEntity, FluidTankRenderState> {
    private final InWorldFluidRenderingComponent fluidRenderingComponent = new InWorldFluidRenderingComponent();

    public FluidTankBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public FluidTankRenderState createRenderState() {
        return new FluidTankRenderState();
    }

    @Override
    public void extractRenderState(FluidTankBlockEntity blockEntity, FluidTankRenderState state, float tickProgress, Vec3 cameraPos, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
        super.extractRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
        state.fluidTank = blockEntity.getFluidTank();
    }

    @Override
    protected void onRender(FluidTankRenderState state, PoseStack matrices, SubmitNodeCollector queue, int light, int overlay) {
        matrices.pushPose();
        matrices.translate(-0.5, 1.5, 0.5);
        this.fluidRenderingComponent.render(state.fluidTank,
                queue, matrices,
                light, overlay,
                Minecraft.getInstance().level, state.blockPos,
                3 / 16f, 0, 3 / 16f,
                13 / 16f, 15, 13 / 16f);
        matrices.popPose();
    }
}
