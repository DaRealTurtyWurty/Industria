package dev.turtywurty.industria.renderer.block;

import dev.turtywurty.industria.blockentity.FluidTankBlockEntity;
import dev.turtywurty.industria.state.FluidTankRenderState;
import dev.turtywurty.industria.util.InWorldFluidRenderingComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class FluidTankBlockEntityRenderer extends IndustriaBlockEntityRenderer<FluidTankBlockEntity, FluidTankRenderState> {
    private final InWorldFluidRenderingComponent fluidRenderingComponent = new InWorldFluidRenderingComponent();

    public FluidTankBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public FluidTankRenderState createRenderState() {
        return new FluidTankRenderState();
    }

    @Override
    public void updateRenderState(FluidTankBlockEntity blockEntity, FluidTankRenderState state, float tickProgress, Vec3d cameraPos, ModelCommandRenderer.@Nullable CrumblingOverlayCommand crumblingOverlay) {
        super.updateRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
        state.fluidTank = blockEntity.getFluidTank();
    }

    @Override
    protected void onRender(FluidTankRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, int light, int overlay) {
        matrices.push();
        matrices.translate(-0.5, 1.5, 0.5);
        this.fluidRenderingComponent.render(state.fluidTank,
                queue, matrices,
                light, overlay,
                MinecraftClient.getInstance().world, state.pos,
                3 / 16f, 0, 3 / 16f,
                13 / 16f, 15, 13 / 16f);
        matrices.pop();
    }
}
