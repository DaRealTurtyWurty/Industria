package dev.turtywurty.industria.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.turtywurty.industria.blockentity.MultiblockIOBlockEntity;
import dev.turtywurty.industria.multiblock.PortType;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.multiblock.old.Port;
import dev.turtywurty.industria.renderer.world.PipeNetworkWorldRenderer;
import dev.turtywurty.industria.state.MultiblockIORenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.core.Direction;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class MultiblockIOBlockEntityRenderer extends IndustriaBlockEntityRenderer<MultiblockIOBlockEntity, MultiblockIORenderState> {
    public MultiblockIOBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public MultiblockIORenderState createRenderState() {
        return new MultiblockIORenderState();
    }

    @Override
    public void extractRenderState(MultiblockIOBlockEntity blockEntity, MultiblockIORenderState state, float tickProgress, Vec3 cameraPos, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
        super.extractRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);

        state.ports.clear();
        for (Direction direction : Direction.values()) {
            Map<Direction, Port> ports = blockEntity.getPorts(direction);
            if (ports != null) {
                state.ports.put(direction, ports);
            }
        }
    }

    @Override
    protected void onRender(MultiblockIORenderState state, PoseStack matrices, SubmitNodeCollector queue, int light, int overlay) {
        if (!shouldRenderHitboxes())
            return;

        for (Map.Entry<Direction, Map<Direction, Port>> mapEntry : state.ports.entrySet()) {
            Direction direction = mapEntry.getKey();
            Map<Direction, Port> ports = mapEntry.getValue();

            for (Port port : ports.values()) {
                float size = 0.5f;
                for (TransferType<?, ?, ?> transferType : port.portTypes().stream().map(PortType::transferType).toList()) {
                    int color = PipeNetworkWorldRenderer.getColor(transferType);

                    Gizmos.cuboid(state.blockPos, size, GizmoStyle.stroke(color));

                    size += 0.1F;
                }

                // draw the direction
                Direction opposite = direction.getOpposite();
                float xOffset = opposite.getStepX() * 0.75F;
                float yOffset = opposite.getStepY() * 0.75F;
                float zOffset = opposite.getStepZ() * 0.75F;

                float alpha = (float) (Math.sin(Minecraft.getInstance().level.getGameTime() % 20) * 0.5 + 0.5F);
                Gizmos.arrow(state.blockPos.getCenter(), state.blockPos.getCenter().add(xOffset, yOffset, zOffset), 0x88FFFFFF | ((int) (alpha * 255) << 24));
            }
        }
    }
}
