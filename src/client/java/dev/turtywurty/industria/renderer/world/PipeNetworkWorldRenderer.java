package dev.turtywurty.industria.renderer.world;

import dev.turtywurty.industria.block.PipeBlock;
import dev.turtywurty.industria.data.ClientPipeNetworks;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.pipe.PipeNetwork;
import dev.turtywurty.industria.pipe.PipeNetworkManager;
import dev.turtywurty.industria.util.DebugRenderingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.DrawStyle;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.debug.gizmo.GizmoDrawing;
import org.joml.Matrix4f;

public class PipeNetworkWorldRenderer implements IndustriaWorldRenderer {
    public static int getColor(TransferType<?, ?, ?> type) {
        if (type == TransferType.ITEM) {
            return 0x8800BF4D;
        } else if (type == TransferType.ENERGY) {
            return 0x88FFFF33;
        } else if (type == TransferType.FLUID) {
            return 0x8887CEFA;
        } else if (type == TransferType.SLURRY) {
            return 0x888B4513;
        } else if (type == TransferType.HEAT) {
            return 0x88FF7F50;
        } else if (type == TransferType.GAS) {
            return 0x883A9F02;
        }

        throw new IllegalStateException("Unexpected value: " + type);
    }

    @Override
    public void render(WorldRenderContext context) {
        if (!DebugRenderingRegistry.debugRendering)
            return;

        VertexConsumerProvider consumers = context.consumers();
        if (consumers == null)
            return;

        MatrixStack matrices = context.matrices();
        if (matrices == null)
            return;

        Entity cameraEntity = context.gameRenderer().getCamera().getFocusedEntity();
        if (cameraEntity == null)
            return;

        RegistryKey<World> dimension = cameraEntity.getEntityWorld().getRegistryKey();
        for (PipeNetworkManager<?, ?> manager : ClientPipeNetworks.get(dimension)) {
            TransferType<?, ?, ?> transferType = manager.getTransferType();
            for (PipeNetwork<?> network : manager.getNetworks()) {
                for (BlockPos pipe : network.getPipes()) {
                    GizmoDrawing.box(pipe, 0.25f, DrawStyle.stroked(getColor(transferType)));

                    MinecraftClient client = MinecraftClient.getInstance();
                    ClientWorld world = client.world;
                    if (world == null)
                        return;

                    Block block = world.getBlockState(pipe).getBlock();
                    if (!(block instanceof PipeBlock<?, ?, ?> pipeBlock))
                        continue;

                    double amount = transferType.getAmount(world, pipe).doubleValue();
                    if (transferType == TransferType.HEAT) {
                        amount += 23;
                    }

                    String amountStr = String.format("%.2f", amount).replace(".00", "");

                    Text text = Text.literal(amountStr + pipeBlock.getUnit());

                    Vec3d pos = pipe.toCenterPos();
                    Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
                    Vec3d cameraPos = camera.getCameraPos();
                    pos = pos.subtract(cameraPos);

                    matrices.push();
                    matrices.translate(pos.x, pos.y + 0.5, pos.z);
                    matrices.multiply(camera.getRotation());
                    matrices.scale(0.025F, -0.025F, 0.025F);
                    Matrix4f matrix4f = matrices.peek().getPositionMatrix();

                    TextRenderer textRenderer = client.textRenderer;
                    float xOffset = (float) (-textRenderer.getWidth(text)) / 2.0F;

                    textRenderer.draw(text, xOffset, 0, Colors.WHITE, false, matrix4f, consumers, TextRenderer.TextLayerType.NORMAL, 0, LightmapTextureManager.MAX_LIGHT_COORDINATE);
                    matrices.pop();
                }
            }
        }
    }
}
