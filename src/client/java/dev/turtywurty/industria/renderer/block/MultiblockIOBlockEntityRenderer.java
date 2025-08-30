package dev.turtywurty.industria.renderer.block;

import dev.turtywurty.industria.blockentity.AutoMultiblockIOBlockEntity;
import dev.turtywurty.industria.multiblock.old.Port;
import dev.turtywurty.industria.multiblock.PortType;
import dev.turtywurty.industria.multiblock.TransferType;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;

import java.util.Map;

public class MultiblockIOBlockEntityRenderer extends IndustriaBlockEntityRenderer<AutoMultiblockIOBlockEntity> {
    public MultiblockIOBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    protected void onRender(AutoMultiblockIOBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if(!this.context.getEntityRenderDispatcher().shouldRenderHitboxes())
            return;

        matrices.push();
        matrices.translate(0, 1, 0);

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getLines());
        for (Direction direction : Direction.values()) {
            Map<Direction, Port> ports = entity.getPorts(direction);
            if (ports == null)
                continue;

            for (Port port : ports.values()) {
                double size = 0.5;
                for (TransferType<?, ?, ?> transferType : port.portTypes().stream().map(PortType::transferType).toList()) {
                    float[] color = getColor(transferType);

                    VertexRendering.drawBox(
                            matrices,
                            vertexConsumer,
                            -size,
                            -size,
                            -size,
                            size,
                            size,
                            size,
                            color[0],
                            color[1],
                            color[2],
                            0.5F);

                    size += 0.1F;
                }

                // draw the direction
                Direction opposite = direction.getOpposite();
                float xOffset = opposite.getOffsetX() * 0.75F;
                float yOffset = opposite.getOffsetY() * 0.75F;
                float zOffset = opposite.getOffsetZ() * 0.75F;

                float alpha = (float) (Math.sin(entity.getWorld().getTime() % 20) * 0.5 + 0.5F);
                VertexRendering.drawBox(
                        matrices,
                        vertexConsumer,
                        -0.1F + xOffset,
                        -0.1F + yOffset,
                        -0.1F + zOffset,
                        0.1F + xOffset,
                        0.1F + yOffset,
                        0.1F + zOffset,
                        1.0F,
                        1.0F,
                        1.0F,
                        alpha);
            }
        }

        matrices.pop();
    }

    private static float[] getColor(TransferType<?, ?, ?> type) {
        if(type == TransferType.ITEM) {
            return new float[]{0.0F, 0.75F, 0.30F};
        } else if (type == TransferType.ENERGY) {
            return new float[]{1.0F, 1.0F, 51/255F};
        } else if (type == TransferType.FLUID) {
            return new float[]{135/255F, 206/255F, 250/255F};
        } else if (type == TransferType.SLURRY) {
            return new float[]{139/255F, 69/255F, 19/255F};
        } else if (type == TransferType.HEAT) {
            return new float[]{1.0F, 127/255F, 80/255F};
        } else if (type == TransferType.GAS) {
            return new float[]{58/255F, 159/255F, 2/255F};
        }

        throw new IllegalStateException("Unexpected value: " + type);
    }
}
