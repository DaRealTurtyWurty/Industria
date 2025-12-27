package dev.turtywurty.industria.renderer.block;

import dev.turtywurty.industria.blockentity.MultiblockControllerBlockEntity;
import dev.turtywurty.industria.state.ExampleMultiblockControllerRenderState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
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

        BlockPos entityPos = state.pos;
        for (BlockPos position : state.positions) {
            double relativeX = position.getX() - entityPos.getX();
            double relativeY = entityPos.getY() - position.getY();
            double relativeZ = entityPos.getZ() - position.getZ();

            queue.submitCustom(matrices, RenderLayer.getDebugFilledBox(), (entry, vertexConsumer) ->
                    drawFilledBox(entry, vertexConsumer,
                            relativeX - 0.5, relativeY - 0.5, relativeZ - 0.5,
                            relativeX + 0.5, relativeY + 0.5, relativeZ + 0.5,
                            0f, 1f, 0f, 0.5f));
        }

        matrices.pop();
    }
}
