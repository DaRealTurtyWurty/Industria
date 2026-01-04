package dev.turtywurty.industria.renderer.world;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.turtywurty.industria.block.PipeBlock;
import dev.turtywurty.industria.data.ClientPipeNetworks;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.pipe.PipeNetwork;
import dev.turtywurty.industria.pipe.PipeNetworkManager;
import dev.turtywurty.industria.util.DebugRenderingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.CommonColors;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
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
    public void render(LevelRenderContext context) {
        if (!DebugRenderingRegistry.debugRendering)
            return;

        MultiBufferSource consumers = context.bufferSource();
        if (consumers == null)
            return;

        PoseStack matrices = context.poseStack();
        if (matrices == null)
            return;

        Entity cameraEntity = context.gameRenderer().getMainCamera().entity();
        if (cameraEntity == null)
            return;

        ResourceKey<Level> dimension = cameraEntity.level().dimension();
        for (PipeNetworkManager<?, ?> manager : ClientPipeNetworks.get(dimension)) {
            TransferType<?, ?, ?> transferType = manager.getTransferType();
            for (PipeNetwork<?> network : manager.getNetworks()) {
                for (BlockPos pipe : network.getPipes()) {
                    Gizmos.cuboid(pipe, 0.25f, GizmoStyle.stroke(getColor(transferType)));

                    Minecraft client = Minecraft.getInstance();
                    ClientLevel world = client.level;
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

                    Component text = Component.literal(amountStr + pipeBlock.getUnit());

                    Vec3 pos = pipe.getCenter();
                    Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
                    Vec3 cameraPos = camera.position();
                    pos = pos.subtract(cameraPos);

                    matrices.pushPose();
                    matrices.translate(pos.x, pos.y + 0.5, pos.z);
                    matrices.mulPose(camera.rotation());
                    matrices.scale(0.025F, -0.025F, 0.025F);
                    Matrix4f matrix4f = matrices.last().pose();

                    Font textRenderer = client.font;
                    float xOffset = (float) (-textRenderer.width(text)) / 2.0F;

                    textRenderer.drawInBatch(text, xOffset, 0, CommonColors.WHITE, false, matrix4f, consumers, Font.DisplayMode.NORMAL, 0, LightCoordsUtil.FULL_BRIGHT);
                    matrices.popPose();
                }
            }
        }
    }
}
