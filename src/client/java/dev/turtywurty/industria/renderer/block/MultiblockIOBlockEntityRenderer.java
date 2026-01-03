package dev.turtywurty.industria.renderer.block;

import dev.turtywurty.industria.blockentity.MultiblockIOBlockEntity;
import dev.turtywurty.industria.multiblock.PortType;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.multiblock.old.Port;
import dev.turtywurty.industria.renderer.world.PipeNetworkWorldRenderer;
import dev.turtywurty.industria.state.MultiblockIORenderState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.DrawStyle;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.debug.gizmo.GizmoDrawing;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class MultiblockIOBlockEntityRenderer extends IndustriaBlockEntityRenderer<MultiblockIOBlockEntity, MultiblockIORenderState> {
    public MultiblockIOBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public MultiblockIORenderState createRenderState() {
        return new MultiblockIORenderState();
    }

    @Override
    public void updateRenderState(MultiblockIOBlockEntity blockEntity, MultiblockIORenderState state, float tickProgress, Vec3d cameraPos, ModelCommandRenderer.@Nullable CrumblingOverlayCommand crumblingOverlay) {
        super.updateRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);

        state.ports.clear();
        for (Direction direction : Direction.values()) {
            Map<Direction, Port> ports = blockEntity.getPorts(direction);
            if (ports != null) {
                state.ports.put(direction, ports);
            }
        }
    }

    @Override
    protected void onRender(MultiblockIORenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, int light, int overlay) {
        if (!shouldRenderHitboxes())
            return;

        for (Map.Entry<Direction, Map<Direction, Port>> mapEntry : state.ports.entrySet()) {
            Direction direction = mapEntry.getKey();
            Map<Direction, Port> ports = mapEntry.getValue();

            for (Port port : ports.values()) {
                float size = 0.5f;
                for (TransferType<?, ?, ?> transferType : port.portTypes().stream().map(PortType::transferType).toList()) {
                    int color = PipeNetworkWorldRenderer.getColor(transferType);

                    GizmoDrawing.box(state.pos, size, DrawStyle.stroked(color));

                    size += 0.1F;
                }

                // draw the direction
                Direction opposite = direction.getOpposite();
                float xOffset = opposite.getOffsetX() * 0.75F;
                float yOffset = opposite.getOffsetY() * 0.75F;
                float zOffset = opposite.getOffsetZ() * 0.75F;

                float alpha = (float) (Math.sin(MinecraftClient.getInstance().world.getTime() % 20) * 0.5 + 0.5F);
                GizmoDrawing.arrow(state.pos.toCenterPos(), state.pos.toCenterPos().add(xOffset, yOffset, zOffset), 0x88FFFFFF | ((int) (alpha * 255) << 24));
            }
        }
    }
}
