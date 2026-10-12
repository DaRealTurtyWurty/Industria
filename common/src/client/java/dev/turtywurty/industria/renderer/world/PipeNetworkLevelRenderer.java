package dev.turtywurty.industria.renderer.world;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.turtywurty.industria.block.PipeBlock;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.pipe.ClientPipeNetworks;
import dev.turtywurty.industria.pipe.PipeNetwork;
import dev.turtywurty.industria.pipe.PipeNetworkManager;
import dev.turtywurty.industria.util.DebugRenderingRegistry;
import dev.turtywurty.turtymultiloader.event.client.LevelRenderContext;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
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

public class PipeNetworkLevelRenderer implements IndustriaLevelRenderer {
    public static int getColor(TransferType<?, ?, ?> type) {
        if (type == TransferType.ITEM)
            return 0x8800BF4D;
        else if (type == TransferType.ENERGY)
            return 0x88FFFF33;
        else if (type == TransferType.FLUID)
            return 0x8887CEFA;
        else if (type == TransferType.SLURRY)
            return 0x888B4513;
        else if (type == TransferType.GAS)
            return 0x883A9F02;

        throw new IllegalStateException("Unexpected value: " + type);
    }

    @Override
    public void render(LevelRenderContext context) {
        if (!DebugRenderingRegistry.debugRendering)
            return;

        SubmitNodeCollector consumers = context.submitNodeCollector();
        if (consumers == null)
            return;

        PoseStack matrices = context.poseStack();
        if (matrices == null)
            return;

        Entity cameraEntity = context.gameRenderer().mainCamera().entity();
        if (cameraEntity == null)
            return;

        ResourceKey<Level> dimension = cameraEntity.level().dimension();
        List<PipeNetworkManager<?, ?>> pipeNetworkManagers = ClientPipeNetworks.get(dimension);
        if (pipeNetworkManagers == null)
            return;

        for (PipeNetworkManager<?, ?> manager : pipeNetworkManagers) {
            TransferType<?, ?, ?> transferType = manager.getTransferType();
            for (PipeNetwork<?> network : manager.getNetworks()) {
                for (BlockPos pipe : network.getPipes()) {
                    Gizmos.cuboid(pipe, 0.25f, GizmoStyle.stroke(getColor(transferType)));

                    Minecraft client = Minecraft.getInstance();
                    ClientLevel world = client.level;
                    if (world == null)
                        return;

                    Block block = world.getBlockState(pipe).getBlock();
                    if (!(block instanceof PipeBlock<?, ?> pipeBlock))
                        continue;

                    double amount = transferType.getAmount(world, pipe).doubleValue();

                    String amountStr = String.format("%.2f", amount).replace(".00", "");

                    Component text = Component.literal(amountStr + pipeBlock.getUnit());

                    Vec3 pos = Vec3.atCenterOf(pipe);
                    Camera camera = Minecraft.getInstance().gameRenderer.mainCamera();
                    Vec3 cameraPos = camera.position();
                    pos = pos.subtract(cameraPos);

                    matrices.pushPose();
                    matrices.translate(pos.x, pos.y + 0.5, pos.z);
                    matrices.rotate(camera.rotation());
                    matrices.scale(0.025F, -0.025F, 0.025F);
                    Matrix4f matrix4f = matrices.last().pose();

                    Font textRenderer = client.font;
                    float xOffset = (float) (-textRenderer.width(text)) / 2.0F;

                    consumers.submitText(matrices, xOffset, 0, text.getVisualOrderText(), false,
                        Font.DisplayMode.NORMAL,
                        LightCoordsUtil.FULL_BRIGHT, CommonColors.WHITE, 0, 0);
                    matrices.popPose();
                }
            }
        }
    }
}
