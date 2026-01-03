package dev.turtywurty.industria.renderer.block;

import dev.turtywurty.industria.blockentity.MultiblockControllerBlockEntity;
import dev.turtywurty.industria.state.ExampleMultiblockControllerRenderState;
import net.minecraft.client.render.DrawStyle;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.debug.gizmo.GizmoDrawing;
import org.jetbrains.annotations.Nullable;

public class ExampleMultiblockControllerBlockEntityRenderer extends IndustriaBlockEntityRenderer<MultiblockControllerBlockEntity, ExampleMultiblockControllerRenderState> {
    public ExampleMultiblockControllerBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public ExampleMultiblockControllerRenderState createRenderState() {
        return new ExampleMultiblockControllerRenderState();
    }

    @Override
    public void updateRenderState(MultiblockControllerBlockEntity blockEntity, ExampleMultiblockControllerRenderState state, float tickProgress, Vec3d cameraPos, ModelCommandRenderer.@Nullable CrumblingOverlayCommand crumblingOverlay) {
        super.updateRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
        state.positions.clear();
        state.positions.addAll(blockEntity.getPositions());
    }

    @Override
    protected void onRender(ExampleMultiblockControllerRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, int light, int overlay) {
        matrices.push();
        matrices.translate(0, 1, 0);

        for (BlockPos position : state.positions) {
            GizmoDrawing.box(position, DrawStyle.filledAndStroked(0x7700FF00, 5.0f, 0x5500EE00));
        }

        matrices.pop();
    }
}
