package dev.turtywurty.industria.renderer.world;

import com.google.common.collect.ImmutableMap;
import dev.turtywurty.industria.block.PipeBlock;
import dev.turtywurty.industria.data.ClientPipeNetworks;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.pipe.PipeNetwork;
import dev.turtywurty.industria.pipe.PipeNetworkManager;
import dev.turtywurty.industria.util.DebugRenderingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Matrix4f;

import java.util.Map;

public class PipeNetworkWorldRenderer implements IndustriaWorldRenderer {
    private static final Map<TransferType<?, ?, ?>, float[]> COLOR_MAP;

    static {
        ImmutableMap.Builder<TransferType<?, ?, ?>, float[]> builder = ImmutableMap.builder();
        for (TransferType<?, ?, ?> transferType : TransferType.getValues()) {
            float[] color;
            if(transferType == TransferType.ITEM) {
                color = new float[]{0.0F, 0.0F, 0.0F};
            } else if (transferType == TransferType.ENERGY) {
                color = new float[]{1.0F, 1.0F, 51/255F};
            } else if (transferType == TransferType.FLUID) {
                color = new float[]{135/255F, 206/255F, 250/255F};
            } else if (transferType == TransferType.SLURRY) {
                color = new float[]{139/255F, 69/255F, 19/255F};
            } else if (transferType == TransferType.HEAT) {
                color = new float[]{1.0F, 127/255F, 80/255F};
            } else {
                color = new float[]{0.5F, 0.5F, 0.5F};
            }

            builder.put(transferType, color);
        }

        COLOR_MAP = builder.build();
    }

    @Override
    public void render(WorldRenderContext context) {
        if(!DebugRenderingRegistry.debugRendering)
            return;

        VertexConsumerProvider consumers = context.consumers();
        if(consumers == null)
            return;

        MatrixStack matrices = context.matrixStack();
        if(matrices == null)
            return;

        Entity cameraEntity = context.camera().getFocusedEntity();
        if(cameraEntity == null)
            return;

        RegistryKey<World> dimension = cameraEntity.getEntityWorld().getRegistryKey();
        for (PipeNetworkManager<?, ?> manager : ClientPipeNetworks.get(dimension)) {
            TransferType<?, ?, ?> transferType = manager.getTransferType();
            float[] color = COLOR_MAP.get(transferType);
            for (PipeNetwork<?> network : manager.getNetworks()) {
                for (BlockPos pipe : network.getPipes()) {
                    Vec3d vertex = pipe.toCenterPos();

                    Vec3d cameraPos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();
                    Vec3d pos = vertex.subtract(cameraPos);

                    VertexConsumer vertexConsumer = consumers.getBuffer(RenderLayer.getLines());
                    VertexRendering.drawBox(
                            matrices,
                            vertexConsumer,
                            new Box(pos, pos).expand(0.25),
                            color[0],
                            color[1],
                            color[2],
                            0.5F);

                    MinecraftClient client = MinecraftClient.getInstance();
                    ClientWorld world = client.world;
                    if(world == null)
                        return;

                    Block block = world.getBlockState(pipe).getBlock();
                    if (!(block instanceof PipeBlock<?, ?, ?> pipeBlock))
                        continue;
                    double amount = transferType.getAmount(world, pipe).doubleValue();
                    if(transferType == TransferType.HEAT) {
                        amount += 23;
                    }

                    String amountStr = String.format("%.2f", amount).replace(".00", "");

                    Text text = Text.literal(amountStr + pipeBlock.getUnit());

                    matrices.push();
                    matrices.translate(pos.x, pos.y + 0.5, pos.z);
                    matrices.multiply(client.getEntityRenderDispatcher().getRotation());
                    matrices.scale(0.025F, -0.025F, 0.025F);
                    Matrix4f matrix4f = matrices.peek().getPositionMatrix();

                    TextRenderer textRenderer = client.textRenderer;
                    float xOffset = (float)(-textRenderer.getWidth(text)) / 2.0F;

                    textRenderer.draw(text, xOffset, 0, Colors.WHITE, false, matrix4f, consumers, TextRenderer.TextLayerType.NORMAL, 0, LightmapTextureManager.MAX_LIGHT_COORDINATE);
                    matrices.pop();
                }
            }
        }
    }
}
